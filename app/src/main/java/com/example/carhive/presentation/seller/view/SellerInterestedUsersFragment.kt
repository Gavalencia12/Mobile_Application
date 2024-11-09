package com.example.carhive.presentation.seller.view

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.R
import com.example.carhive.data.model.CarWithLastMessage
import com.example.carhive.data.model.UserWithLastMessage
import com.example.carhive.presentation.chat.view.BaseMessagesFragment
import com.example.carhive.presentation.chat.viewModel.InterestedUsersViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SellerInterestedUsersFragment : BaseMessagesFragment() {

    override val viewModel: InterestedUsersViewModel by viewModels()

    // The owner's ID, retrieved from FirebaseAuth
    private val ownerId = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Defines navigation behavior based on the selected item type (User or Car).
     * Navigates to either the chat or interested users screen.
     */
    override val navigateToChat: (Any) -> Unit = { item ->
        if (item is UserWithLastMessage) {
            val bundle = Bundle().apply {
                putString("carId", item.carId)
                putString("buyerId", item.user.id)
                putString("ownerId", ownerId)
            }
            findNavController().navigate(R.id.action_interestedUsersFragment_to_chatFragment, bundle)
        }
        if (item is CarWithLastMessage) {
            val bundle = Bundle().apply {
                putString("carId", item.car.id)
                putString("ownerId", ownerId)
            }
            findNavController().navigate(R.id.action_interestedUsersFragment_to_interestedUsersFragment, bundle)
        }
    }

    /**
     * Loads the interested users for the currently authenticated seller.
     */
    override fun loadData() {
        val sellerId = FirebaseAuth.getInstance().currentUser?.uid
        sellerId?.let {
            viewModel.loadInterestedUsersForSeller(it)
        }
    }
}
