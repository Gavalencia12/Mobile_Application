package com.example.carhive.data.model

data class MessageEntity(
    val messageId: String = "",          // ID único del mensaje
    val senderId: String = "",
    val receiverId: String = "",
    val carId: String = "",
    val content: String? = null,
    val timestamp: Long = 0L,
    val fileUrl: String? = null,
    val fileType: String? = null,        // Nuevo campo para el tipo de archivo
    val fileName: String? = null,
    val fileSize: Long = 0L,
    val hash: String? = null,
    val status: String = "sent",         // Estado: "sent", "delivered", "read", "failed"
    val deletedFor: List<String> = listOf() // Lista de usuarios que han eliminado el mensaje para ellos
)

data class UserWithLastMessage(
    val user: UserEntity,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val carId: String,                   // Para almacenar el carId asociado
    val isFile: Boolean = false,         // Nuevo campo para indicar si el mensaje es un archivo
    val fileName: String? = null,        // Nombre del archivo si es un archivo
    val fileType: String? = null         // Tipo de archivo si es un archivo (application, image, video, etc.)
)

data class CarWithLastMessage(
    val car: CarEntity,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val isFile: Boolean = false,         // Nuevo campo para indicar si el mensaje es un archivo
    val fileName: String? = null,        // Nombre del archivo si es un archivo
    val fileType: String? = null         // Tipo de archivo si es un archivo (application, image, video, etc.)
)

data class SellerInterestedData(
    val interestedUsers: List<UserWithLastMessage>,
    val cars: List<CarWithLastMessage>
)
