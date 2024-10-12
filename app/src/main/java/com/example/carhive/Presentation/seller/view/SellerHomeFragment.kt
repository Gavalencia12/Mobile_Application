package com.example.carhive.Presentation.seller.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.Presentation.seller.viewModel.SellerHomeViewModel
import com.example.carhive.Presentation.user.viewModel.UserViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentSellerHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SellerHomeFragment : Fragment() {
    private var _binding: FragmentSellerHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SellerHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura el botón de Sign Out
        binding.signOutButton.setOnClickListener {
            viewModel.onLogicClick() // Llama a la lógica en el ViewModel
            findNavController().navigate(R.id.action_userFragment_to_loginFragment) // Navega a LoginFragment
        }

        binding.crud.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_crud)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}