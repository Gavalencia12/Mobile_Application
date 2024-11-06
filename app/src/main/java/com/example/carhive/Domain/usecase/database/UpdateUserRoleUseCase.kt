package com.example.carhive.Domain.usecase.database

import com.example.carhive.data.repository.AuthRepository

class UpdateUserRoleUseCase (private val repository: AuthRepository) {
    suspend operator fun invoke(userId: String, newRole: Int): Result<Unit>{
        return repository.updateUserRole(userId, newRole)
    }
}