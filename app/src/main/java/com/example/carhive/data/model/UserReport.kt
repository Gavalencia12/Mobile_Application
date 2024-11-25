package com.example.carhive.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserReport(
    val userId: String = "",
    val carId: String = "",
    val comment: String = "",
    val sampleMessages: List<MessageEntity> = emptyList(), // Asegúrate de que MessageEntity también sea Parcelable
    val reportedUserId: String = "",
    val timestamp: Long = 0L
) : Parcelable

