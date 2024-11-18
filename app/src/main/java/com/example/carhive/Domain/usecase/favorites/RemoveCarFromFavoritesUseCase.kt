package com.example.carhive.Domain.usecase.favorites

import com.example.carhive.data.repository.AuthRepository

class RemoveCarFromFavoritesUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke (userId: String, carId: String): Result<Unit>{
        return repository.removeCarFromFavorites(userId, carId)
    }
}