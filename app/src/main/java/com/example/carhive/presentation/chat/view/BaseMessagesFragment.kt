package com.example.carhive.presentation.chat.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.databinding.FragmentInterestedUsersBinding
import com.example.carhive.presentation.chat.adapter.UsersMessagesAdapter
import com.example.carhive.presentation.chat.viewModel.InterestedUsersViewModel

abstract class BaseMessagesFragment : Fragment() {

    protected lateinit var binding: FragmentInterestedUsersBinding
    protected lateinit var messagesAdapter: UsersMessagesAdapter

    abstract val viewModel: InterestedUsersViewModel
    abstract val navigateToChat: (Any) -> Unit
    abstract fun loadData()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInterestedUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        loadData()  // Llama a la función para cargar datos
    }

    private fun setupRecyclerView() {
        messagesAdapter = UsersMessagesAdapter(mutableListOf(), mutableListOf()) { item ->
            navigateToChat(item)
        }

        binding.recyclerViewInterestedUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = messagesAdapter
        }
    }

    protected open fun observeViewModel() {
        viewModel.usersWithMessages.observe(viewLifecycleOwner) { usersWithMessages ->
            val interestedUsers = usersWithMessages.interestedUsers.take(10)
            val cars = usersWithMessages.cars
            Log.i("angel", "$interestedUsers")
            Log.i("angel", "$cars")
            messagesAdapter.updateData(interestedUsers, cars)
        }

        viewModel.carsWithMessages.observe(viewLifecycleOwner) { carsWithMessages ->
            val recentChats = carsWithMessages.take(10) // Los 10 chats más recientes
            messagesAdapter.updateData(recentChats, carsWithMessages)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                // Mostrar mensaje de error, como Toast
            }
        }
    }

}
