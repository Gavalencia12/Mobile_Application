package com.example.carhive.Domain.initial.usecase

import com.example.carhive.Data.initial.model.UserModel
import com.example.carhive.Data.initial.repository.AuthRepository

class SaveUserToDatabaseUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userID: String, user: UserModel): Result<Unit> {
        return repository.saveUserToDatabase(userID, user)
    }
}