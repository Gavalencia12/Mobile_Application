package com.example.carhive.presentation.notifications.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carhive.R
import com.example.carhive.data.model.NotificationModel
import com.example.carhive.presentation.notifications.adapter.NotificationsAdapter
import com.example.carhive.presentation.notifications.viewModel.NotificationsViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

import androidx.navigation.fragment.findNavController
import com.example.carhive.presentation.notifications.adapter.NotificationsSellerAdapter

@AndroidEntryPoint
class NotificationsSellerFragment : Fragment() {

    private val viewModel: NotificationsViewModel by viewModels()
    private lateinit var adapter: NotificationsSellerAdapter
    private val notifications = mutableListOf<NotificationModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Obtener el ID del usuario autenticado
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            // Pasa el NavController al adaptador
            adapter = NotificationsSellerAdapter(
                notifications,
                currentUserId,
                findNavController() // Pasamos el NavController aquí
            ) { notification ->
                onNotificationClicked(notification)
            }
            recyclerView.adapter = adapter
            viewModel.loadNotifications(currentUserId)
        } else {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }

        observeViewModel()
        return view
    }

    private fun observeViewModel() {
        viewModel.notifications.observe(viewLifecycleOwner) { newNotifications ->
            notifications.clear()
            notifications.addAll(newNotifications)
            adapter.notifyDataSetChanged()
        }
    }

    private fun onNotificationClicked(notification: NotificationModel) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (!notification.isRead) {
            if (currentUserId != null) {
                viewModel.markNotificationAsRead(currentUserId, notification.id)
            } else {
                Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
