package com.example.carhive.Domain.usecase.auth

import com.example.carhive.Data.repository.AuthRepository

class GetCurrentUserIdUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke():Result<String?>{
        return repository.getCurrentUserId()
    }
}