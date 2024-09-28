package com.example.carhive.Domain.initial.usecase

import com.example.carhive.Data.initial.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<String> {
        return repository.registerUser(email, password)
    }
}