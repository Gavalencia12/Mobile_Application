package com.example.carhive.data.model

data class RatingSellerEntity(
    var sellerId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val date: Long = 0L,
    val userId: String = ""
)