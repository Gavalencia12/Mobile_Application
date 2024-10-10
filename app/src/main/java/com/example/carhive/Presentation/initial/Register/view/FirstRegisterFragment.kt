package com.example.carhive.Presentation.initial.Register.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.Presentation.initial.Register.viewModel.FirstRegisterViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentRegisterFirstBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FirstRegisterFragment : Fragment() {

    private var _binding: FragmentRegisterFirstBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FirstRegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura el botón de siguiente
        binding.nextButton.setOnClickListener {
            val firstName = binding.firstNameEditText.text.toString()
            val lastName = binding.lastNameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            // Llama al ViewModel para guardar la información del usuario
            viewModel.saveFirstPartOfUserData(firstName, lastName, email, password)
            // Navega a la siguiente pantalla
            findNavController().navigate(R.id.action_firstRegisterFragment_to_secondRegisterFragment)
        }

        // Configura el enlace de inicio de sesión
        binding.loginLink.setOnClickListener {
            findNavController().navigate(R.id.action_firstRegisterFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Evitar fugas de memoria
    }
}
