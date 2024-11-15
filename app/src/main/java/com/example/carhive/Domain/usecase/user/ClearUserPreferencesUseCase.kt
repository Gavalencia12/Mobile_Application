package com.example.carhive.Domain.usecase.user

import com.example.carhive.data.repository.UserRepository

class ClearUserPreferencesUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.clear()
    }
}
