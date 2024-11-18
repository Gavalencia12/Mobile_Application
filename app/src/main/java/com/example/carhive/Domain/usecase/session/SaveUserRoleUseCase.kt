package com.example.carhive.Domain.usecase.session

import com.example.carhive.data.repository.SessionRepository

class SaveUserRoleUseCase(private val repository: SessionRepository) {
    suspend operator fun invoke(userRole: Int): Result<Unit> {
        return repository.saveUserRole(userRole)
    }
}