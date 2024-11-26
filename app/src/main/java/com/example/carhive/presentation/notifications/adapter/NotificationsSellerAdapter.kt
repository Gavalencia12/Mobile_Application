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

class NotificationsSellerAdapter(
    private val notifications: MutableList<NotificationModel>,
    private val navController: NavController,
    private val onDeleteClick: (NotificationModel) -> Unit,
    private val onMarkAsReadClick: (NotificationModel) -> Unit
) : RecyclerView.Adapter<NotificationsSellerAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageView = itemView.findViewById(R.id.notificationIcon)
        val titleTextView: TextView = itemView.findViewById(R.id.notificationTitle)
        val messageTextView: TextView = itemView.findViewById(R.id.notificationMessage)
        val timestampTextView: TextView = itemView.findViewById(R.id.notificationTimestamp)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteNotificationButton)
        val lineIndicator: View = itemView.findViewById(R.id.notificationIndicator) // Línea de color
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
        val iconRes = getIconResource(notification.title)
        holder.iconImageView.setImageResource(iconRes)
        holder.iconImageView.tag = iconRes

        // Actualizar la apariencia según el estado `isRead`
        updateNotificationAppearance(holder, notification.isRead)
        Log.d("NotificationStatus", "isRead: ${notification.isRead}, title: ${notification.title}")

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

    private fun updateNotificationAppearance(holder: NotificationViewHolder, isRead: Boolean) {
        val context = holder.itemView.context
        // Actualizar el fondo
        holder.itemView.setBackgroundColor(
            if (isRead) ContextCompat.getColor(context, android.R.color.transparent)
            else ContextCompat.getColor(context, R.color.white)
        )
        // Actualizar el color de la línea lateral
        if (!isRead) {
            val iconColor = when (holder.iconImageView.tag) { // Usa el tag para identificar el ícono
                R.drawable.ic_verify_account -> ContextCompat.getColor(context, R.color.green_tree)
                R.drawable.ic_check_car -> ContextCompat.getColor(context, R.color.green_woods)
                R.drawable.ic_favorite_cars -> ContextCompat.getColor(context, R.color.red_bright)
                R.drawable.ic_message_unread -> ContextCompat.getColor(context, R.color.yellow_sun)
                R.drawable.ic_desactive_user -> ContextCompat.getColor(context, R.color.red_bright)
                R.drawable.ic_desactive_car -> ContextCompat.getColor(context, R.color.red_bright)
                else -> ContextCompat.getColor(context, R.color.blue)
            }
            holder.lineIndicator.setBackgroundColor(iconColor)
        } else {
            // Para notificaciones leídas, línea transparente
            holder.lineIndicator.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        }
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

    private fun getIconResource(title: String): Int {
        return when {
            title.contains("Account Verified", ignoreCase = true) -> R.drawable.ic_verify_account
            title.contains("Account Deactivated", ignoreCase = false) -> R.drawable.ic_desactive_user
            title.contains("Car approved!", ignoreCase = true) -> R.drawable.ic_check_car
            title.contains("Car disapproved!", ignoreCase = true) -> R.drawable.ic_desactive_car
            title.contains("Car added to favorites", ignoreCase = true) || title.contains("New favorite for your car", ignoreCase = true) -> R.drawable.ic_favorite_cars
            title.contains("New Message", ignoreCase = true) || title.contains("Unread Messages", ignoreCase = true) -> R.drawable.ic_message_unread
            else -> R.drawable.ic_new_notification
        }
    }
}
