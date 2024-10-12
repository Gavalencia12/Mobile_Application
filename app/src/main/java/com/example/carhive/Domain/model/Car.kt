package com.example.carhive.Domain.model

data class Car(
    var id: String = "",
    var modelo: String = "",
    var color: String = "",
    var speed: String = "",
    var addOn: String = "",
    var description: String = "",
    var price: String = "",
    var imageUrls: List<String>? = mutableListOf()
)