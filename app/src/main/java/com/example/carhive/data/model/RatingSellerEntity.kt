package com.example.carhive.data.model

data class RatingSellerEntity(
    val calificacion: Int,
    val comentario: String,
    val hora: Long,
    val usuarioId: String
)