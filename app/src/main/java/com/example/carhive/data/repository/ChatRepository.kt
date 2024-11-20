package com.example.carhive.data.repository

import android.content.Context
import android.net.Uri
import com.example.carhive.Domain.model.Message
import com.example.carhive.data.model.CarEntity
import com.example.carhive.data.model.SupportUserData
import com.example.carhive.data.model.UserWithLastMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(ownerId: String, carId: String, buyerId: String): Flow<Message>
    suspend fun sendMessage(
        ownerId: String,
        carId: String,
        buyerId: String,
        message: Message
    ): Result<Unit>

    suspend fun getAllMessagesOnce(ownerId: String, carId: String, buyerId: String): List<Message>

    suspend fun getUserInfo(userId: String, carId: String): Result<CarEntity?>
    suspend fun getInterestedUsers(
        ownerId: String?,
        userId: String?,
        direction: String,
        type: String
    ): List<Any>

    suspend fun sendFileMessage(
        ownerId: String,
        carId: String,
        buyerId: String,
        message: Message
    ): Result<Unit>

    suspend fun cleanUpDatabase(context: Context)

    suspend fun updateMessageStatus(ownerId: String, carId: String, buyerId: String, messageId: String, status: String)

    suspend fun deleteMessageForUser(ownerId: String, carId: String, buyerId: String, messageId: String, userId: String)

    suspend fun getSupportUsers(ownerId: String): SupportUserData

}