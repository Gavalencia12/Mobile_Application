package com.example.carhive.Domain.usecase.favorites

import com.example.carhive.Data.model.FavoriteCar
import com.example.carhive.Data.repository.AuthRepository

class GetUserFavoritesUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userId: String): Result<List<FavoriteCar>>{
        return repository.getUserFavorites(userId)
    }
}