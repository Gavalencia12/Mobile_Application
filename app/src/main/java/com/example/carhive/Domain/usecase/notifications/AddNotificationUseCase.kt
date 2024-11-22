package com.example.carhive.Domain.usecase.notifications

import com.example.carhive.data.repository.NotificationsRepository

class AddNotificationUseCase(private val repository: NotificationsRepository) {
    suspend operator fun invoke(userId: String, title: String, message: String){
        return repository.addNotification(userId, title, message)
    }
}