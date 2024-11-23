package com.example.carhive.Domain.usecase.notifications

import com.example.carhive.data.repository.NotificationsRepository

class ShowNotificationUseCase(private val repository: NotificationsRepository) {
    suspend operator fun invoke(title: String, message: String){
        return repository.showNotification(title, message)
    }
}