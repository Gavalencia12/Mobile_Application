package com.example.carhive.Domain.usecase.auth

import com.example.carhive.Data.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<String> {
        return repository.registerUser(email, password)
    }
}