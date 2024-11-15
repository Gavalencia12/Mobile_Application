package com.example.carhive.Domain.usecase.database

import com.example.carhive.data.repository.AuthRepository
import com.example.carhive.Domain.model.Car
import javax.inject.Inject

class SaveCarToDatabaseUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(userId: String, car: Car): Result<String> {
        return repository.saveCarToDatabase(userId, car)
    }
}