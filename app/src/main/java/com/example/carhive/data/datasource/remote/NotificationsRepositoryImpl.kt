package com.example.carhive.data.datasource.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.carhive.data.repository.NotificationsRepository
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject

class NotificationsRepositoryImpl @Inject constructor(
    private val context: Context,
    private val database: FirebaseDatabase
) : NotificationsRepository {

    override suspend fun listenForChatMessages(groupId: String, onNewMessage: (String, String, String) -> Unit) {
        val messagesRef = database.getReference("ChatGroups/$groupId/messages")

        messagesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val messageId = snapshot.key ?: return
                val message = snapshot.child("message").getValue(String::class.java) ?: return
                val senderId = snapshot.child("senderId").getValue(String::class.java) ?: return

                // Lógica para manejar el mensaje recibido
                onNewMessage(messageId, senderId, message)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                println("Error al escuchar mensajes: ${error.message}")
            }
        })
    }

    override suspend fun listenForNewFavorites(carId: String, onFavoriteAdded: (String, String, String) -> Unit) {
        val carFavoritesRef = database.getReference("Favorites/CarFavorites/$carId/users")

        carFavoritesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val userId = snapshot.key ?: return
                val userName = snapshot.child("userName").getValue(String::class.java) ?: return
                val addedAt = snapshot.child("addedAt").getValue(Long::class.java) ?: return

                // Callback para manejar el nuevo favorito
                onFavoriteAdded(userId, userName, carId)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                println("Error al escuchar favoritos: ${error.message}")
            }
        })
    }

    override suspend fun listenForCarApprovalStatus(
        ownerId: String,
        carId: String,
        onStatusChanged: (Boolean, String) -> Unit
    ) {
        val carRef = database.getReference("Car/$ownerId/$carId/approved")

        carRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isApproved = snapshot.getValue(Boolean::class.java)
                if (isApproved != null) {
                    onStatusChanged(isApproved, carId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error al escuchar cambios de aprobación: ${error.message}")
            }
        })
    }

    override suspend fun listenForUserVerification(userId: String, isVerified: Boolean, fullName: String) {
        val notificationRef = database.getReference("Notifications/$userId").push()

        val notification = mapOf(
            "id" to notificationRef.key,
            "title" to if (isVerified) "Account Verified" else "Account Deactivated",
            "message" to if (isVerified) {
                "Dear $fullName, your account has been successfully verified."
            } else {
                "Dear $fullName, your account has been deactivated. Please contact support for more details."
            },
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false
        )

        notificationRef.setValue(notification).addOnSuccessListener {
            println("Notification sent for user $userId.")
        }.addOnFailureListener { e ->
            println("Failed to send notification: ${e.message}")
        }
    }

    override suspend fun notifyCarApprovalStatus(
        userId: String,
        carModel: String,
        isApproved: Boolean
    ) {
        val title = if (isApproved) "Car approved!" else "Car disapproved!"
        val message = if (isApproved) {
            "Your $carModel car has been approved and is visible to other users.."
        } else {
            "Your $carModel car has not been approved. Please review the information and try again.."
        }

        addNotification(userId, title, message)
        showNotification(title, message)
    }

    override suspend fun addNotification(userId: String, title: String, message: String) {
        val notificationRef = database.getReference("Notifications/$userId").push()

        val notification = mapOf(
            "id" to notificationRef.key,
            "title" to title,
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false
        )

        notificationRef.setValue(notification).addOnSuccessListener {
            println("Notificación añadida correctamente para el usuario $userId")
        }.addOnFailureListener { e ->
            println("Error al añadir la notificación: ${e.message}")
        }
    }

    override suspend fun markNotificationAsRead(userId: String, notificationId: String) {
        val notificationRef = database.getReference("Notifications/$userId/$notificationId/isRead")
        try {
            notificationRef.setValue(true).addOnSuccessListener {
                println("Notificación $notificationId marcada como leída para el usuario $userId")
            }.addOnFailureListener { e ->
                println("Error al marcar la notificación como leída: ${e.message}")
            }
        } catch (e: Exception) {
            println("Error en la operación suspendida: ${e.message}")
        }
    }

    override suspend fun showNotification(title: String, message: String) {
        val channelId = "default_channel"
        val notificationId = System.currentTimeMillis().toInt()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = NotificationManagerCompat.from(context)
        manager.notify(notificationId, builder.build())
    }

}