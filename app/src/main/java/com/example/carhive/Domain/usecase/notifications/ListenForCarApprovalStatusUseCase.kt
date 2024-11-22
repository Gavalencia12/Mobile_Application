package com.example.carhive.Domain.usecase.notifications

import com.example.carhive.data.repository.NotificationsRepository

class ListenForCarApprovalStatusUseCase(private val repository: NotificationsRepository) {
    suspend operator fun invoke(ownerId: String, carId: String, onStatusChanged: (Boolean, String) -> Unit){
        return repository.listenForCarApprovalStatus(ownerId, carId, onStatusChanged)
    }
}