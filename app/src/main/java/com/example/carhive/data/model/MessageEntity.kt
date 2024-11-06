package com.example.carhive.data.model

data class MessageEntity(
    val senderId: String = "",
    val content: String? = null,
    val timestamp: Long = 0L,
    val fileUrl: String? = null,
    val fileType: String? = null,  // Tipo de archivo: "image", "video", "pdf", etc.
    val fileName: String? = null,   // Nombre del archivo si es PDF
    val hash: String? = null
)

data class UserWithLastMessage(
    val user: UserEntity,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val carId: String // Agrega esta propiedad para almacenar el carId asociado
)

data class CarWithLastMessage(
    val car: CarEntity,
    val lastMessage: String,
    val lastMessageTimestamp: Long
)

data class SellerInterestedData(
    val interestedUsers: List<UserWithLastMessage>,
    val cars: List<CarWithLastMessage>
)

