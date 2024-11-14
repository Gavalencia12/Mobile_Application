package com.example.carhive.data.model

/**
 * Data model representing a car entity in the data layer.
 * This model holds all attributes related to a car listing.
 */
data class CarEntity(
    var id: String = "",                           // Unique identifier for the car
    var modelo: String = "",                       // Car model name
    var color: String = "",                        // Car color
    var mileage: String = "",                      // Mileage or distance the car has traveled
    var brand: String = "",                        // Brand or manufacturer of the car
    var description: String = "",                  // Description of the car
    var price: String = "",                        // Price of the car as a string
    var year: String = "",                         // Year the car was manufactured
    var sold: Boolean = false,                     // Sales status (true if sold)
    var imageUrls: List<String>? = mutableListOf(),// List of image URLs for the car
    var ownerId: String = "",                      // Owner's unique identifier
    var approved: Boolean = false,


    // Additional fields
    var transmission: String = "",                 // Transmission type: manual or automatic
    var fuelType: String = "",                     // Fuel type: gasoline, diesel, electric, hybrid
    var doors: Int = 0,                            // Number of doors in the car
    var engineCapacity: String = "",               // Engine capacity, e.g., "2.0L"
    var location: String = "",                     // Geographic location of the car
    var condition: String = "",                    // Condition of the car: new, used, etc.
    var features: List<String>? = mutableListOf(), // List of additional features or options
    var vin: String = "",                          // Vehicle Identification Number (VIN)
    var previousOwners: Int = 0,                   // Number of previous owners
    var views: Int = 0,                            // Number of views the listing has received

    // Listing dates
    var listingDate: String = "",                  // Date when the listing was created
    var lastUpdated: String = ""                   // Date when the listing was last updated
)
