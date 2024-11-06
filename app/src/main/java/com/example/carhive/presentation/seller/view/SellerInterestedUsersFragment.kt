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

    private val ownerId = FirebaseAuth.getInstance().currentUser?.uid

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

    override fun loadData() {
        val sellerId = FirebaseAuth.getInstance().currentUser?.uid
        if (sellerId != null) {
            viewModel.loadInterestedUsersForSeller(sellerId)
        }
    }
}
