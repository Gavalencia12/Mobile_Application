package com.example.carhive.Domain.usecase.session

import com.example.carhive.data.repository.SessionRepository

class IsUserAuthenticatedUseCase(private val repository: SessionRepository
) {
    suspend operator fun invoke(): Result<String?> {
        return repository.isUserAuthenticated()
    }
}