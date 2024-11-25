package com.example.carhive.presentation.notifications.adapter

import android.content.Context
import android.util.Log
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
import java.text.SimpleDateFormat
import java.util.*

class NotificationsAdapter(
    private val notifications: MutableList<NotificationModel>,
    private val userId: String,
    private val navController: NavController,
    private val onDeleteClick: (NotificationModel) -> Unit,
    private val onMarkAsReadClick: (NotificationModel) -> Unit
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

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

        // Formatear la fecha
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.timestampTextView.text = formatter.format(Date(notification.timestamp))

        // Establecer ícono según el título
        holder.iconImageView.setImageResource(getIconResource(notification.title))

        // Actualizar el fondo según el estado `isRead`
        updateNotificationBackground(holder, notification.isRead)

        // Escuchar clics en el botón de eliminar
        holder.deleteButton.setOnClickListener {
            showDeleteConfirmation(holder.itemView.context, notification)
        }

        // Escuchar clic en la notificación
        holder.itemView.setOnClickListener {
            if (!notification.isRead) {
                onMarkAsReadClick(notification) // Marca como leído
            }
            navigateToFragment(notification.title)
        }
    }

    private fun updateNotificationBackground(holder: NotificationViewHolder, isRead: Boolean) {
        val context = holder.itemView.context
        holder.itemView.setBackgroundColor(
            if (isRead) ContextCompat.getColor(context, android.R.color.transparent)
            else ContextCompat.getColor(context, R.color.notification_unread_background)
        )
    }

    fun updateNotifications(newNotifications: List<NotificationModel>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged() // Forzar actualización completa
    }

    private fun showDeleteConfirmation(context: Context, notification: NotificationModel) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle("Delete Notification")
        builder.setMessage("Are you sure you want to delete this notification?")
        builder.setPositiveButton("Delete") { _, _ ->
            onDeleteClick(notification)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    override fun getItemCount(): Int = notifications.size

    private fun navigateToFragment(title: String) {
        when {
            title.contains("Account Verified", ignoreCase = true) || title.contains("Account Deactivated", ignoreCase = false) -> {
                navController.navigate(R.id.action_notificationsFragment_to_profile)
            }
            title.contains("Car added to favorites", ignoreCase = true) || title.contains("New favorite for your car", ignoreCase = true) -> {
                navController.navigate(R.id.action_notificationsFragment_to_favorites)
            }
            title.contains("New Message", ignoreCase = true) || title.contains("Unread Messages", ignoreCase = true) -> {
                navController.navigate(R.id.action_notificationsFragment_to_chats)
            }
            else -> {
                navController.navigate(R.id.action_notificationsFragment_to_defaultFragment)
            }
        }
    }

    private fun getIconResource(title: String): Int {
        return when {
            title.contains("Account Verified", ignoreCase = true) -> R.drawable.ic_verify_account
            title.contains("Account Deactivated", ignoreCase = false) -> R.drawable.ic_verify_account
            title.contains("Car added to favorites", ignoreCase = true) || title.contains("New favorite for your car", ignoreCase = true) -> R.drawable.ic_favorite_cars
            title.contains("New Message", ignoreCase = true) || title.contains("Unread Messages", ignoreCase = true) -> R.drawable.ic_message_unread
            else -> R.drawable.ic_new_notification
        }
    }
}
