package com.example.carhive.presentation.user.view

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.R
import com.example.carhive.data.model.CarWithLastMessage
import com.example.carhive.presentation.chat.view.BaseMessagesFragment
import com.example.carhive.presentation.chat.viewModel.InterestedUsersViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserMessagesFragment : BaseMessagesFragment() {

    // ViewModel for accessing and managing the interested users data
    override val viewModel: InterestedUsersViewModel by viewModels()
    val buyerId = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Defines the navigation action to open a chat for a specific car when an item is clicked.
     * Prepares a bundle with necessary car and user identifiers.
     */
    override val navigateToChat: (Any) -> Unit = { item ->
        if (item is CarWithLastMessage) {
            val bundle = Bundle().apply {
                putString("carId", item.car.id)
                putString("ownerId", item.car.ownerId)
                putString("buyerId", buyerId)
            }
            findNavController().navigate(R.id.action_userMessagesFragment_to_chatFragment, bundle)
        }
    }

    /**
     * Loads messages for cars the current user (buyer) has shown interest in.
     * The ViewModel fetches the cars along with the latest message from each chat.
     */
    override fun loadData() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            viewModel.loadCarsWithUserMessages(currentUserId)
        }
    }
}
