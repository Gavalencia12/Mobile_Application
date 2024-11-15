package com.example.carhive.Domain.usecase.user

import com.example.carhive.data.repository.UserRepository
import com.example.carhive.Domain.model.User

class SaveUserPreferencesUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(user: User): Result<Unit> {
        return repository.saveUser(user)
    }
}
