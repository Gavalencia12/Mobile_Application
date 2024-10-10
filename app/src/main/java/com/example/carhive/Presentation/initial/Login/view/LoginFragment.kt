package com.example.carhive.Presentation.initial.Login.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.Presentation.initial.Login.viewModel.LoginViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // Configurando el botón de login
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            viewModel.onLoginClick(email, password) { destination ->
                // Navegar según el rol
                when (destination) {
                    "Admin" -> findNavController().navigate(R.id.action_loginFragment_to_adminFragment)
                    "Seller" -> findNavController().navigate(R.id.action_loginFragment_to_sellerFragment)
                    "User" -> findNavController().navigate(R.id.action_loginFragment_to_userhomeFragment)
                    else -> findNavController().navigate(R.id.action_loginFragment_to_loginFragment)
                }
            }
        }

        // Configurando el texto de "Forgot your Password"
        binding.forgotPasswordText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_recoveryPasswordFragment)
        }

        // Configurando el texto de "Register Now"
        binding.registerNowText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_firstRegisterFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
