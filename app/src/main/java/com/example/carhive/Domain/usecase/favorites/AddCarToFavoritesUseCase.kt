package com.example.carhive.Domain.usecase.favorites

import com.example.carhive.data.repository.AuthRepository

class AddCarToFavoritesUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userId: String, userName: String, carId: String, carModel: String, carOwner: String): Result<Unit>{
        return repository.addCarToFavorites(userId, userName, carId, carModel, carOwner)
    }
}