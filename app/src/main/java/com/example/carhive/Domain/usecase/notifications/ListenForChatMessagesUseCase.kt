package com.example.carhive.Domain.usecase.notifications

import com.example.carhive.data.repository.NotificationsRepository

class ListenForChatMessagesUseCase(private val repository: NotificationsRepository) {
    suspend operator fun invoke(groupId: String, onNewMessage: (String, String, String) -> Unit){
        return repository.listenForChatMessages(groupId, onNewMessage)
    }
}