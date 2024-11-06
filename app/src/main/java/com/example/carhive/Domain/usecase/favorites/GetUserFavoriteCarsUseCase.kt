package com.example.carhive.Domain.usecase.favorites

import com.example.carhive.data.model.CarEntity
import com.example.carhive.data.repository.AuthRepository

class GetUserFavoriteCarsUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userId: String): Result<List<CarEntity>>{
        return repository.getUserFavoriteCars(userId)
    }
}