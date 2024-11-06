package com.example.carhive.Domain.usecase.database

import com.example.carhive.data.repository.AuthRepository

class UpdateTermsSellerUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userId:String, termsSeller:Boolean):Result<Unit>{
        return repository.updateTermsSeller(userId, termsSeller)
    }
}