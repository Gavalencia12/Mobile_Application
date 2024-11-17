package com.example.carhive.Domain.usecase.database

import com.example.carhive.data.repository.AuthRepository
import com.example.carhive.Domain.model.User

class SaveUserToDatabaseUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(userID: String, user: User): Result<Unit> {
        return repository.saveUserToDatabase(userID, user)
    }
}