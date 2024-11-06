package com.example.carhive.Domain.usecase.chats

import com.example.carhive.data.model.CarEntity
import com.example.carhive.data.repository.ChatRepository

class GetUserInfoUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(userId: String, carId: String): Result<CarEntity?>{
        return repository.getUserInfo(userId, carId)
    }
}