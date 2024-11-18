package com.example.carhive.Domain.usecase.chats

import com.example.carhive.data.model.UserEntity
import com.example.carhive.data.model.UserWithLastMessage
import com.example.carhive.data.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetInterestedUsersUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(ownerId: String, carId: String, direction: String, type: String): List<Any> {
        return repository.getInterestedUsers(ownerId, carId, direction, type)
    }
}