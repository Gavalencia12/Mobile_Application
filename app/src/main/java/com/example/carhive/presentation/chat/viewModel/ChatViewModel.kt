package com.example.carhive.presentation.chat.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.model.Message
import com.example.carhive.Domain.usecase.chats.DeleteMessageForUserUseCase
import com.example.carhive.Domain.usecase.chats.GetMessagesUseCase
import com.example.carhive.Domain.usecase.chats.GetUserInfoUseCase
import com.example.carhive.Domain.usecase.chats.SendFileMessageUseCase
import com.example.carhive.Domain.usecase.chats.SendMessageUseCase
import com.example.carhive.Domain.usecase.chats.UpdateMessageStatusUseCase
import com.example.carhive.Domain.usecase.database.GetUserDataUseCase
import com.example.carhive.data.model.CarEntity
import com.example.carhive.data.model.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun observeMessages(ownerId: String, carId: String, buyerId: String) {
        viewModelScope.launch {
            getMessagesUseCase(ownerId, carId, buyerId).collect { message ->
                // Verifica que el usuario actual no esté en la lista deletedFor del mensaje
                if (!message.deletedFor.contains(currentUserId)) {
                    Log.i("angel", "Mensaje observado")
                    _messages.value += message

                    if (message.receiverId == currentUserId && message.status == "sent") {
                        updateMessageStatus(ownerId, carId, buyerId, message.messageId, "read")
                    }
                }
            }
        }
    }

    fun sendTextMessage(ownerId: String, carId: String, buyerId: String, content: String) {
        val isSeller = currentUserId == ownerId
        val receiver = if (isSeller) buyerId else ownerId

        viewModelScope.launch {
            isUserBlocked(receiver, currentUserId) { isBlocked ->
                val message = Message(
                    messageId = "",
                    senderId = currentUserId,
                    receiverId = receiver,
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    status = "sent",
                    carId = carId,
                    deletedFor = mutableListOf() // Inicializa deletedFor vacío
                )

                if (isBlocked) {
                    // Si está bloqueado, agrega el ID del receptor a deletedFor
                    message.deletedFor.add(receiver)
                }

                // Luego intenta enviar el mensaje dentro de la coroutine
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

    fun sendFileMessage(ownerId: String, carId: String, buyerId: String, fileUri: Uri, fileType: String, fileName: String, fileHash: String) {
        viewModelScope.launch {
            val isSeller = currentUserId == ownerId
            val receiver = if (isSeller) buyerId else ownerId

            isUserBlocked(receiver, currentUserId) { isBlocked ->
                // Inicializa la lista `deletedFor`
                val deletedFor = mutableListOf<String>()

                // Si el receptor bloqueó al remitente, añade el ID a `deletedFor`
                if (isBlocked) {
                    deletedFor.add(receiver)
                }

                // Llama al caso de uso para enviar el archivo con la lista `deletedFor`
                viewModelScope.launch {
                    val result = sendFileMessageUseCase(ownerId, carId, buyerId, fileUri, fileType, fileName, fileHash, receiver, deletedFor)
                    if (result.isFailure) {
                        _error.value = result.exceptionOrNull()?.message
                    }
                }
            }
        }
    }

    // Llama a la función para cambiar el estado del mensaje
    private fun updateMessageStatus(ownerId: String, carId: String, buyerId: String, messageId: String, status: String) {
        viewModelScope.launch {
            updateMessageStatusUseCase(ownerId, carId, buyerId, messageId, status)
        }
    }

    fun clearChatForUser(ownerId: String, carId: String, buyerId: String) {
        viewModelScope.launch {
            try {
                Log.d("clearChatForUser", "Iniciando el vaciado del chat para el usuario: $currentUserId, $ownerId, $carId, $buyerId")

                // Vacia temporalmente la lista local
                _messages.value = emptyList()

                // Marca los mensajes como eliminados para el usuario actual
                getMessagesUseCase(ownerId, carId, buyerId).collect { message ->
                    deleteMessageForUserUseCase(
                        ownerId, carId, buyerId, message.messageId, currentUserId
                    )
                }

                // Recarga los mensajes filtrados por el usuario actual
                reloadMessages(ownerId, carId, buyerId)

            } catch (e: Exception) {
                Log.e("clearChatForUser", "Error al vaciar el chat: ${e.message}", e)
                _error.value = "Error al vaciar el chat: ${e.message}"
            }
        }
    }

    private fun reloadMessages(ownerId: String, carId: String, buyerId: String) {
        viewModelScope.launch {
            getMessagesUseCase(ownerId, carId, buyerId).collect { message ->
                if (!message.deletedFor.contains(currentUserId)) {
                    _messages.value += message
                }
            }
        }
    }

    fun reportUser(currentUserId: String, ownerId: String, buyerId: String, carId: String) {
        viewModelScope.launch {
            try {
                // Enviar un reporte en el nodo "Reports" de Firebase
                val reportRef = FirebaseDatabase.getInstance().getReference("Reports")
                    .child("UserReports").push()

                val reportData = mapOf(
                    "reporterId" to currentUserId,
                    "reportedUserId" to buyerId,
                    "carId" to carId,
                    "ownerId" to ownerId,
                    "timestamp" to System.currentTimeMillis(),
                    "sampleMessages" to _messages.value.takeLast(5) // Adjuntar últimos 5 mensajes
                )

                reportRef.setValue(reportData)
            } catch (e: Exception) {
                _error.value = "Error al reportar usuario: ${e.message}"
            }
        }
    }

    fun blockUser(currentUserId: String, ownerId: String, buyerId: String, carId: String) {
        viewModelScope.launch {
            try {
                val blockRef = FirebaseDatabase.getInstance().getReference("BlockedUsers")
                    .child(currentUserId).child(buyerId)
                val blockData = mapOf(
                    "blockedUserId" to buyerId,
                    "carId" to carId,
                    "timestamp" to System.currentTimeMillis()
                )
                blockRef.setValue(blockData).await()

                // Utiliza setUserBlocked para actualizar el estado de bloqueo
                setUserBlocked(true)

            } catch (e: Exception) {
                _error.value = "Error al bloquear usuario: ${e.message}"
            }
        }
    }

    fun unblockUser(currentUserId: String, blockedUserId: String) {
        viewModelScope.launch {
            try {
                val blockRef = FirebaseDatabase.getInstance().getReference("BlockedUsers")
                    .child(currentUserId).child(blockedUserId)
                blockRef.removeValue().await()

                // Utiliza setUserBlocked para actualizar el estado de bloqueo
                setUserBlocked(false)
            } catch (e: Exception) {
                _error.value = "Error al desbloquear usuario: ${e.message}"
            }
        }
    }

    fun setUserBlocked(isBlocked: Boolean) {
        _isUserBlocked.postValue(isBlocked)
    }

    fun isUserBlocked(currentUserId: String, otherUserId: String, callback: (Boolean) -> Unit) {
        val blockRef = FirebaseDatabase.getInstance().getReference("BlockedUsers")
            .child(currentUserId).child(otherUserId)
        blockRef.get().addOnSuccessListener { snapshot ->
            callback(snapshot.exists())
        }
    }

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
