package com.example.carhive.Domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Parcelize
data class Message(
    val messageId: String,
    val senderId: String,
    val receiverId: String,
    val carId: String,
    val content: String? = null,
    val timestamp: Long,
    val fileUrl: String? = null,
    val fileType: String? = null,
    val fileName: String? = null,
    val fileSize: Long = 0L,
    val hash: String? = null,
    val status: String = "sent",
    val deletedFor: List<String> = listOf()
): Parcelable {
    fun getFormattedTime(): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return formatter.format(date)
    }

    fun getFormattedDate(): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return formatter.format(date)
    }
}
