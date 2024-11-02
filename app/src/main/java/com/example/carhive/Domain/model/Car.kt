package com.example.carhive.Domain.model

data class Car(
    var id: String = "",
    var modelo: String = "",
    var color: String = "",
    var mileage: String = "",
    var brand: String = "",
    var description: String = "",
    var price: String = "",
    var year: String = "",
    var sold: Boolean = false,
    var imageUrls: List<String>? = mutableListOf(),
    var ownerId: String = "",

    // Campos adicionales
    var transmission: String = "",               // Tipo de transmisión: manual o automática
    var fuelType: String = "",                   // Tipo de combustible: gasolina, diésel, eléctrico, híbrido
    var doors: Int = 0,                          // Número de puertas
    var engineCapacity: String = "",             // Capacidad del motor, por ejemplo, "2.0L"
    var location: String = "",                   // Ubicación del vehículo
    var condition: String = "",                  // Estado del vehículo: nuevo, usado, etc.
    var features: List<String>? = mutableListOf(), // Lista de características adicionales
    var vin: String = "",                        // Número de identificación del vehículo (VIN)
    var previousOwners: Int = 0,                 // Número de propietarios anteriores
    var views: Int = 0,                          // Número de vistas en la publicación

    // Fechas de creación y última actualización
    var listingDate: String = "",                  // Fecha de creación
    var lastUpdated: String = ""                   // Fecha de última actualización
)
