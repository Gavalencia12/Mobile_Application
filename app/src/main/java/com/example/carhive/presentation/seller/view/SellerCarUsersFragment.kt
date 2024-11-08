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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        carId = arguments?.getString("carId")
        ownerId = arguments?.getString("ownerId")

        setupSimpleRecyclerView()
        loadData()
    }

    private fun setupSimpleRecyclerView() {
        simpleUsersAdapter = SimpleUsersMessagesAdapter(mutableListOf()) { item ->
            navigateToChat(item)
        }

        binding.recyclerViewInterestedUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = simpleUsersAdapter
        }
    }

    override fun loadData() {
        if (ownerId != null && carId != null) {
            viewModel.loadInterestedUsersForCar(ownerId!!, carId!!)
        }
    }

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

    // Observa los datos de usuarios interesados y actualiza el adaptador
    override fun observeViewModel() {
        viewModel.usersWithMessages.observe(viewLifecycleOwner) { usersWithMessages ->
            // Filtra la lista para incluir solo los usuarios cuyo carId coincida con el argumento carId
            val filteredUsers = usersWithMessages.interestedUsers.filter { it.carId == carId }

            // Actualiza el adaptador con la lista filtrada
            simpleUsersAdapter.updateData(filteredUsers)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

}
