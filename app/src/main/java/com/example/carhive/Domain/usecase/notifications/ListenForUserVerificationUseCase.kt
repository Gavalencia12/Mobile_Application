package com.example.carhive.Domain.usecase.notifications

import com.example.carhive.data.repository.NotificationsRepository

class ListenForUserVerificationUseCase(private val repository: NotificationsRepository) {
    suspend operator fun invoke(userId: String, isVerified: Boolean, fullName: String){
        return repository.listenForUserVerification(userId, isVerified, fullName)
    }
}