package com.example.carhive.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.carhive.data.model.MessageEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UnreadMessagesWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    override suspend fun doWork(): Result {
        try {
            // Check if user is authenticated
            if (currentUserId.isEmpty()) {
                Log.e("UnreadMessagesWorker", "User is not authenticated.")
                return Result.failure()
            }

            Log.d("UnreadMessagesWorker", "Starting to count unread messages for user: $currentUserId")

            // Call the function to count unread messages
            val unreadChatsCount = getUnreadChatsCount()

            Log.d("UnreadMessagesWorker", "Unread chats count: $unreadChatsCount")

            // If there are unread messages, send a notification
            if (unreadChatsCount > 0) {
                sendUnreadChatsNotification(unreadChatsCount)
            }

            Log.d("UnreadMessagesWorker", "Worker completed successfully.")
            return Result.success()

        } catch (e: Exception) {
            // Log the error to understand the failure
            Log.e("UnreadMessagesWorker", "Error in worker: ${e.message}", e)
            return Result.failure()
        }
    }

    /**
     * Retrieves the count of chats with unread messages.
     */
    private suspend fun getUnreadChatsCount(): Int {
        val chatGroupsRef = FirebaseDatabase.getInstance().getReference("ChatGroups")
        val snapshot = chatGroupsRef.get().await()
        var unreadChatsCount = 0

        for (ownerSnapshot in snapshot.children) {
            val ownerId = ownerSnapshot.key ?: continue
            for (carSnapshot in ownerSnapshot.children) {
                val carId = carSnapshot.key ?: continue
                val messagesSnapshot = carSnapshot.child("messages")
                for (userSnapshot in messagesSnapshot.children) {
                    for (messageSnapshot in userSnapshot.children) {
                        val message = messageSnapshot.getValue(MessageEntity::class.java)
                        if (message != null && message.receiverId == currentUserId &&
                            message.status == "sent" && !message.deletedFor.contains(currentUserId)
                        ) {
                            unreadChatsCount++
                            break // Count one unread message per chat
                        }
                    }
                }
            }
        }
        return unreadChatsCount
    }

    /**
     * Sends a notification to the user about unread messages.
     */
    private fun sendUnreadChatsNotification(unreadChatsCount: Int) {
        val notificationRef = FirebaseDatabase.getInstance()
            .getReference("Notifications/$currentUserId")
            .push()

        val notification = mapOf(
            "id" to notificationRef.key,
            "title" to "Unread Messages",
            "message" to "You have $unreadChatsCount chats with unread messages.",
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false
        )

        notificationRef.setValue(notification).addOnSuccessListener {
            Log.d("UnreadMessagesWorker", "Notification sent successfully.")
        }.addOnFailureListener { e ->
            Log.e("UnreadMessagesWorker", "Failed to send notification: ${e.message}", e)
        }
    }
}

