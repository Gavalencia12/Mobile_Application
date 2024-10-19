package com.example.carhive.Domain.usecase.auth

import com.example.carhive.Data.repository.AuthRepository

class IsVerifiedTheEmailUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke():Result<Unit>{
        return repository.isVerifiedTheEmail()
    }
}