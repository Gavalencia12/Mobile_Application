package com.example.carhive.Domain.usecase.user

import com.example.carhive.data.repository.UserRepository

class SavePasswordUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(password: String): Result<Unit> {
        return repository.savePassword(password)
    }
}
