package com.example.carhive.Presentation.admin.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.carhive.R
import com.example.carhive.databinding.FragmentAdminActivateUserBinding

class AdminUserActiveFragment : Fragment() {
    private var _binding: FragmentAdminActivateUserBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inicializa el binding
        _binding = FragmentAdminActivateUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura el click listener para el botón
        binding.bureturn.setOnClickListener {
            Log.d("AdminUserActivateFragment", "Navegando de regreso a AdminHomeFragment")
            findNavController().navigate(R.id.action_adminUserActiveFragment_to_adminHomeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpia el binding
    }
}
