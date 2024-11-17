package com.example.carhive.data.model

data class FavoriteCar(
    val carModel: String = "",    // El modelo del coche que se ha marcado como favorito
    val carOwner: String = "",    // El dueño del coche
    val addedAt: Long = 0L        // Marca de tiempo de cuándo el coche fue marcado como favorito
)
