package com.example.carhive.Data.mapper

import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Domain.model.Car
import javax.inject.Inject

class CarMapper @Inject constructor() {

    fun mapToDomain(entity: CarEntity): Car {
        return Car (
            id = entity.id,
            modelo = entity.modelo,
            color = entity.color,
            speed = entity.speed,
            addOn = entity.addOn,
            description = entity.description,
            price = entity.price,
            imageUrls = entity.imageUrls,
            ownerId = entity.ownerId
        )
    }

    fun mapToEntity(domain: Car): CarEntity{
        return CarEntity(
            id = domain.id,
            modelo = domain.modelo,
            color = domain.color,
            speed = domain.speed,
            addOn = domain.addOn,
            description = domain.description,
            price = domain.price,
            imageUrls = domain.imageUrls,
            ownerId = domain.ownerId
        )
    }
}