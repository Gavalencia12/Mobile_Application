package com.example.carhive.Domain.usecase.chats

import android.content.Context
import com.example.carhive.data.repository.ChatRepository

class CleanUpDatabaseUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(context: Context){
        return repository.cleanUpDatabase(context)
    }
}