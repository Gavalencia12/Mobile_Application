package com.example.carhive.Data.model

data class CarEntity (
    var id : String = "",
    var modelo: String = "",
    var color: String = "",
    var speed: String = "",
    var addOn: String = "",
    var description: String = "",
    var price: String = "",
    var imageUrls: List<String>? = mutableListOf()
)