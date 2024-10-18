package com.example.carhive.Domain.usecase.database

import com.example.carhive.Data.model.UserEntity
import com.example.carhive.Data.repository.AuthRepository
import com.example.carhive.Domain.model.User

class GetUserDataUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userId:String) : Result<List<UserEntity>>{
        return repository.getUserData(userId)
    }
}