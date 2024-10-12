package com.example.carhive.Domain.usecase.database

import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Data.repository.AuthRepository
import javax.inject.Inject

class GetCarUserInDatabaseUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(userId:String) : Result<List<CarEntity>> {
        return repository.getCarUserFromDatabase(userId)
    }
}