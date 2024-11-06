package com.example.carhive.Domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Parcelize
data class Message(
    val senderId: String,
    val content: String? = null,
    val timestamp: Long,
    val fileUrl: String? = null,
    val fileType: String? = null,
    val fileName: String? = null,
    val hash: String? = null     // Hash del archivo para optimizar almacenamiento y evitar duplicación
) : Parcelable {
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
