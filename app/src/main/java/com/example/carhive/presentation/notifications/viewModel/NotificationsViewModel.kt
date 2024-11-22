package com.example.carhive.presentation.notifications.viewModel

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
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase
) : ViewModel() {

    private val _notifications = MutableLiveData<List<NotificationModel>>()
    val notifications: LiveData<List<NotificationModel>> get() = _notifications

    fun loadNotifications(userId: String) {
        val notificationsRef = FirebaseDatabase.getInstance()
            .getReference("Notifications/$userId")
            .orderByChild("timestamp") // Ordenar por el campo de timestamp
            .limitToLast(20) // Limitar a las últimas 20 notificaciones

        notificationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notificationsList = mutableListOf<NotificationModel>()
                for (childSnapshot in snapshot.children) {
                    val notification = childSnapshot.getValue(NotificationModel::class.java)
                    if (notification != null) {
                        notificationsList.add(notification)
                    }
                }
                // Invertir el orden para que las más recientes queden arriba
                notificationsList.reverse()

                _notifications.postValue(notificationsList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo del error
                println("Error fetching notifications: ${error.message}")
            }
        })
    }


    fun markNotificationAsRead(userId: String, notificationId: String) {
        viewModelScope.launch {
            markNotificationAsReadUseCase(userId, notificationId)
        }
    }
}
