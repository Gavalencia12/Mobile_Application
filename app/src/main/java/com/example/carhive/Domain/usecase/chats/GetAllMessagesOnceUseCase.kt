package com.example.carhive.Domain.usecase.chats

import com.example.carhive.Domain.model.Message
import com.example.carhive.data.repository.ChatRepository

class GetAllMessagesOnceUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(ownerId: String, carId: String, buyerId: String): List<Message> {
        return repository.getAllMessagesOnce(ownerId, carId, buyerId)
    }
}