package com.example.carhive.Domain.usecase.favorites

import com.example.carhive.Data.model.FavoriteUser
import com.example.carhive.Data.repository.AuthRepository

class GetCarFavoriteCountAndUsersUseCase(private val repository: AuthRepository) {

    suspend operator fun invoke (carId: String): Result<Pair<Int, List<FavoriteUser>>>{
        return repository.getCarFavoriteCountAndUsers(carId)
    }

}