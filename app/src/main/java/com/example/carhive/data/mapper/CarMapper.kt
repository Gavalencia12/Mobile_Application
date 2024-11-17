package com.example.carhive.data.mapper

import com.example.carhive.data.model.CarEntity
import com.example.carhive.Domain.model.Car
import javax.inject.Inject

/**
 * Mapper class for converting between CarEntity (Data Layer) and Car (Domain Layer) models.
 * This class is responsible for mapping data to ensure seamless data transfer between layers.
 */
class CarMapper @Inject constructor() {

    /**
     * Maps a CarEntity from the data layer to a Car model in the domain layer.
     * @param entity The CarEntity object to map.
     * @return A Car object with properties copied from the entity.
     */
    fun mapToDomain(entity: CarEntity): Car {
        return Car(
            id = entity.id,                       // Unique identifier
            modelo = entity.modelo,               // Model of the car
            color = entity.color,                 // Car color
            mileage = entity.mileage,             // Mileage or distance traveled
            brand = entity.brand,                 // Brand or manufacturer
            description = entity.description,     // Description of the car
            price = entity.price,                 // Price in specific currency
            year = entity.year,                   // Manufacturing year
            sold = entity.sold,                   // Sales status (sold or available)
            imageUrls = entity.imageUrls,         // List of URLs pointing to car images
            ownerId = entity.ownerId,             // Identifier for the owner of the car
            transmission = entity.transmission,   // Transmission type (automatic/manual)
            fuelType = entity.fuelType,           // Fuel type (e.g., gasoline, diesel)
            doors = entity.doors,                 // Number of doors
            engineCapacity = entity.engineCapacity, // Engine capacity in liters or cc
            location = entity.location,           // Geographic location of the car
            condition = entity.condition,         // Condition status (e.g., new, used)
            features = entity.features,           // List of features or add-ons
            vin = entity.vin,                     // Vehicle Identification Number (VIN)
            previousOwners = entity.previousOwners, // Number of previous owners
            views = entity.views,                 // Number of views on the listing
            listingDate = entity.listingDate,     // Date the listing was created
            lastUpdated = entity.lastUpdated      // Date the listing was last updated
        )
    }

    /**
     * Maps a Car model from the domain layer to a CarEntity in the data layer.
     * @param domain The Car object to map.
     * @return A CarEntity object with properties copied from the domain model.
     */
    fun mapToEntity(domain: Car): CarEntity {
        return CarEntity(
            id = domain.id,                       // Unique identifier
            modelo = domain.modelo,               // Model of the car
            color = domain.color,                 // Car color
            mileage = domain.mileage,             // Mileage or distance traveled
            brand = domain.brand,                 // Brand or manufacturer
            description = domain.description,     // Description of the car
            price = domain.price,                 // Price in specific currency
            year = domain.year,                   // Manufacturing year
            sold = domain.sold,                   // Sales status (sold or available)
            imageUrls = domain.imageUrls,         // List of URLs pointing to car images
            ownerId = domain.ownerId,             // Identifier for the owner of the car
            transmission = domain.transmission,   // Transmission type (automatic/manual)
            fuelType = domain.fuelType,           // Fuel type (e.g., gasoline, diesel)
            doors = domain.doors,                 // Number of doors
            engineCapacity = domain.engineCapacity, // Engine capacity in liters or cc
            location = domain.location,           // Geographic location of the car
            condition = domain.condition,         // Condition status (e.g., new, used)
            features = domain.features,           // List of features or add-ons
            vin = domain.vin,                     // Vehicle Identification Number (VIN)
            previousOwners = domain.previousOwners, // Number of previous owners
            views = domain.views,                 // Number of views on the listing
            listingDate = domain.listingDate,     // Date the listing was created
            lastUpdated = domain.lastUpdated      // Date the listing was last updated
        )
    }
}
