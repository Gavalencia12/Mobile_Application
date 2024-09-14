package com.example.carhive.models

data class Car(
    var id: String = "",
    var name: String = "",
    var imageUrls: MutableList<String> = mutableListOf()  // Añadimos una lista de URLs de imágenes
) {
    constructor() : this("", "", mutableListOf())
}
