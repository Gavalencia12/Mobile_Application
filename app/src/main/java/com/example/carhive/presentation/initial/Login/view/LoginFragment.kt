package com.example.carhive.presentation.initial.Login.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.carhive.presentation.initial.Login.viewModel.LoginViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    // Variable para saber si la contraseña está visible
    private var isPasswordVisible = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // Configurar el evento de clic en el icono de la contraseña
        binding.passwordEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.passwordEditText.right - binding.passwordEditText.compoundDrawables[2].bounds.width() - 40)) {
                    // Cambiar la visibilidad de la contraseña
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Configurando el botón de login
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            viewModel.onLoginClick(email, password,
                // Navegar según el rol
                { destination ->
                    when (destination) {
                        "Admin" -> findNavController().navigate(R.id.action_loginFragment_to_adminFragment)
                        "Seller" -> findNavController().navigate(R.id.action_loginFragment_to_sellerFragment)
                        "User" -> findNavController().navigate(R.id.action_loginFragment_to_userhomeFragment)
                        else -> findNavController().navigate(R.id.action_loginFragment_to_loginFragment)
                    }
                },
                // Navegar a la pantalla de verificación de correo
                {
                    findNavController().navigate(R.id.action_loginFragment_to_verifyEmailFragment)
                })
        }

        // Observar los errores de login
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginError.collectLatest { error ->
                error?.let {
                    binding.instruction.apply {
                        text = it
                        visibility = View.VISIBLE
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                        setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_error, // Left drawable (ícono de error)
                            0,
                            0,
                            0
                        )
                        compoundDrawablePadding = 2 // Space between the icon and the text
                    }
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

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Si la contraseña está visible, ocultarla
            binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_passw, 0, R.drawable.ic_visibility_off, 0)
        } else {
            // Si la contraseña está oculta, mostrarla
            binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT
            binding.passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_passw, 0, R.drawable.ic_visibility_on, 0)
        }
        isPasswordVisible = !isPasswordVisible

        // Mover el cursor al final del texto
        binding.passwordEditText.setSelection(binding.passwordEditText.text.length)
        val customFont = ResourcesCompat.getFont(requireContext(), R.font.inter_semibold)
        binding.passwordEditText.typeface = customFont
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
