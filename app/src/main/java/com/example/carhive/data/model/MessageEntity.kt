package com.example.carhive.data.model

/**
 * Represents a message entity in the chat system.
 *
 * @property messageId Unique ID of the message.
 * @property senderId ID of the user who sent the message.
 * @property receiverId ID of the user who received the message.
 * @property carId ID of the car associated with the message.
 * @property content The text content of the message, if any.
 * @property timestamp The time the message was sent, in milliseconds.
 * @property fileUrl URL of the file attached to the message, if any.
 * @property fileType Type of the file attached (e.g., image, video, application).
 * @property fileName Name of the file attached to the message, if any.
 * @property fileSize Size of the attached file in bytes.
 * @property hash Unique hash for the file, used to identify duplicates.
 * @property status Status of the message ("sent", "delivered", "read", "failed").
 * @property deletedFor List of user IDs for whom the message has been deleted.
 */

data class MessageEntity(
    val messageId: String = "",          // Unique ID of the message
    val senderId: String = "",           // ID of the sender
    val receiverId: String = "",         // ID of the receiver
    val carId: String = "",              // ID of the associated car
    val content: String? = null,         // Text content of the message
    val timestamp: Long = 0L,            // Timestamp when the message was sent
    val fileUrl: String? = null,         // URL of the attached file
    val fileType: String? = null,        // Type of the file (e.g., image, video)
    val fileName: String? = null,        // Name of the attached file
    val fileSize: Long = 0L,             // Size of the attached file in bytes
    val hash: String? = null,            // Unique hash for the file
    val status: String = "sent",         // Message status: "sent", "delivered", "read", "failed"
    val deletedFor: MutableList<String> = mutableListOf() // List of users who deleted the message for themselves
)

/**
 * Represents a user with their last message in the chat system.
 *
 * @property user The user entity.
 * @property lastMessage The content of the last message sent or received.
 * @property lastMessageTimestamp Timestamp of the last message.
 * @property carId ID of the car associated with the chat.
 * @property isFile Indicates if the last message is a file.
 * @property fileName Name of the file if the last message is a file.
 * @property fileType Type of the file if the last message is a file (e.g., application, image, video).
 */
data class UserWithLastMessage(
    val user: UserEntity,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val carId: String,                   // ID of the associated car
    val isFile: Boolean = false,         // Indicates if the last message is a file
    val fileName: String? = null,        // Name of the file if it is a file message
    val fileType: String? = null         // Type of file (application, image, video, etc.)
)

/**
 * Represents a car with its last message in the chat system.
 *
 * @property car The car entity.
 * @property lastMessage The content of the last message associated with the car.
 * @property lastMessageTimestamp Timestamp of the last message related to the car.
 * @property isFile Indicates if the last message is a file.
 * @property fileName Name of the file if the last message is a file.
 * @property fileType Type of the file if the last message is a file (e.g., application, image, video).
 */
data class CarWithLastMessage(
    val car: CarEntity,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val isFile: Boolean = false,         // Indicates if the last message is a file
    val fileName: String? = null,        // Name of the file if it is a file message
    val fileType: String? = null         // Type of file (application, image, video, etc.)
)

/**
 * Represents data for a seller interested in users and cars in the chat system.
 *
 * @property interestedUsers List of users who showed interest, along with their last message.
 * @property cars List of cars with their last message for interested users.
 */
data class SellerInterestedData(
    val interestedUsers: List<UserWithLastMessage>,
    val cars: List<CarWithLastMessage>
)
