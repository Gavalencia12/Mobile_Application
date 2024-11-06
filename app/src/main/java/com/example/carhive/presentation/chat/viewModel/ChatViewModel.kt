package com.example.carhive.presentation.chat.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.model.Message
import com.example.carhive.Domain.usecase.chats.GetMessagesUseCase
import com.example.carhive.Domain.usecase.chats.GetUserInfoUseCase
import com.example.carhive.Domain.usecase.chats.SendFileMessageUseCase
import com.example.carhive.Domain.usecase.chats.SendMessageUseCase
import com.example.carhive.Domain.usecase.database.GetUserDataUseCase
import com.example.carhive.data.model.CarEntity
import com.example.carhive.data.model.UserEntity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val sendFileMessageUseCase: SendFileMessageUseCase,
    private val getUserDataUseCase: GetUserDataUseCase,
    private val infoUseCase: GetUserInfoUseCase
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

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    fun observeMessages(ownerId: String, carId: String, buyerId: String) {
        viewModelScope.launch {
            getMessagesUseCase(ownerId, carId, buyerId).collect { message ->
                _messages.value += message
            }
        }
    }

    fun sendTextMessage(ownerId: String, carId: String, buyerId: String, content: String) {
        val message = Message(
            senderId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            content = content,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            val result = sendMessageUseCase(ownerId, carId, buyerId, message)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun sendFileMessage(ownerId: String, carId: String, buyerId: String, fileUri: Uri, fileType: String, fileName: String, fileHash: String) {
        viewModelScope.launch {
            val result = sendFileMessageUseCase(ownerId, carId, buyerId, fileUri, fileType, fileName, fileHash)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
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
