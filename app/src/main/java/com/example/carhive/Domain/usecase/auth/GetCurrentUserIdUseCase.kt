package com.example.carhive.Domain.usecase.auth

import com.example.carhive.Data.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserIdUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() : Result<String?>{
        return repository.getCurrentUserId()
    }
}