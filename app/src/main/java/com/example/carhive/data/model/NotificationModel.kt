package com.example.carhive.data.model

data class NotificationModel(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    var isRead: Boolean = false
)

