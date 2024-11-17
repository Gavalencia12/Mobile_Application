package com.example.carhive.Domain.usecase.chats

import com.example.carhive.data.repository.ChatRepository

class DeleteMessageForUserUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(ownerId: String, carId: String, buyerId: String, messageId: String, userId: String){
        return repository.deleteMessageForUser(ownerId, carId, buyerId, messageId, userId)
    }
}