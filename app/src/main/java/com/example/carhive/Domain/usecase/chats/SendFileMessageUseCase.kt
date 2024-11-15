package com.example.carhive.Domain.usecase.chats

import android.net.Uri
import com.example.carhive.data.repository.ChatRepository

class SendFileMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(
        ownerId: String,
        carId: String,
        buyerId: String,
        fileUri: Uri,
        fileType: String,
        fileName: String,
        fileHash: String,
        receiver: String,
        deletedFor: List<String> // Agregamos `deletedFor` como parámetro
    ): Result<Unit> {
        return repository.sendFileMessage(ownerId, carId, buyerId, fileUri, fileType, fileName, fileHash, receiver, deletedFor)
    }
}