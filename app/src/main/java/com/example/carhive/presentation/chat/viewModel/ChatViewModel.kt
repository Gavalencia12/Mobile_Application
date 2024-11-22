package com.example.carhive.presentation.chat.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import com.example.carhive.Domain.model.Message
import com.example.carhive.Domain.usecase.chats.*
import com.example.carhive.Domain.usecase.database.GetUserDataUseCase
import com.example.carhive.data.model.CarEntity
import com.example.carhive.data.model.UserEntity
import com.example.carhive.data.model.HistoryEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val sendFileMessageUseCase: SendFileMessageUseCase,
    private val getUserDataUseCase: GetUserDataUseCase,
    private val infoUseCase: GetUserInfoUseCase,
    private val deleteMessageForUserUseCase: DeleteMessageForUserUseCase,
    private val updateMessageStatusUseCase: UpdateMessageStatusUseCase,
    private val getAllMessagesOnceUseCase: GetAllMessagesOnceUseCase,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> get() = _messages

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _userData = MutableLiveData<UserEntity>()
    val userData: LiveData<UserEntity> get() = _userData

    private val _buyerData = MutableLiveData<UserEntity>()
    val buyerData: LiveData<UserEntity> get() = _buyerData

    private val _carDetails = MutableLiveData<CarEntity?>()
    val carDetails: LiveData<CarEntity?> get() = _carDetails

    private val _isUserBlocked = MutableLiveData<Boolean>()
    val isUserBlocked: LiveData<Boolean> get() = _isUserBlocked

    private val _uploading = MutableLiveData<Boolean>()
    val uploading: LiveData<Boolean> get() = _uploading

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    private val historyRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("History/userHistory")

    private fun logHistoryEvent(userId: String, eventType: String, message: String) {
        val history = HistoryEntity(
            userId = userId,
            timestamp = System.currentTimeMillis(),
            eventType = eventType,
            message = message
        )

        viewModelScope.launch {
            try {
                val newHistoryRef = historyRef.push()
                newHistoryRef.setValue(history).await()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error logging history event: ${e.message}")
            }
        }
    }

    fun createChat(ownerId: String, buyerId: String, carId: String) {
        viewModelScope.launch {
            try {
                // Lógica para crear un chat
                logHistoryEvent(
                    userId = currentUserId,
                    eventType = "Chat Created",
                    message = "Chat creado entre $ownerId y $buyerId para el auto $carId"
                )
            } catch (e: Exception) {
                _error.value = "Error creando el chat: ${e.message}"
            }
        }
    }

    // Registro al eliminar un chat
    fun deleteChat(ownerId: String, buyerId: String, carId: String) {
        viewModelScope.launch {
            try {
                // Lógica para eliminar un chat
                logHistoryEvent(
                    userId = currentUserId,
                    eventType = "Chat Deleted",
                    message = "Chat eliminado entre $ownerId y $buyerId para el auto $carId"
                )
            } catch (e: Exception) {
                _error.value = "Error eliminando el chat: ${e.message}"
            }
        }
    }

    // Registro al reportar un usuario

    /**
     * Observes chat messages and updates the messages list. Automatically updates the message
     * status to "read" if the current user is the message receiver.
     */
    fun observeMessages(ownerId: String, carId: String, buyerId: String, admin: Boolean) {
        viewModelScope.launch {
            getMessagesUseCase(ownerId, carId, buyerId).collectLatest { message ->
                if (!message.deletedFor.contains(currentUserId)) {
                    if (message.receiverId == currentUserId && message.status == "sent") {
                        updateMessageStatus(ownerId, carId, buyerId, message.messageId, "read")
                        message.status = "read"
                    } else if (message.receiverId == "TechnicalSupport" && admin){
                        updateMessageStatus(ownerId, carId, buyerId, message.messageId, "read")
                        message.status = "read"
                    }

                    // Actualiza la lista local de mensajes
                    _messages.value = _messages.value.map {
                        if (it.messageId == message.messageId) message else it
                    }.toMutableList().apply {
                        if (none { it.messageId == message.messageId }) add(message)
                    }
                }
            }
        }
    }

    fun sendTextMessageWithNotification(ownerId: String, carId: String, buyerId: String, content: String, admin: Boolean) {
        val isSeller = currentUserId == ownerId
        val receiverId = if (isSeller) buyerId else ownerId

        viewModelScope.launch {
            try {
                // Verifica cuántos mensajes hay en el chat
                val messagesRef = FirebaseDatabase.getInstance()
                    .getReference("ChatGroups")
                    .child(ownerId)
                    .child(carId)
                    .child("messages")
                    .child(buyerId)
                    .get()
                    .await()

                val messageCount = messagesRef.childrenCount
                Log.d("angel", "$messageCount")

                // Envía el mensaje
                sendTextMessage(ownerId, carId, buyerId, content, admin)

                // Solo envía la notificación si es el primer mensaje
                if (messageCount == 0L) {
                    sendNotificationToOwner(receiverId, carId)
                }
            } catch (e: Exception) {
                _error.value = "Error sending message: ${e.message}"
            }
        }
    }

    fun sendFileMessageWithNotification(ownerId: String, carId: String, buyerId: String, fileUri: Uri, fileType: String, fileName: String, fileHash: String, admin: Boolean) {
        val isSeller = currentUserId == ownerId
        val receiverId = if (isSeller) buyerId else ownerId

        viewModelScope.launch {
            try {
                // Verifica cuántos mensajes hay en el chat
                val messagesRef = FirebaseDatabase.getInstance()
                    .getReference("ChatGroups")
                    .child(ownerId)
                    .child(carId)
                    .child("messages")
                    .child(buyerId)
                    .get()
                    .await()

                val messageCount = messagesRef.childrenCount
                Log.d("angel", "$messageCount")

                // Envía el mensaje
                sendFileMessage(ownerId, carId, buyerId, fileUri, fileType, fileName, fileHash, admin)

                // Solo envía la notificación si es el primer mensaje
                if (messageCount == 0L) {
                    sendNotificationToOwner(receiverId, carId)
                }
            } catch (e: Exception) {
                _error.value = "Error sending message: ${e.message}"
            }
        }
    }

    /**
     * Sends a text message if the user is not blocked by the receiver. If the sender is blocked,
     * the receiver's ID is added to the `deletedFor` list.
     */
    fun sendTextMessage(ownerId: String, carId: String, buyerId: String, content: String, admin: Boolean) {
        val isSeller = currentUserId == ownerId
        val receiver = if (isSeller) buyerId else if (ownerId=="TechnicalSupport") if (!admin) "TechnicalSupport" else buyerId else ownerId
        var sender = currentUserId
        if (admin) sender = "TechnicalSupport"

        viewModelScope.launch {
            isUserBlocked(receiver, currentUserId, carId) { isBlocked ->
                val message = Message(
                    messageId = "",
                    senderId = sender,
                    receiverId = receiver,
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    status = "sent",
                    carId = carId,
                    deletedFor = mutableListOf()
                )

                if (isBlocked) {
                    message.deletedFor.add(receiver)
                }

                viewModelScope.launch {
                    val result = sendMessageUseCase(ownerId, carId, buyerId, message)
                    if (result.isFailure) {
                        _error.value = result.exceptionOrNull()?.message
                        updateMessageStatus(ownerId, carId, buyerId, message.messageId, "failed")
                    }
                }
            }
        }
    }

    private fun sendNotificationToOwner(receiverId: String, carId: String) {
        val notificationRef = FirebaseDatabase.getInstance().getReference("Notifications/$receiverId").push()

        val notification = mapOf(
            "id" to notificationRef.key,
            "title" to "New Message",
            "message" to "You have a new message in your chat for car ID: $carId",
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false
        )

        notificationRef.setValue(notification).addOnSuccessListener {
            Log.d("ChatViewModel", "Notification sent to $receiverId")
        }.addOnFailureListener { e ->
            Log.e("ChatViewModel", "Error sending notification: ${e.message}")
        }
    }

    /**
     * Sends a file message with the specified metadata and updates the status in case of failure.
     */
    fun sendFileMessage(
        ownerId: String,
        carId: String,
        buyerId: String,
        fileUri: Uri,
        fileType: String,
        fileName: String,
        fileHash: String,
        admin: Boolean
    ) {
        viewModelScope.launch {
            _uploading.value = true
            val isSeller = currentUserId == ownerId
            val receiver = if (isSeller) buyerId else if (ownerId == "TechnicalSupport")
                if (!admin) "TechnicalSupport" else buyerId
            else ownerId

            isUserBlocked(receiver, currentUserId, carId) { isBlocked ->
                val deletedFor = mutableListOf<String>()
                if (isBlocked) {
                    deletedFor.add(receiver)
                }

                // Crear el objeto Message
                val message = Message(
                    messageId = "",
                    senderId = if (admin) "TechnicalSupport" else currentUserId,
                    receiverId = receiver,
                    fileUrl = fileUri.toString(), // Inicialmente, el URI del archivo sin la URL descargable
                    fileType = fileType,
                    fileName = fileName,
                    hash = fileHash,
                    timestamp = System.currentTimeMillis(),
                    status = "sent",
                    carId = carId,
                    deletedFor = deletedFor
                )

                // Llamar al caso de uso para enviar el archivo
                viewModelScope.launch {
                    val result = sendFileMessageUseCase(ownerId, carId, buyerId, message)
                    _uploading.value = false
                    if (result.isFailure) {
                        _error.value = result.exceptionOrNull()?.message
                    }
                }
            }
        }
    }

    /**
     * Updates the status of a message, such as marking it as "read" or "failed".
     */
    private fun updateMessageStatus(ownerId: String, carId: String, buyerId: String, messageId: String, status: String) {
        viewModelScope.launch {
            try {
                updateMessageStatusUseCase(ownerId, carId, buyerId, messageId, status)
            } catch (e: Exception) {
                _error.value = "Error updating message status: ${e.message}"
            }
        }
    }

    /**
     * Clears the chat for the current user by marking messages as deleted. Reloads messages after clearing.
     */
    fun clearChatForUser(ownerId: String, carId: String, buyerId: String) {
        viewModelScope.launch {
            try {
                val messages = getAllMessagesOnceUseCase(ownerId, carId, buyerId)

                messages.forEach { message ->
                    deleteMessageForUserUseCase(
                        ownerId,
                        carId,
                        buyerId,
                        message.messageId,
                        currentUserId
                    )
                }
                _messages.value = emptyList()

            } catch (e: Exception) {
                _error.value = "Error clean chat: ${e.message}"
            }
        }
    }

    /**
     * Reports a user by adding a record in Firebase with sample messages.
     */
    fun reportUser(
        currentUserId: String,
        ownerId: String,
        buyerId: String,
        carId: String,
        comment: String?
    ) {
        val isSeller = currentUserId == ownerId
        val receiver = if (isSeller) buyerId else ownerId
        viewModelScope.launch {
            try {
                val reportRef = FirebaseDatabase.getInstance().getReference("Reports")
                    .child("UserReports").push()

                val allMessages = getAllMessagesOnceUseCase(ownerId, carId, buyerId)

                val sampleMessages = allMessages.takeLast(5)

                val reportData = mapOf(
                    "reporterId" to currentUserId,
                    "reportedUserId" to receiver,
                    "carId" to carId,
                    "ownerId" to ownerId,
                    "timestamp" to System.currentTimeMillis(),
                    "sampleMessages" to sampleMessages,
                    "revised" to false,
                    "comment" to comment
                )

                reportRef.setValue(reportData)
            } catch (e: Exception) {
                _error.value = "Error reporting user: ${e.message}"
            }
        }
    }

    /**
     * Blocks a user by adding them to the blocked list in Firebase.
     */
    fun blockUser(currentUserId: String, ownerId: String, buyerId: String, carId: String) {
        val isSeller = currentUserId == ownerId
        val blockedUserId = if (isSeller) buyerId else ownerId
        viewModelScope.launch {
            try {
                val blockRef = FirebaseDatabase.getInstance().getReference("BlockedUsers")
                    .child(currentUserId).child(blockedUserId)
                val blockData = mapOf(
                    "blockedUserId" to blockedUserId,
                    "carId" to carId,
                    "timestamp" to System.currentTimeMillis()
                )
                blockRef.setValue(blockData).await()
                setUserBlocked(true)
            } catch (e: Exception) {
                _error.value = "Error blocking user: ${e.message}"
            }
        }
    }

    /**
     * Unblocks a user by removing them from the blocked list in Firebase.
     */
    fun unblockUser(currentUserId: String, blockedUserId: String) {
        viewModelScope.launch {
            try {
                val blockRef = FirebaseDatabase.getInstance().getReference("BlockedUsers")
                    .child(currentUserId).child(blockedUserId)
                blockRef.removeValue().await()
                setUserBlocked(false)
            } catch (e: Exception) {
                _error.value = "Error unblocking user: ${e.message}"
            }
        }
    }

    /**
     * Updates the live data indicating whether the user is blocked.
     */
    fun setUserBlocked(isBlocked: Boolean) {
        _isUserBlocked.postValue(isBlocked)
    }

    /**
     * Checks if a user is blocked by querying the Firebase database and invokes the provided callback.
     */
    fun isUserBlocked(
        currentUserId: String,
        otherUserId: String,
        carId: String,
        callback: (Boolean) -> Unit
    ) {
        val blockRef = FirebaseDatabase.getInstance().getReference("BlockedUsers")
            .child(currentUserId).child(otherUserId)

        blockRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val blockedCarId = snapshot.child("carId").getValue(String::class.java)
                // Verifica si el carId en la base de datos coincide con el carId proporcionado
                callback(blockedCarId == carId)
            } else {
                // Si el nodo de usuario bloqueado no existe, significa que el usuario no está bloqueado
                callback(false)
            }
        }.addOnFailureListener {
            // Manejo de errores, en caso de fallo en la consulta
            callback(false)
        }
    }

    /**
     * Loads user and car information for the chat header based on the roles of the current user.
     */
    fun loadInfoHead(ownerId: String, carId: String, buyerId: String) {
        viewModelScope.launch {
            try {
                val isSeller = currentUserId == ownerId

                if (ownerId == "TechnicalSupport") {
                    // Load data from Users using buyerId
                    val userRef = FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(buyerId)
                        .get()
                        .await()

                    // Convert the data to UserEntity
                    val userEntity = userRef.getValue(UserEntity::class.java)

                    if (userEntity != null) {
                        _buyerData.value = userEntity?:return@launch
                    } else {
                        _error.value = "Failed to load buyer data"
                    }

                    // Load car details (if applicable)
                    val carResult = infoUseCase(ownerId, carId)
                    if (carResult.isSuccess) {
                        _carDetails.value = carResult.getOrNull()
                    }
                } else {
                    // Logic for other cases
                    if (isSeller) {
                        val buyerResult = getUserDataUseCase(buyerId)
                        if (buyerResult.isSuccess) {
                            _buyerData.value = buyerResult.getOrNull()?.firstOrNull()
                        }
                    } else {
                        val userResult = getUserDataUseCase(ownerId)
                        if (userResult.isSuccess) {
                            _userData.value = userResult.getOrNull()?.firstOrNull()
                        }
                    }

                    // Load car details
                    val carResult = infoUseCase(ownerId, carId)
                    if (carResult.isSuccess) {
                        _carDetails.value = carResult.getOrNull()
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error loading user or car data: ${e.message}"
            }
        }
    }

    /**
     * Deletes all messages from the chat in the Firebase Realtime Database.
     *
     * @param directory The ID of the car related to the chat.
     * @param buyerId The ID of the buyer in the chat.
     */
    fun deleteAllMessages(directory: String, buyerId: String) {
        viewModelScope.launch {
        val messagesRef = FirebaseDatabase.getInstance()
            .getReference("ChatGroups")
            .child("TechnicalSupport")
            .child(directory) // Use the dynamic carId (buyer or seller)
            .child("messages")
            .child(buyerId)

        messagesRef.removeValue().await()
        }
    }

    /**
     * Determines whether the user ID is located under the "buyer" or "seller" node in the database.
     *
     * @param userId The ID of the user to search for.
     * @param onResult Callback that returns "buyer" or "seller" depending on the location, or null if not found.
     */
    fun findUserNode(userId: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val database = FirebaseDatabase.getInstance().reference.child("ChatGroups").child("TechnicalSupport")

                // Check if the user exists under the "buyer" node
                val buyerSnapshot = database.child("buyer").child("messages").child(userId).get().await()
                if (buyerSnapshot.exists()) {
                    onResult("buyer")
                    return@launch
                }

                // Check if the user exists under the "seller" node
                val sellerSnapshot = database.child("seller").child("messages").child(userId).get().await()
                if (sellerSnapshot.exists()) {
                    onResult("seller")
                    return@launch
                }

                // If the user is not found in either node
                onResult(null)
            } catch (e: Exception) {
                onResult(null) // Return null if there's an error
            }
        }
    }

}
