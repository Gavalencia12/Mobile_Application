package com.example.carhive.presentation.notifications.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.NavController
import com.example.carhive.R
import com.example.carhive.data.model.NotificationModel
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class NotificationsSellerAdapter(
    private val notifications: MutableList<NotificationModel>,
    private val userId: String,
    private val navController: NavController, // NavController para manejar la navegación
    private val onItemClick: (NotificationModel) -> Unit
) : RecyclerView.Adapter<NotificationsSellerAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageView = itemView.findViewById(R.id.notificationIcon)
        val titleTextView: TextView = itemView.findViewById(R.id.notificationTitle)
        val messageTextView: TextView = itemView.findViewById(R.id.notificationMessage)
        val timestampTextView: TextView = itemView.findViewById(R.id.notificationTimestamp)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteNotificationButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.titleTextView.text = notification.title
        holder.messageTextView.text = notification.message

        // Format date/time
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = Date(notification.timestamp)
        holder.timestampTextView.text = formatter.format(date)

        // Set the icon based on title
        holder.iconImageView.setImageResource(getIconResource(notification.title))

        // Update background based on read status
        updateNotificationBackground(holder, notification.isRead)

        // Listen for delete button clicks
        holder.deleteButton.setOnClickListener {
            showDeleteConfirmation(holder.itemView.context, notification, position)
        }

        // Handle notification click
        holder.itemView.setOnClickListener {
            navigateToFragment(notification.title)
            onItemClick(notification)
        }
    }

    private fun updateNotificationBackground(holder: NotificationViewHolder, isRead: Boolean) {
        val context = holder.itemView.context
        if (isRead) {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(context, android.R.color.transparent)
            )
        } else {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(context, R.color.notification_unread_background)
            )
        }
    }

    override fun getItemCount(): Int = notifications.size

    // Show confirmation dialog before deleting the notification
    private fun showDeleteConfirmation(context: Context, notification: NotificationModel, position: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle("Delete Notification")
        builder.setMessage("Are you sure you want to delete this notification?")
        builder.setPositiveButton("Delete") { _, _ ->
            deleteNotification(notification, position)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    // Delete the notification from Firebase and update the list
    private fun deleteNotification(notification: NotificationModel, position: Int) {
        val notificationRef = FirebaseDatabase.getInstance()
            .getReference("Notifications/$userId/${notification.id}")

        notificationRef.removeValue().addOnSuccessListener {
            notifications.removeAt(position)
            notifyItemRemoved(position)
        }.addOnFailureListener { e ->
            println("Error deleting notification: ${e.message}")
        }
    }

    // Navegación según el título de la notificación
    private fun navigateToFragment(title: String) {
        when {
            title.contains("Account Verified", ignoreCase = true) || title.contains("Account Deactivated", ignoreCase = false) -> {
                navController.navigate(R.id.action_notificationsSellerFragment_to_profile)
            }
            title.contains("Car approved!", ignoreCase = true) || title.contains("Car disapproved!", ignoreCase = true) -> {
                navController.navigate(R.id.action_notificationsSellerFragment_to_allCars)
            }
            title.contains("Car added to favorites", ignoreCase = true) || title.contains("New favorite for your car", ignoreCase = true) -> {
                navController.navigate(R.id.action_notificationsSellerFragment_to_favorites)
            }
            title.contains("New Message", ignoreCase = true) || title.contains("Unread Messages", ignoreCase = true) -> {
                navController.navigate(R.id.action_notificationsSellerFragment_to_chats)
            }
            else -> {
                navController.navigate(R.id.action_notificationsSellerFragment_to_defaultFragment)
            }
        }
    }

    // Asignar íconos basados en el título
    private fun getIconResource(title: String): Int {
        return when {
            title.contains("Account Verified", ignoreCase = true) || title.contains("Account Deactivated", ignoreCase = false) -> R.drawable.ic_notifications_account
            title.contains("Car approved!", ignoreCase = true) || title.contains("Car disapproved!", ignoreCase = true) -> R.drawable.ic_notification_car
            title.contains("Car added to favorites", ignoreCase = true) || title.contains("New favorite for your car", ignoreCase = true) -> R.drawable.ic_notification_favorites
            title.contains("New Message", ignoreCase = true) || title.contains("Unread Messages", ignoreCase = true) -> R.drawable.ic_notification_chats
            else -> R.drawable.ic_notification
        }
    }
}
