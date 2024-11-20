package com.example.carhive.Domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Represents a message in the chat system.
 *
 * @property messageId Unique identifier for the message.
 * @property senderId ID of the user who sent the message.
 * @property receiverId ID of the user who received the message.
 * @property carId ID of the car associated with the message.
 * @property content Text content of the message, if any.
 * @property timestamp The time the message was sent, in milliseconds.
 * @property fileUrl URL of any file attached to the message.
 * @property fileType Type of the attached file (e.g., image, video).
 * @property fileName Name of the attached file, if applicable.
 * @property fileSize Size of the attached file in bytes.
 * @property hash Unique hash of the file, used for identification.
 * @property status Status of the message ("sent", "delivered", "read", "failed").
 * @property deletedFor List of user IDs for whom the message has been marked as deleted.
 */
@Parcelize
data class Message(
    val messageId: String,                // Unique ID of the message
    val senderId: String,                 // ID of the sender
    val receiverId: String,               // ID of the receiver
    val carId: String,                    // ID of the associated car
    val content: String? = null,          // Text content of the message
    val timestamp: Long,                  // Timestamp when the message was sent
    val fileUrl: String? = null,          // URL of the attached file, if any
    val fileType: String? = null,         // Type of the attached file (e.g., image, video)
    val fileName: String? = null,         // Name of the attached file, if any
    val fileSize: Long = 0L,              // Size of the attached file in bytes
    val hash: String? = null,             // Unique hash for the attached file
    var status: String = "sent",          // Status of the message
    val deletedFor: MutableList<String> = mutableListOf() // List of users who have deleted the message
) : Parcelable {

    /**
     * Formats the timestamp to a string in "hh:mm a" format (e.g., "03:45 PM").
     *
     * @return A formatted time string.
     */
    fun getFormattedTime(): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Formats the timestamp to a string in "dd MMMM yyyy" format (e.g., "21 October 2023").
     *
     * @return A formatted date string.
     */
    fun getFormattedDate(): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return formatter.format(date)
    }
}