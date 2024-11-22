package com.example.carhive.Domain.usecase.notifications

import com.example.carhive.data.repository.NotificationsRepository

class NotifyCarApprovalStatusUseCase(private val repository: NotificationsRepository) {
    suspend operator fun invoke(userId: String, carModel: String, isApproved: Boolean){
        return repository.notifyCarApprovalStatus(userId, carModel, isApproved)
    }
}