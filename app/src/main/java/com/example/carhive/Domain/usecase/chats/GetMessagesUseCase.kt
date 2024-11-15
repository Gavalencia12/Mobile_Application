package com.example.carhive.Domain.usecase.chats

import com.example.carhive.Domain.model.Message
import com.example.carhive.data.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetMessagesUseCase(private val repository : ChatRepository) {
    operator fun invoke(ownerId: String, carId: String, buyerId: String): Flow<Message> {
        return repository.getMessages(ownerId, carId, buyerId)
    }
}