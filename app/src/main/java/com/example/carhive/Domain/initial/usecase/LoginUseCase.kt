package com.example.carhive.Domain.initial.usecase

import com.example.carhive.Data.initial.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return repository.loginUser(email, password)
    }
}