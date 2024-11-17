package com.example.carhive.presentation.seller.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.R
import com.example.carhive.data.model.UserWithLastMessage
import com.example.carhive.presentation.chat.adapter.SimpleUsersMessagesAdapter
import com.example.carhive.presentation.chat.view.BaseMessagesFragment
import com.example.carhive.presentation.chat.viewModel.InterestedUsersViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SellerCarUsersFragment : BaseMessagesFragment() {

    override val viewModel: InterestedUsersViewModel by viewModels()
    private var carId: String? = null
    private var ownerId: String? = null
    private lateinit var simpleUsersAdapter: SimpleUsersMessagesAdapter

    /**
     * Initializes the fragment view and retrieves car and owner IDs from arguments.
     * Sets up the recycler view and loads initial data.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve car and owner IDs from arguments
        carId = arguments?.getString("carId")
        ownerId = arguments?.getString("ownerId")

        setupSimpleRecyclerView()
        loadData()
    }

    /**
     * Configures the recycler view with a custom adapter for displaying users interested in a specific car.
     */
    private fun setupSimpleRecyclerView() {
        simpleUsersAdapter = SimpleUsersMessagesAdapter(mutableListOf()) { item ->
            navigateToChat(item)
        }

        binding.recyclerViewInterestedUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = simpleUsersAdapter
        }
    }

    /**
     * Loads interested users for a specific car if carId and ownerId are not null.
     */
    override fun loadData() {
        if (ownerId != null && carId != null) {
            viewModel.loadInterestedUsersForCar(ownerId!!, carId!!)
        }
    }

    /**
     * Defines navigation behavior to the chat screen when a user item is selected.
     */
    override val navigateToChat: (Any) -> Unit = { item ->
        if (item is UserWithLastMessage) {
            val bundle = Bundle().apply {
                putString("carId", carId)
                putString("buyerId", item.user.id)
                putString("ownerId", ownerId)
            }
            findNavController().navigate(R.id.action_carUsersFragment_to_chatFragment, bundle)
        }
    }

    /**
     * Observes data from the ViewModel and updates the adapter with filtered users for the specific car.
     */
    override fun observeViewModel() {
        viewModel.usersWithMessages.observe(viewLifecycleOwner) { usersWithMessages ->
            // Filter users specific to the carId
            val filteredUsers = usersWithMessages.interestedUsers.filter { it.carId == carId }

            // Update adapter with filtered user list
            simpleUsersAdapter.updateData(filteredUsers)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}
