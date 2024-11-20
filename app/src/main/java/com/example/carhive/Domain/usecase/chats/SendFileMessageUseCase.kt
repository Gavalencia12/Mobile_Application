package com.example.carhive.Domain.usecase.chats

import android.net.Uri
import com.example.carhive.Domain.model.Message
import com.example.carhive.data.repository.ChatRepository

class SendFileMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(
        ownerId: String,
        carId: String,
        buyerId: String,
        message: Message
    ): Result<Unit> {
        return repository.sendFileMessage(ownerId, carId, buyerId, message)
    }
}