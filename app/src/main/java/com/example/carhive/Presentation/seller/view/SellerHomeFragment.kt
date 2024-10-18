package com.example.carhive.Presentation.seller.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.Presentation.seller.viewModel.SellerHomeViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentSellerHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SellerHomeFragment : Fragment() {
    private var _binding: FragmentSellerHomeBinding? = null // ViewBinding for the fragment
    private val binding get() = _binding!! // Getter for the binding object

    private val viewModel: SellerHomeViewModel by viewModels() // ViewModel for the fragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerHomeBinding.inflate(inflater, container, false) // Inflate the binding layout
        return binding.root // Return the root view of the binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the Sign Out button
        binding.signOutButton.setOnClickListener {
            viewModel.onLogicClick() // Call logic in the ViewModel
            findNavController().navigate(R.id.action_userFragment_to_loginFragment) // Navigate to LoginFragment
        }

        // Set up the CRUD button to navigate to the CRUD operations screen
        binding.crud.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_crud) // Navigate to CRUD screen
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding reference to avoid memory leaks
    }
}
