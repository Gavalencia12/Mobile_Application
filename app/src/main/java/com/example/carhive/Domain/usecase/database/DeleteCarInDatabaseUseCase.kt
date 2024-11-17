package com.example.carhive.Domain.usecase.database

import com.example.carhive.data.repository.AuthRepository
import javax.inject.Inject

class DeleteCarInDatabaseUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(userId:String, carId:String):Result<Unit>{
        return repository.deleteCarInDatabase(userId, carId)
    }
}