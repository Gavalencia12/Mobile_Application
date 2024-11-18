package com.example.carhive.Domain.usecase.chats

import com.example.carhive.data.repository.ChatRepository

class UpdateMessageStatusUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(ownerId: String, carId: String, buyerId: String, messageId: String, status: String){
        return repository.updateMessageStatus(ownerId, carId, buyerId, messageId, status)
    }
}