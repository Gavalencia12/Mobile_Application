package com.example.carhive.presentation.notifications.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.usecase.notifications.AddNotificationUseCase
import com.example.carhive.Domain.usecase.notifications.ListenForChatMessagesUseCase
import com.example.carhive.Domain.usecase.notifications.ShowNotificationUseCase
import com.example.carhive.Domain.usecase.notifications.MarkNotificationAsReadUseCase
import com.example.carhive.data.model.NotificationModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
) : ViewModel() {

    private val _notifications = MutableLiveData<List<NotificationModel>>()
    val notifications: LiveData<List<NotificationModel>> get() = _notifications

    fun loadNotifications(userId: String) {
        val notificationsRef = FirebaseDatabase.getInstance()
            .getReference("Notifications/$userId")
            .orderByChild("timestamp")
            .limitToLast(20)

        notificationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notificationList = snapshot.children.mapNotNull {
                    val notification = it.getValue(NotificationModel::class.java)
                    // Si el campo isRead es null, configúralo como false por defecto
                    notification?.copy(isRead = it.child("isRead").getValue(Boolean::class.java) ?: false)
                }.sortedByDescending { it.timestamp }

                _notifications.postValue(notificationList) // Actualiza LiveData
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error fetching notifications: ${error.message}")
            }
        })
    }

    fun markNotificationAsRead(userId: String, notificationId: String) {
        val notificationRef = FirebaseDatabase.getInstance()
            .getReference("Notifications/$userId/$notificationId")
        notificationRef.child("isRead").setValue(true).addOnSuccessListener {
            // Recargar notificaciones después de marcar como leído
            loadNotifications(userId)
        }
    }

    fun deleteNotification(userId: String, notificationId: String) {
        val notificationRef = FirebaseDatabase.getInstance()
            .getReference("Notifications/$userId/$notificationId")
        notificationRef.removeValue()
    }
}
