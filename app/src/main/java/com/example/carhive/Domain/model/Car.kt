package com.example.carhive.Domain.model

data class Car(
    var id: String = "",
    var modelo: String = "", // YA
    var color: String = "", // YES
    var speed: String = "", //YES
    var addOn: String = "", //NO
    var description: String = "", //NO
    var price: String = "", // YES
    var imageUrls: List<String>? = mutableListOf() // YES
)