package com.example.carhive.Domain.usecase.database

import com.example.carhive.data.model.UserEntity
import com.example.carhive.data.repository.AuthRepository

class GetUserDataUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userId:String) : Result<List<UserEntity>>{
        return repository.getUserData(userId)
    }
}