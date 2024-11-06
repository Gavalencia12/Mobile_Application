package com.example.carhive.data.datasource.remote

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val messageMapper: MessageMapper
) : ChatRepository {

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


    override suspend fun sendMessage(
        ownerId: String,
        carId: String,
        buyerId: String,
        message: Message
    ): Result<Unit> {
        return try {
            val messageEntity = messageMapper.mapToEntity(message)
            val messageRef = database.reference
                .child("ChatGroups")
                .child(ownerId)
                .child(carId)
                .child("messages")
                .child(buyerId)
                .push()

            messageRef.setValue(messageEntity).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error sending message: ${e.message}", e))
        }
    }

    override suspend fun sendFileMessage(
        ownerId: String,
        carId: String,
        buyerId: String,
        fileUri: Uri,
        fileType: String,
        fileName: String,
        fileHash: String
    ): Result<Unit> {
        return try {
            // Define la subcarpeta de almacenamiento según el tipo de archivo
            val folder = when {
                fileType.startsWith("image/") -> "images"
                fileType.startsWith("video/") -> "videos"
                else -> "documents"
            }

            // Revisa si el archivo con el hash ya existe en el nodo global de "Files"
            val fileRef =
                database.reference.child("ChatGroups").child("Files").child(folder).child(fileHash)
            val fileSnapshot = fileRef.get().await()

            val downloadUri: Uri
            Log.i("angel", "$fileName")
            if (!fileSnapshot.exists()) {
                // Si el archivo no existe en "Files", sube el archivo a Firebase Storage con el nombre original
                val storageReference =
                    storage.reference.child("chat_files/$buyerId/$folder/$fileName")
                val uploadTask = storageReference.putFile(fileUri).await()
                downloadUri = uploadTask.storage.downloadUrl.await()

                // Guarda los detalles del archivo en "Files" con el nombre original
                val fileData = mapOf(
                    "name" to fileName,  // Guarda el nombre original del archivo
                    "timestamp" to System.currentTimeMillis(),
                    "type" to fileType,
                    "url" to downloadUri.toString(),
                    "users" to listOf(buyerId)
                )
                fileRef.setValue(fileData).await()
            } else {
                // Si ya existe, obtiene la URL de descarga existente y actualiza la lista de usuarios si es necesario
                downloadUri = Uri.parse(fileSnapshot.child("url").value as String)
                val existingUsers = fileSnapshot.child("users").value as MutableList<String>
                if (!existingUsers.contains(buyerId)) {
                    existingUsers.add(buyerId)
                    fileRef.child("users").setValue(existingUsers).await()
                }
            }

            // Crear y enviar el mensaje con el hash del archivo
            val message = Message(
                senderId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                fileUrl = downloadUri.toString(),
                fileType = fileType,
                fileName = fileName,  // Asegúrate de que el `fileName` original se incluya en el mensaje
                hash = fileHash,
                timestamp = System.currentTimeMillis()
            )

            sendMessage(ownerId, carId, buyerId, message)
        } catch (e: Exception) {
            Result.failure(Exception("Error sending file message: ${e.message}", e))
        }
    }


    override suspend fun getUserInfo(userId: String, carId: String): Result<CarEntity?> {
        return try {
            val carSnapshot = database.getReference("Car")
                .child(userId)
                .child(carId)
                .get()
                .await()

            if (carSnapshot.exists()) {
                val car = carSnapshot.getValue(CarEntity::class.java)
                Log.d("getUserInfo", "Car found: ${car?.id}")
                Result.success(car)
            } else {
                Log.d("getUserInfo", "Car not found for userId: $userId, carId: $carId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e("getUserInfo", "Error fetching car details: ${e.message}")
            Result.failure(RepositoryException("Error fetching car details from database", e))
        }
    }

    override suspend fun getInterestedUsers(
        ownerId: String?,
        userId: String?,
        direction: String,
        type: String
    ): List<Any> {
        val resultList = mutableListOf<Any>()

        // Caso para el Usuario (Comprador)
        if (direction == "sent" && userId != null) {
            Log.d("getInterestedUsers", "Direction: sent - Checking chats for userId: $userId")

            // Recorre todos los `ChatGroups`
            val chatGroupsSnapshot = database.reference.child("ChatGroups").get().await()
            Log.d("getInterestedUsers", "Total sellers in ChatGroups: ${chatGroupsSnapshot.childrenCount}")

            chatGroupsSnapshot.children.forEach { sellerSnapshot ->
                val sellerId = sellerSnapshot.key
                Log.d("getInterestedUsers", "Checking sellerId: $sellerId")

                sellerSnapshot.children.forEach { carSnapshot ->
                    val carId = carSnapshot.key
                    Log.d("getInterestedUsers", "Checking carId: $carId for sellerId: $sellerId")

                    val messagesSnapshot = carSnapshot.child("messages")

                    // Verifica si el `userId` está presente en los mensajes
                    if (messagesSnapshot.hasChild(userId)) {
                        Log.d("getInterestedUsers", "User $userId has sent a message in carId: $carId for sellerId: $sellerId")

                        // Obtiene la información del coche
                        val carEntityResult = getUserInfo(sellerId ?: "", carId ?: "")
                        carEntityResult.onSuccess { carEntity ->
                            if (carEntity != null) {
                                Log.d("getInterestedUsers", "Car found: ${carEntity.id}")
                                resultList.add(CarWithLastMessage(carEntity, "Último mensaje aquí", 0L))
                            } else {
                                Log.d("getInterestedUsers", "No car found for carId: $carId")
                            }
                        }.onFailure { e ->
                            Log.e("getInterestedUsers", "Error fetching car for carId: $carId, error: ${e.message}")
                        }
                    }
                }
            }
        }

        // Caso para el Vendedor
        if (direction == "received" && ownerId != null) {
            Log.d("getInterestedUsers", "Direction: received - Checking chats for ownerId (seller): $ownerId")

            // Obtiene todos los coches del vendedor (usando `ownerId` como `sellerId`)
            val carsSnapshot = database.reference.child("ChatGroups").child(ownerId).get().await()
            Log.d("getInterestedUsers", "Total cars for sellerId $ownerId: ${carsSnapshot.childrenCount}")

            carsSnapshot.children.forEach { carSnapshot ->
                val carId = carSnapshot.key
                Log.d("getInterestedUsers", "Checking carId: $carId for sellerId: $ownerId")

                val messagesSnapshot = carSnapshot.child("messages")

                messagesSnapshot.children.forEach { userSnapshot ->
                    val userWithMessage = userSnapshot.key
                    Log.d("getInterestedUsers", "User with message: $userWithMessage in carId: $carId")

                    val lastMessageSnapshot = userSnapshot.children.lastOrNull()
                    val lastMessage = lastMessageSnapshot?.child("content")?.value as? String ?: ""
                    val lastMessageTimestamp = lastMessageSnapshot?.child("timestamp")?.value as? Long ?: 0L

                    Log.d("getInterestedUsers", "Last message from user $userWithMessage: $lastMessage at $lastMessageTimestamp")

                    // Obtiene información del usuario y añade a la lista
                    val userEntityResult = database.reference.child("Users").child(userWithMessage ?: "").get().await()
                        .getValue(UserEntity::class.java)?.apply {
                            if (userWithMessage != null) {
                                id = userWithMessage
                            }
                        }

                    if (userEntityResult != null) {
                        Log.d("getInterestedUsers", "User found: ${userEntityResult.id}")
                        resultList.add(UserWithLastMessage(userEntityResult, lastMessage, lastMessageTimestamp, carId.toString()))
                    } else {
                        Log.d("getInterestedUsers", "User not found for userId: $userWithMessage")
                    }
                }
            }
        }

        Log.d("getInterestedUsers", "Total items found: ${resultList.size}")
        return resultList
    }


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

                // Usa una consulta a MediaStore en Android 10+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
                    val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
                    val selectionArgs = arrayOf(file.fileName)

                    contentResolver.query(uri, projection, selection, selectionArgs, null).use { cursor ->
                        if (cursor != null && cursor.moveToFirst()) {
                            fileExists = true
                            Log.d("FileCleanup", "Archivo encontrado en MediaStore: ${file.fileName}")
                        }
                    }
                } else {
                    // Verificación directa en almacenamiento para versiones antiguas
                    fileExists = File(uri.path ?: "").exists()
                    Log.d("FileCleanup", "Verificación directa: ${uri.path}")
                }

                if (!fileExists) {
                    // Si el archivo no se encuentra, elimínalo de la base de datos
                    fileDao.deleteFileByHash(file.fileHash)
                    Log.d("FileCleanup", "Eliminado de la base de datos porque no se encontró el archivo: ${file.fileName}")
                } else {
                    Log.d("FileCleanup", "Archivo válido en almacenamiento y base de datos: ${file.fileName}")
                }
            }
        }
    }
}
