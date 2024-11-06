package com.example.carhive.Domain.usecase.user

import com.example.carhive.data.repository.UserRepository

class GetPasswordUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): Result<String?> {
        return repository.getPassword()
    }
}
