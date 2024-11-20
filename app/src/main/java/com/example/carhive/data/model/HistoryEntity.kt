package com.example.carhive.data.model

data class HistoryEntity(
    val userId: String? = null,
    val timestamp: Long? = null,
    val eventType: String? = null,
    val message: String? = null
)