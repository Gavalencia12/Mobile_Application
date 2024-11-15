package com.example.carhive.data.model

data class FavoriteUser(
    val userName: String = "",    // El nombre del usuario que ha marcado el coche como favorito
    val addedAt: Long = 0L        // Marca de tiempo de cuándo el usuario marcó el coche como favorito
)
