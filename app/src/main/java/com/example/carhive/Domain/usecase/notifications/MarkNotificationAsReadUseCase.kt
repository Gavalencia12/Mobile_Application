package com.example.carhive.Domain.usecase.notifications

import com.example.carhive.data.repository.NotificationsRepository

class MarkNotificationAsReadUseCase(private val repository: NotificationsRepository) {
    suspend operator fun invoke(userId: String, notificationId: String){
        return repository.markNotificationAsRead(userId, notificationId)
    }
}