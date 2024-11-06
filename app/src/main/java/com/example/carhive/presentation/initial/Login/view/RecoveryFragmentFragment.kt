package com.example.carhive.presentation.initial.Login.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.carhive.presentation.initial.Login.viewModel.EmailState
import com.example.carhive.presentation.initial.Login.viewModel.RecoveryPasswordViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentRecoveryPasswordBinding // Asegúrate de que el nombre del archivo es correcto
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecoveryPasswordFragment : Fragment() {

    private var _binding: FragmentRecoveryPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecoveryPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecoveryPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura el botón para enviar el correo de recuperación
        binding.sendButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            if (email.isNotEmpty() && isValidEmail(email)) {
                viewModel.sendPasswordResetEmail(email) // Llama al ViewModel para enviar el correo
            } else {
                binding.errorMessageTextView.visibility = View.VISIBLE
                binding.errorMessageTextView.text = "Por favor, ingresa un correo válido"
            }
        }

        // Recoge el estado del ViewModel y actualiza la UI en consecuencia
        lifecycleScope.launch {
            viewModel.emailState.collectLatest { emailState ->
                when (emailState) {
                    is EmailState.Loading -> {
                        binding.loadingProgressBar.visibility = View.VISIBLE
                        binding.successMessageTextView.visibility = View.GONE
                        binding.errorMessageTextView.visibility = View.GONE
                    }
                    is EmailState.Success -> {
                        binding.loadingProgressBar.visibility = View.GONE
                        binding.successMessageTextView.visibility = View.VISIBLE
                        binding.successMessageTextView.text = "Correo enviado con éxito"
                        // Navega de vuelta al login después de un retraso
                        view.postDelayed({
                            navigateToLogin()
                        }, 2000)
                    }
                    is EmailState.Error -> {
                        binding.loadingProgressBar.visibility = View.GONE
                        binding.errorMessageTextView.visibility = View.VISIBLE
                        binding.errorMessageTextView.text = emailState.message
                    }
                    else -> {
                        binding.loadingProgressBar.visibility = View.GONE
                    }
                }
            }
        }
        binding.goBackLink.setOnClickListener{
            findNavController().navigate(R.id.action_recoveryPasswordFragment_to_loginFragment)
        }
    }

    // Función para validar el formato de correo electrónico
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_recoveryPasswordFragment_to_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Evitar fugas de memoria
    }
}
