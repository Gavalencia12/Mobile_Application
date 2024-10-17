package com.example.carhive.Presentation.admin.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.Presentation.admin.viewModel.AdminHomeViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentAdminHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminHomeFragment : Fragment() {
    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura el botón de Sign Out
        binding.signOutButton.setOnClickListener {
            viewModel.onLogicClick() // Llama a la lógica en el ViewModel
            findNavController().navigate(R.id.action_userFragment_to_loginFragment) // Navega a LoginFragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
