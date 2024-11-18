package com.example.carhive.Domain.usecase.favorites

import com.example.carhive.data.model.FavoriteCar
import com.example.carhive.data.repository.AuthRepository

class GetUserFavoritesUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userId: String): Result<List<FavoriteCar>>{
        return repository.getUserFavorites(userId)
    }
}