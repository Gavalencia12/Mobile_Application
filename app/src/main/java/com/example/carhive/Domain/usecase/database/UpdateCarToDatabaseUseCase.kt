package com.example.carhive.Domain.usecase.database

import com.example.carhive.data.repository.AuthRepository
import com.example.carhive.Domain.model.Car
import javax.inject.Inject

class UpdateCarToDatabaseUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(userId: String, carId:String, car: Car): Result<Unit> {
        return repository.updateCarToDatabase(userId, carId, car)
    }
}