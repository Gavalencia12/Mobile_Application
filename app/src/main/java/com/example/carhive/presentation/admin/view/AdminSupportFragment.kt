package com.example.carhive.presentation.admin.view

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.R
import com.example.carhive.data.model.UserWithLastMessage
import com.example.carhive.presentation.chat.adapter.AdminMessagesAdapter
import com.example.carhive.presentation.chat.view.BaseMessagesFragment
import com.example.carhive.presentation.chat.viewModel.InterestedUsersViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminSupportFragment : BaseMessagesFragment() {

    override val viewModel: InterestedUsersViewModel by viewModels()

    // The owner's ID, retrieved from FirebaseAuth
    private val ownerId = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Initializes the RecyclerView and its adapter, setting up the item click listener to navigate to chat.
     */
    override fun setupRecyclerView() {
        messagesAdminAdapter = AdminMessagesAdapter(mutableListOf(), mutableListOf()) { item ->
            navigateToChat(item)
        }

        binding.recyclerViewInterestedUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = messagesAdminAdapter
        }
    }

    /**
     * Defines navigation behavior based on the selected item type (User).
     * Navigates to the chat screen.
     */
    override val navigateToChat: (Any) -> Unit = { item ->
        if (item is UserWithLastMessage) {
            val bundle = Bundle().apply {
                putString("carId", item.carId)
                putString("buyerId", item.user.id)
                putString("ownerId", "TechnicalSupport")
            }
            findNavController().navigate(R.id.action_adminSupportFragment_to_chatFragment, bundle)
        }
    }

    /**
     * Loads the support users (buyers and sellers) for the current owner.
     */
    override fun loadData() {
        ownerId?.let {
            viewModel.loadSupportUsers(it) // Carga datos de soporte técnico
        }
    }

}
