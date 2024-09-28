package com.example.carhive.Domain.initial.usecase

import com.example.carhive.Data.initial.repository.AuthRepository
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userId: String) = authRepository.getUserById(userId)
}
