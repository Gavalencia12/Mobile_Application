package com.example.carhive.Domain.usecase.notifications

import com.example.carhive.data.repository.NotificationsRepository

class ListenForNewFavoritesUseCase(private val repository: NotificationsRepository) {
    suspend operator fun invoke(carId: String, onFavoriteAdded: (String, String, String) -> Unit){
        return repository.listenForNewFavorites(carId, onFavoriteAdded)
    }
}