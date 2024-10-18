package com.example.carhive.Domain.usecase.database

import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Data.repository.AuthRepository

class GetAllCarsFromDatabaseUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() :Result<List<CarEntity>>{
        return repository.getAllCarsFromDatabase()
    }
}