package com.example.carhive.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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
@Parcelize
data class MessageEntity(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val carId: String = "",
    val content: String? = null,
    val timestamp: Long = 0L,
    val fileUrl: String? = null,
    val fileType: String? = null,
    val fileName: String? = null,
    val fileSize: Long = 0L,
    val hash: String? = null,
    val status: String = "sent",
    val deletedFor: MutableList<String> = mutableListOf()
): Parcelable

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
    val carId: String,
    val isFile: Boolean = false,
    val fileName: String? = null,
    val fileType: String? = null,
    var unreadCount: Int = 0
)

/**
 * Represents a car with its last message in the chat system.
 *
 * @property car The car entity.
 * @property owner The owner of the car.
 * @property lastMessage The content of the last message associated with the car.
 * @property lastMessageTimestamp Timestamp of the last message related to the car.
 * @property isFile Indicates if the last message is a file.
 * @property fileName Name of the file if the last message is a file.
 * @property fileType Type of the file if the last message is a file (e.g., application, image, video).
 */
data class CarWithLastMessage(
    val car: CarEntity,
    val owner: UserEntity,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val isFile: Boolean = false,
    val fileName: String? = null,
    val fileType: String? = null,
    var unreadCount: Int = 0
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

data class SupportUserData(
    val buyers: List<UserWithLastMessage>,
    val sellers: List<UserWithLastMessage>
)

