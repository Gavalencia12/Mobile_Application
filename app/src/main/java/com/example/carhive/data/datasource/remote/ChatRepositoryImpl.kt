package com.example.carhive.data.datasource.remote

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.carhive.Domain.model.Message
import com.example.carhive.data.exception.RepositoryException
import com.example.carhive.data.mapper.MessageMapper
import com.example.carhive.data.model.CarEntity
import com.example.carhive.data.model.CarWithLastMessage
import com.example.carhive.data.model.MessageEntity
import com.example.carhive.data.model.UserEntity
import com.example.carhive.data.model.UserWithLastMessage
import com.example.carhive.data.repository.ChatRepository
import com.example.carhive.di.DatabaseModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Implementation of ChatRepository that provides methods for handling chat messages,
 * including sending, receiving, and managing message files.
 */
class ChatRepositoryImpl @Inject constructor(
    private val context: Context,
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val messageMapper: MessageMapper
) : ChatRepository {

    /**
     * Retrieves messages for a specific chat group in real-time.
     * @param ownerId The ID of the owner (seller).
     * @param carId The ID of the car associated with the chat.
     * @param buyerId The ID of the buyer.
     * @return A Flow emitting Message objects as they are added to the chat.
     */
    override fun getMessages(ownerId: String, carId: String, buyerId: String): Flow<Message> =
        callbackFlow {
            val messagesRef = database.reference
                .child("ChatGroups")
                .child(ownerId)
                .child(carId)
                .child("messages")
                .child(buyerId)

            val listener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val messageEntity = snapshot.getValue(MessageEntity::class.java)
                    messageEntity?.let { message ->
                        trySend(messageMapper.mapToDomain(message))
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }
            messagesRef.addChildEventListener(listener)
            awaitClose { messagesRef.removeEventListener(listener) }
        }

    /**
     * Retrieves all messages for a specific chat group in a single query.
     * @param ownerId The ID of the owner (seller).
     * @param carId The ID of the car associated with the chat.
     * @param buyerId The ID of the buyer.
     * @return A list of Message objects representing all messages in the chat.
     */
    override suspend fun getAllMessagesOnce(ownerId: String, carId: String, buyerId: String): List<Message> {
        val messages = mutableListOf<Message>()
        val snapshot = database.reference
            .child("ChatGroups")
            .child(ownerId)
            .child(carId)
            .child("messages")
            .child(buyerId)
            .get()
            .await()

        for (child in snapshot.children) {
            val messageEntity = child.getValue(MessageEntity::class.java)
            messageEntity?.let { messages.add(messageMapper.mapToDomain(it)) }
        }
        return messages
    }

    /**
     * Sends a message to a specific chat group.
     * @param ownerId The ID of the owner (seller).
     * @param carId The ID of the car associated with the chat.
     * @param buyerId The ID of the buyer.
     * @param message The Message object to be sent.
     */
    override suspend fun sendMessage(
        ownerId: String,
        carId: String,
        buyerId: String,
        message: Message
    ): Result<Unit> {
        return try {
            val messageRef = database.reference
                .child("ChatGroups")
                .child(ownerId)
                .child(carId)
                .child("messages")
                .child(buyerId)
                .push()

            val messageEntity = messageMapper.mapToEntity(message.copy(
                messageId = messageRef.key ?: "",
            ))
            messageRef.setValue(messageEntity).await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Mark as "failed" if sending fails
            updateMessageStatus(ownerId, carId, buyerId, message.messageId, "failed")
            Result.failure(Exception("Error sending message: ${e.message}", e))
        }
    }

    /**
     * Sends a file message, uploading the file to Firebase Storage and saving its metadata.
     * @param ownerId The ID of the owner (seller).
     * @param carId The ID of the car associated with the chat.
     * @param buyerId The ID of the buyer.
     * @param fileUri The URI of the file to send.
     * @param fileType The type of the file (e.g., image, video).
     * @param fileName The name of the file.
     * @param fileHash The unique hash of the file.
     * @param receiver The ID of the message receiver.
     * @param deletedFor List of users for whom the message is deleted.
     */
    override suspend fun sendFileMessage(
        ownerId: String,
        carId: String,
        buyerId: String,
        fileUri: Uri,
        fileType: String,
        fileName: String,
        fileHash: String,
        receiver: String,
        deletedFor: List<String>
    ): Result<Unit> {
        return try {
            val fileSize = context.contentResolver.openFileDescriptor(fileUri, "r")?.statSize ?: 0L

            // Define the storage folder based on file type
            val folder = when {
                fileType.startsWith("image/") -> "images"
                fileType.startsWith("video/") -> "videos"
                else -> "documents"
            }

            // Check if the file with the same hash already exists
            val fileRef = database.reference.child("ChatGroups").child("Files").child(folder).child(fileHash)
            val fileSnapshot = fileRef.get().await()

            val downloadUri: Uri
            if (!fileSnapshot.exists()) {
                // Upload file to Firebase Storage if it doesn't exist
                val storageReference = storage.reference.child("chat_files/$buyerId/$folder/$fileName")
                val uploadTask = storageReference.putFile(fileUri).await()
                downloadUri = uploadTask.storage.downloadUrl.await()

                // Save file metadata in Firebase Database
                val fileData = mapOf(
                    "name" to fileName,
                    "timestamp" to System.currentTimeMillis(),
                    "type" to fileType,
                    "size" to fileSize,
                    "url" to downloadUri.toString(),
                    "users" to listOf(buyerId)
                )
                fileRef.setValue(fileData).await()
            } else {
                // Use existing file URL and update user list if necessary
                downloadUri = Uri.parse(fileSnapshot.child("url").value as String)
                val existingUsers = fileSnapshot.child("users").value as MutableList<String>
                if (!existingUsers.contains(buyerId)) {
                    existingUsers.add(buyerId)
                    fileRef.child("users").setValue(existingUsers).await()
                }
            }

            // Create message reference in the chat group
            val messageRef = database.reference
                .child("ChatGroups")
                .child(ownerId)
                .child(carId)
                .child("messages")
                .child(buyerId)
                .push()

            // Create and send the message with `deletedFor`
            val message = Message(
                messageId = messageRef.key ?: "",
                senderId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                fileUrl = downloadUri.toString(),
                fileType = fileType,
                fileName = fileName,
                fileSize = fileSize,
                hash = fileHash,
                timestamp = System.currentTimeMillis(),
                receiverId = receiver,
                carId = carId,
                deletedFor = deletedFor.toMutableList()
            )

            // Save the message in Firebase Database
            messageRef.setValue(message).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error sending file message: ${e.message}", e))
        }
    }

    /**
     * Retrieves information about a specific car.
     * @param userId The ID of the car owner.
     * @param carId The ID of the car.
     */
    override suspend fun getUserInfo(userId: String, carId: String): Result<CarEntity?> {
        return try {
            val carSnapshot = database.getReference("Car")
                .child(userId)
                .child(carId)
                .get()
                .await()

            if (carSnapshot.exists()) {
                val car = carSnapshot.getValue(CarEntity::class.java)
                Result.success(car)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error fetching car details from database", e))
        }
    }

    /**
     * Fetches users who are interested in a specific car or chat direction.
     * @param ownerId ID of the car owner.
     * @param userId ID of the user.
     * @param direction Specifies the direction ("sent" or "received").
     * @param type Type of interest.
     * @return A list of CarWithLastMessage or UserWithLastMessage based on the interest type.
     */
    override suspend fun getInterestedUsers(
        ownerId: String?,
        userId: String?,
        direction: String,
        type: String
    ): List<Any> {
        val resultList = mutableListOf<Any>()

        if (direction == "sent" && userId != null) {
            try {
                val chatGroupsSnapshot = database.reference.child("ChatGroups").get().await()
                chatGroupsSnapshot.children.forEach { sellerSnapshot ->
                    val sellerId = sellerSnapshot.key
                    sellerSnapshot.children.forEach { carSnapshot ->
                        val carId = carSnapshot.key

                        val messagesSnapshot = carSnapshot.child("messages").child(ownerId.toString())

                        // Contador de mensajes no leídos
                        val unreadCount = messagesSnapshot.children.count { messageSnapshot ->
                            val messageStatus = messageSnapshot.child("status").value as? String
                            val receiverId = messageSnapshot.child("receiverId").value as? String
                            messageStatus == "sent" && receiverId == ownerId
                        }

                        messagesSnapshot.children.lastOrNull()?.let { messaSnapshot ->
                            val lastMessageContent = messaSnapshot.child("content").value as? String
                            val lastMessageFileName = messaSnapshot.child("fileName").value as? String
                            val lastMessageFileType = messaSnapshot.child("fileType").value as? String
                            val lastMessageTimestamp = messaSnapshot.child("timestamp").value as? Long ?: 0L
                            val isFile = !lastMessageFileName.isNullOrEmpty()
                            val lastMessageDisplay = lastMessageFileName ?: lastMessageContent ?: ""

                            val carEntityResult = getUserInfo(sellerId ?: "", carId ?: "")
                            carEntityResult.onSuccess { carEntity ->
                                if (carEntity != null) {
                                    val ownerResult = database.reference.child("Users").child(sellerId ?: "").get().await()
                                    val owner = ownerResult.getValue(UserEntity::class.java)

                                    if (owner != null && !resultList.any { it is CarWithLastMessage && it.car.id == carEntity.id }) {
                                        resultList.add(
                                            CarWithLastMessage(
                                                car = carEntity,
                                                owner = owner,
                                                lastMessage = lastMessageDisplay,
                                                lastMessageTimestamp = lastMessageTimestamp,
                                                isFile = isFile,
                                                fileName = if (isFile) lastMessageFileName else null,
                                                fileType = lastMessageFileType,
                                                unreadCount = unreadCount // Asignar el contador de no leídos
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Error handling
            }
        }

        if (direction == "received" && ownerId != null) {
            val carsSnapshot = database.reference.child("ChatGroups").child(ownerId).get().await()
            carsSnapshot.children.forEach { carSnapshot ->
                val carId = carSnapshot.key
                val messagesSnapshot = carSnapshot.child("messages")

                messagesSnapshot.children.forEach { userSnapshot ->
                    val userWithMessage = userSnapshot.key
                    val lastMessageSnapshot = userSnapshot.children.lastOrNull()
                    val unreadCount = userSnapshot.children.count { messageSnapshot ->
                        val messageStatus = messageSnapshot.child("status").value as? String
                        val receiverId = messageSnapshot.child("receiverId").value as? String
                        messageStatus == "sent" && receiverId == ownerId
                    }

                    if (lastMessageSnapshot != null) {
                        val lastMessageContent = lastMessageSnapshot.child("content").value as? String
                        val lastMessageFileName = lastMessageSnapshot.child("fileName").value as? String
                        val lastMessageFileType = lastMessageSnapshot.child("fileType").value as? String
                        val lastMessageTimestamp = lastMessageSnapshot.child("timestamp").value as? Long ?: 0L

                        val isFile = !lastMessageFileName.isNullOrEmpty()
                        val lastMessageDisplay = lastMessageFileName ?: lastMessageContent ?: ""

                        val userEntityResult = database.reference.child("Users").child(userWithMessage ?: "").get().await()
                            .getValue(UserEntity::class.java)?.apply {
                                if (userWithMessage != null) {
                                    id = userWithMessage
                                }
                            }

                        if (userEntityResult != null) {
                            resultList.add(
                                UserWithLastMessage(
                                    user = userEntityResult,
                                    lastMessage = lastMessageDisplay,
                                    lastMessageTimestamp = lastMessageTimestamp,
                                    carId = carId.toString(),
                                    isFile = isFile,
                                    fileName = if (isFile) lastMessageFileName else null,
                                    fileType = lastMessageFileType,
                                    unreadCount = unreadCount
                                )
                            )
                        }
                    }
                }
            }
        }
        return resultList
    }

    /**
     * Cleans up the local database by removing files that no longer exist in the file storage.
     * @param context The application context.
     */
    override suspend fun cleanUpDatabase(context: Context) {
        val fileDao = DatabaseModule.getDatabase(context).downloadedFileDao()

        withContext(Dispatchers.IO) {
            val filesInDb = fileDao.getAllFiles()
            val contentResolver = context.contentResolver

            filesInDb.forEach { file ->
                val directory = when {
                    file.fileType.startsWith("image/") -> "CarHive/Media/Images"
                    file.fileType.startsWith("video/") -> "CarHive/Media/Videos"
                    else -> "CarHive/Media/Documents"
                }

                val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Files.getContentUri("external")
                } else {
                    Uri.fromFile(File(context.getExternalFilesDir(null)?.parentFile, "media/${context.packageName}/$directory/${file.fileName}"))
                }

                var fileExists = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
                    val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
                    val selectionArgs = arrayOf(file.fileName)

                    contentResolver.query(uri, projection, selection, selectionArgs, null).use { cursor ->
                        if (cursor != null && cursor.moveToFirst()) {
                            fileExists = true
                        }
                    }
                } else {
                    fileExists = File(uri.path ?: "").exists()
                }

                if (!fileExists) {
                    fileDao.deleteFileByHash(file.fileHash)
                }
            }
        }
    }

    /**
     * Updates the status of a specific message in the chat.
     * @param ownerId ID of the car owner.
     * @param carId ID of the car associated with the chat.
     * @param buyerId ID of the buyer.
     * @param messageId ID of the message.
     * @param status New status to set for the message.
     */
    override suspend fun updateMessageStatus(ownerId: String, carId: String, buyerId: String, messageId: String, status: String) {
        val messageRef = database.reference.child("ChatGroups")
            .child(ownerId)
            .child(carId)
            .child("messages")
            .child(buyerId)
            .child(messageId)

        messageRef.child("status").setValue(status).await()
    }

    /**
     * Deletes a message for a specific user by adding them to the `deletedFor` list.
     * @param ownerId ID of the car owner.
     * @param carId ID of the car associated with the chat.
     * @param buyerId ID of the buyer.
     * @param messageId ID of the message.
     * @param userId ID of the user for whom the message is deleted.
     */
    override suspend fun deleteMessageForUser(ownerId: String, carId: String, buyerId: String, messageId: String, userId: String) {
        val messageRef = database.reference
            .child("ChatGroups")
            .child(ownerId)
            .child(carId)
            .child("messages")
            .child(buyerId)
            .child(messageId)

        val genericTypeIndicator = object : GenericTypeIndicator<MutableList<String>>() {}
        val deletedForList = messageRef.child("deletedFor").get().await().getValue(genericTypeIndicator) ?: mutableListOf()

        if (!deletedForList.contains(userId)) {
            deletedForList.add(userId)
            messageRef.child("deletedFor").setValue(deletedForList).await()
        }
    }
}