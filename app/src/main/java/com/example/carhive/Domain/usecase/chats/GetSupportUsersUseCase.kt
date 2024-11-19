package com.example.carhive.Domain.usecase.chats

import com.example.carhive.data.model.SupportUserData
import com.example.carhive.data.repository.ChatRepository

class GetSupportUsersUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(ownerId: String): SupportUserData {
        return repository.getSupportUsers(ownerId)
    }
}