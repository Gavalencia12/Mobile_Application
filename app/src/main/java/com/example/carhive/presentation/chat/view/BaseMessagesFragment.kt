package com.example.carhive.presentation.chat.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.databinding.FragmentInterestedUsersBinding
import com.example.carhive.presentation.chat.adapter.AdminMessagesAdapter
import com.example.carhive.presentation.chat.adapter.SimpleUsersMessagesAdapter
import com.example.carhive.presentation.chat.adapter.UsersMessagesAdapter
import com.example.carhive.presentation.chat.viewModel.InterestedUsersViewModel

/**
 * Base fragment for displaying messages related to interested users and cars.
 * Manages the setup and observation of the view model to display messages in a RecyclerView.
 */
abstract class BaseMessagesFragment : Fragment() {

    protected lateinit var binding: FragmentInterestedUsersBinding
    protected lateinit var messagesAdapter: UsersMessagesAdapter
    protected lateinit var messagesAdminAdapter: AdminMessagesAdapter

    // Abstract properties and methods for child fragments to implement
    abstract val viewModel: InterestedUsersViewModel
    abstract val navigateToChat: (Any) -> Unit
    abstract fun loadData()

    /**
     * Inflates the fragment layout using ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInterestedUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Sets up the RecyclerView and observes the ViewModel after the view is created.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        loadData()  // Calls the abstract function to load data in the fragment
    }

    /**
     * Initializes the RecyclerView and its adapter, setting up the item click listener to navigate to chat.
     */
    protected open fun setupRecyclerView() {
        messagesAdapter = UsersMessagesAdapter(mutableListOf(), mutableListOf()) { item ->
            navigateToChat(item)
        }

        messagesAdminAdapter = AdminMessagesAdapter(mutableListOf(), mutableListOf()) { item ->
            navigateToChat(item)
        }

        binding.recyclerViewInterestedUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = messagesAdapter
        }

    }

    /**
     * Observes LiveData from the ViewModel to update the RecyclerView with messages.
     * Can be overridden in child classes to adjust specific behavior.
     */
    protected open fun observeViewModel() {
        viewModel.usersWithMessages.observe(viewLifecycleOwner) { usersWithMessages ->
            val interestedUsers = usersWithMessages.interestedUsers.take(5)
            val cars = usersWithMessages.cars
            messagesAdapter.updateData(interestedUsers, cars)
        }

        viewModel.carsWithMessages.observe(viewLifecycleOwner) { carsWithMessages ->
            val recentChats = carsWithMessages.take(5) // Gets the 5 most recent chats
            messagesAdapter.updateData(recentChats, carsWithMessages)
        }

        viewModel.supportUserData.observe(viewLifecycleOwner) { supportData ->
            val buyers = supportData.buyers
            val sellers = supportData.sellers
            messagesAdminAdapter.updateData(buyers, sellers)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                // Display error message, such as with a Toast
            }
        }
    }

}
