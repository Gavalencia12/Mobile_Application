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

    override val viewModel: InterestedUsersViewModel by viewModels()
    val buyerId = FirebaseAuth.getInstance().currentUser?.uid

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

    override fun loadData() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            viewModel.loadCarsWithUserMessages(currentUserId)
        }
    }
}
