package com.example.carhive.Data.admin.models

data class Car(
    var id: String = "",
    var modelo: String = "",
    var color: String = "",
    var speed: String = "",
    var addOn: String = "",
    var description: String = "",
    var price: String = "",
    var imageUrls: MutableList<String> = mutableListOf()  // Añadimos una lista de URLs de imágenes
) {
    constructor() : this("", "", "", "", "", "", "", mutableListOf())
}
