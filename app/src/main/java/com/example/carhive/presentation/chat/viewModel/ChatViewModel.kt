package com.example.carhive.presentation.chat.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.model.Message
import com.example.carhive.Domain.usecase.chats.*
import com.example.carhive.Domain.usecase.database.GetUserDataUseCase
import com.example.carhive.data.model.CarEntity
import com.example.carhive.data.model.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    private val getAllMessagesOnceUseCase: GetAllMessagesOnceUseCase
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

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    /**
     * Observes chat messages and updates the messages list. Automatically updates the message
     * status to "read" if the current user is the message receiver.
     */
    fun observeMessages(ownerId: String, carId: String, buyerId: String) {
        viewModelScope.launch {
            getMessagesUseCase(ownerId, carId, buyerId).collectLatest { message ->
                if (!message.deletedFor.contains(currentUserId)) {
                    // Reemplaza el mensaje en la lista si ya existe, o agrégalo si no está
                    _messages.value = _messages.value.map {
                        if (it.messageId == message.messageId) message else it
                    }.toMutableList().apply {
                        if (none { it.messageId == message.messageId }) add(message)
                    }

                    // Actualiza el estado del mensaje a "read" si es necesario
                    if (message.receiverId == currentUserId && message.status == "sent") {
                        updateMessageStatus(ownerId, carId, buyerId, message.messageId, "read")
                    }
                }
            }
        }
    }

    /**
     * Sends a text message if the user is not blocked by the receiver. If the sender is blocked,
     * the receiver's ID is added to the `deletedFor` list.
     */
    fun sendTextMessage(ownerId: String, carId: String, buyerId: String, content: String) {
        val isSeller = currentUserId == ownerId
        val receiver = if (isSeller) buyerId else ownerId

        viewModelScope.launch {
            isUserBlocked(receiver, currentUserId, carId) { isBlocked ->
                val message = Message(
                    messageId = "",
                    senderId = currentUserId,
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

    /**
     * Sends a file message with the specified metadata and updates the status in case of failure.
     */
    fun sendFileMessage(ownerId: String, carId: String, buyerId: String, fileUri: Uri, fileType: String, fileName: String, fileHash: String) {
        viewModelScope.launch {
            val isSeller = currentUserId == ownerId
            val receiver = if (isSeller) buyerId else ownerId

            isUserBlocked(receiver, currentUserId, carId) { isBlocked ->
                val deletedFor = mutableListOf<String>()
                if (isBlocked) {
                    deletedFor.add(receiver)
                }

                viewModelScope.launch {
                    val result = sendFileMessageUseCase(ownerId, carId, buyerId, fileUri, fileType, fileName, fileHash, receiver, deletedFor)
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
            updateMessageStatusUseCase(ownerId, carId, buyerId, messageId, status)
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
                    deleteMessageForUserUseCase(ownerId, carId, buyerId, message.messageId, currentUserId)
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
    fun reportUser(currentUserId: String, ownerId: String, buyerId: String, carId: String, comment: String?) {
        viewModelScope.launch {
            try {
                Log.d("reportUser", "Iniciando el proceso de reporte")

                // Obtenemos la referencia de Firebase para los reportes
                val reportRef = FirebaseDatabase.getInstance().getReference("Reports")
                    .child("UserReports").push()
                Log.d("reportUser", "Referencia de reporte obtenida: $reportRef")

                // Usamos getAllMessagesOnceUseCase para obtener todos los mensajes de una vez
                val allMessages = getAllMessagesOnceUseCase(ownerId, carId, buyerId)
                Log.d("reportUser", "Total de mensajes obtenidos: ${allMessages.size}")

                // Tomamos los últimos 5 mensajes
                val sampleMessages = allMessages.takeLast(5)
                Log.d("reportUser", "Últimos 5 mensajes obtenidos para el reporte: $sampleMessages")

                // Datos del reporte
                val reportData = mapOf(
                    "reporterId" to currentUserId,
                    "reportedUserId" to buyerId,
                    "carId" to carId,
                    "ownerId" to ownerId,
                    "timestamp" to System.currentTimeMillis(),
                    "sampleMessages" to sampleMessages, // Adjuntamos los últimos 5 mensajes
                    "revised" to false,
                    "comment" to comment
                )

                // Mostramos los datos que vamos a enviar a Firebase
                Log.d("reportUser", "Datos del reporte que se enviarán: $reportData")

                // Enviamos el reporte a Firebase
                reportRef.setValue(reportData)
                    .addOnSuccessListener {
                        Log.d("reportUser", "Reporte enviado exitosamente")
                    }
                    .addOnFailureListener { e ->
                        Log.e("reportUser", "Error al enviar el reporte: ${e.message}")
                    }
            } catch (e: Exception) {
                _error.value = "Error reporting user: ${e.message}"
                Log.e("reportUser", "Excepción durante el reporte: ${e.message}")
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
    fun isUserBlocked(currentUserId: String, otherUserId: String, carId: String, callback: (Boolean) -> Unit) {
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
            val isSeller = currentUserId == ownerId

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

            val carResult = infoUseCase(ownerId, carId)
            if (carResult.isSuccess) {
                _carDetails.value = carResult.getOrNull()
            }
        }
    }
}
