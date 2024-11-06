package com.example.carhive.Domain.usecase.user

import com.example.carhive.data.repository.UserRepository
import com.example.carhive.Domain.model.User

class GetUserPreferencesUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): Result<User> {
        return repository.getUser()
    }
}
