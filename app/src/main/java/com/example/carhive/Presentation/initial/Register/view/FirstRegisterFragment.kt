package com.example.carhive.Presentation.initial.Register.view

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.content.ContextCompat
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observa el estado de visibilidad de la contraseña
        viewModel.isPasswordVisible.observe(viewLifecycleOwner) { isVisible ->
            togglePasswordVisibility(isVisible, binding.passwordEditText)
        }

        // Observa el estado de visibilidad de la confirmación de contraseña
        viewModel.isConfirmPasswordVisible.observe(viewLifecycleOwner) { isVisible ->
            togglePasswordVisibility(isVisible, binding.confirmPasswordEditText)
        }

        // Configura el evento de clic para alternar la visibilidad de la contraseña
        binding.passwordEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && event.rawX >= binding.passwordEditText.right - binding.passwordEditText.compoundDrawables[2].bounds.width()) {
                viewModel.togglePasswordVisibility()
                true
            } else {
                false
            }
        }

        // Configura el evento de clic para alternar la visibilidad de la confirmación de contraseña
        binding.confirmPasswordEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && event.rawX >= binding.confirmPasswordEditText.right - binding.confirmPasswordEditText.compoundDrawables[2].bounds.width()) {
                viewModel.toggleConfirmPasswordVisibility()
                true
            } else {
                false
            }
        }

        // Configura el botón de siguiente
        binding.nextButton.setOnClickListener {
            val firstName = binding.firstNameEditText.text.toString().trim()
            val lastName = binding.lastNameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
            val termsResult = binding.cbTerms.isChecked

            // Limpia los mensajes de error previos
            clearErrors()

            var errorMessage = ""
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                errorMessage += "Enter the data correctly to continue."
                if (firstName.isEmpty()) {
                    setErrorHint(binding.firstNameEditText, "First name is required")
                }
                if (lastName.isEmpty()) {
                    setErrorHint(binding.lastNameEditText, "Last name is required")
                }
                if (email.isEmpty()) {
                    setErrorHint(binding.emailEditText, "Email is required")
                } else if (!isValidEmail(email)) {
                    setErrorTextAndHint(binding.emailEditText, "Invalid email format")
                    errorMessage = "Invalid email format."
                }
                if (password.isEmpty()) {
                    setErrorHint(binding.passwordEditText, "Password is required")
                } else if (password.length < 6) {
                    setErrorTextAndHint(
                        binding.passwordEditText,
                        "Password must be at least 6 characters"
                    )
                    setErrorTextAndHint(
                        binding.confirmPasswordEditText,
                        "Password must be at least 6 characters"
                    )
                    errorMessage = "Password must be at least 6 characters."
                }
                if (confirmPassword.isEmpty()) {
                    setErrorHint(binding.confirmPasswordEditText, "Confirm password is required")
                } else if (confirmPassword != password) {
                    setErrorTextAndHint(binding.confirmPasswordEditText, "Passwords do not match")
                    errorMessage = "Passwords do not match."
                }
                setErrorButton(binding.cbTerms)
            } else if (!termsResult) {
                errorMessage = "Terms is not selected."
                setErrorButton(binding.cbTerms)
            }

            // Si hay errores, muestra el mensaje en la parte superior
            if (errorMessage.isNotEmpty()) {
                binding.instruction.text = errorMessage.trim()
                binding.instruction.visibility = View.VISIBLE
            } else {
                // Si no hay errores, navega a la siguiente pantalla
                viewModel.saveFirstPartOfUserData(firstName, lastName, email, password, termsResult)
                findNavController().navigate(R.id.action_firstRegisterFragment_to_secondRegisterFragment)
            }
        }

        // Configura el enlace de inicio de sesión
        binding.loginLink.setOnClickListener {
            findNavController().navigate(R.id.action_firstRegisterFragment_to_loginFragment)
        }
    }

    private fun togglePasswordVisibility(isVisible: Boolean, editText: EditText) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT
            editText.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_passw,
                0,
                R.drawable.ic_visibility_on,
                0
            )
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            editText.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_passw,
                0,
                R.drawable.ic_visibility_off,
                0
            )
        }
        editText.setSelection(editText.text.length)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Evitar fugas de memoria
    }

    // Función para validar el formato de correo electrónico
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Función para cambiar el hint temporalmente a rojo
    private fun setErrorHint(editText: EditText, message: String) {
        editText.setHintTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.red
            )
        ) // Cambia el hint a rojo
        editText.hint = message // Cambia el hint temporalmente
    }

    // Función para cambiar el hint temporalmente a rojo
    private fun setErrorButton(editText: CheckBox) {
        // Cambiar el color del botón del CheckBox a rojo
        binding.cbTerms.buttonTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))

        // Cambiar el color del texto del CheckBox a rojo
        binding.cbTerms.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
    }

    // Función para cambiar el texto y hint a rojo
    private fun setErrorTextAndHint(editText: EditText, message: String) {
        editText.setHintTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.red
            )
        ) // Cambia el hint a rojo
        editText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.red
            )
        ) // Cambia el texto a rojo
        editText.hint = message // Cambia el hint temporalmente
    }

    // Función para limpiar los errores anteriores
    private fun clearErrors() {
        binding.instruction.visibility = View.GONE
        // Usar ContextCompat para obtener colores de forma segura y compatible con versiones antiguas
        binding.firstNameEditText.setHintTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.gray
            )
        )
        binding.lastNameEditText.setHintTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.gray
            )
        )
        binding.emailEditText.setHintTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.gray
            )
        )
        binding.passwordEditText.setHintTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.gray
            )
        )
        binding.confirmPasswordEditText.setHintTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.gray
            )
        )

        // Cambiar el color del botón del CheckBox a rojo
        binding.cbTerms.buttonTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black))

        // Cambiar el color del texto del CheckBox a rojo
        binding.cbTerms.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        // Devuelve el color del texto de los campos de entrada a negro
        binding.emailEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.passwordEditText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.confirmPasswordEditText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )

    }

}
