package com.example.carhive.presentation.initial.Register.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.presentation.initial.Register.viewModel.FirstRegisterViewModel
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

        addTextChangeListener(binding.firstNameEditText, R.drawable.ic_person)
        addTextChangeListener(binding.lastNameEditText, R.drawable.ic_person)
        addTextChangeListener(binding.emailEditText, R.drawable.ic_email)
        addTextChangeListener(binding.passwordEditText, R.drawable.ic_passw)
        addTextChangeListener(binding.confirmPasswordEditText, R.drawable.ic_passw)

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
            if (event.action == MotionEvent.ACTION_UP && event.rawX >= binding.passwordEditText.right - binding.passwordEditText.compoundDrawables[2].bounds.width() - 40) {
                viewModel.togglePasswordVisibility()
                true
            } else {
                false
            }
        }

        // Configura el evento de clic para alternar la visibilidad de la confirmación de contraseña
        binding.confirmPasswordEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && event.rawX >= binding.confirmPasswordEditText.right - binding.confirmPasswordEditText.compoundDrawables[2].bounds.width() - 40) {
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
            val note = binding.note.text.toString().trim()

            // Limpia los mensajes de error previos
            clearErrors()

            var errorMessage = ""

            if (firstName.isEmpty()) {
                setErrorHint(binding.firstNameEditText, "First name is required")
                errorMessage = "Enter the data correctly to continue."
                setErrorDrawable(binding.firstNameEditText, R.drawable.ic_person)
            }
            if (lastName.isEmpty()) {
                setErrorHint(binding.lastNameEditText, "Last name is required")
                errorMessage = "Enter the data correctly to continue."
                setErrorDrawable(binding.lastNameEditText, R.drawable.ic_person)
            }
            if (email.isEmpty()) {
                setErrorHint(binding.emailEditText, "Email is required")
                errorMessage = "Enter the data correctly to continue."
                setErrorDrawable(binding.emailEditText, R.drawable.ic_email)
            } else if (!isValidEmail(email)) {
                setErrorTextAndHint(binding.emailEditText, "Invalid email format")
                errorMessage = "Invalid email format."
                setErrorDrawable(binding.emailEditText, R.drawable.ic_email)
            }
            if (password.isEmpty()) {
                setErrorHint(binding.passwordEditText, "Password is required")
                errorMessage = "Enter the data correctly to continue."
                setErrorDrawable(binding.passwordEditText, R.drawable.ic_passw)
            } else if (!isPasswordSecure(password)) {
                setErrorTextAndHint(
                    binding.passwordEditText,
                    "Password is not secure."
                )
                errorMessage = "Password is not secure."
                setErrorDrawable(binding.passwordEditText, R.drawable.ic_passw)

                binding.note.apply {
                    visibility = View.VISIBLE
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.red)) // Cambia el color del texto a rojo
                }
            }

            if (confirmPassword.isEmpty()) {
                setErrorHint(binding.confirmPasswordEditText, "Confirm password is required")
                errorMessage = "Enter the data correctly to continue."
                setErrorDrawable(binding.confirmPasswordEditText, R.drawable.ic_passw)
            } else if (confirmPassword != password) {
                setErrorTextAndHint(binding.confirmPasswordEditText, "Passwords do not match")
                errorMessage = "Passwords do not match."
                setErrorDrawable(binding.confirmPasswordEditText, R.drawable.ic_passw)
            }
            // Si hay errores, muestra el mensaje en la parte superior
            if (errorMessage.isNotEmpty()) {
                binding.instruction.apply {
                    text = errorMessage.trim()
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
            } else {
                // Si no hay errores, navega a la siguiente pantalla
                viewModel.saveFirstPartOfUserData(firstName, lastName, email, password)
                findNavController().navigate(R.id.action_firstRegisterFragment_to_secondRegisterFragment)
            }
        }
        // Configura el enlace de inicio de sesión
        binding.loginLink.setOnClickListener {
            findNavController().navigate(R.id.action_firstRegisterFragment_to_loginFragment)
        }
        binding.btnPrevious.setOnClickListener{
            findNavController().navigate(R.id.action_firstRegisterFragment_to_loginFragment)
        }

    }

    // Función para establecer un drawable tintado de color rojo cuando hay un error
    private fun setErrorDrawable(editText: EditText, drawableId: Int) {
        val drawableStart = ContextCompat.getDrawable(requireContext(), drawableId)?.mutate()
        drawableStart?.let {
            DrawableCompat.setTint(it, ContextCompat.getColor(requireContext(), R.color.red))
            val drawables = editText.compoundDrawables
            editText.setCompoundDrawablesWithIntrinsicBounds(drawableStart, drawables[1], drawables[2], drawables[3])
        }
    }

    // Función para restablecer el drawable a su color original
    private fun resetDrawableColor(editText: EditText, drawableId: Int) {
        val drawableStart = ContextCompat.getDrawable(requireContext(), drawableId)?.mutate()
        drawableStart?.let {
            val drawables = editText.compoundDrawables
            editText.setCompoundDrawablesWithIntrinsicBounds(drawableStart, drawables[1], drawables[2], drawables[3])
        }
    }

    private fun addTextChangeListener(editText: EditText, drawableId: Int){
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Cuando el texto cambia, cambia el color del drawable a gris oscuro
                resetDrawableColor(editText, drawableId)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
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
        val customFont = ResourcesCompat.getFont(requireContext(), R.font.inter_semibold)
        editText.typeface = customFont
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

        // Devuelve el color del texto de los campos de entrada a negro
        binding.emailEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
        binding.passwordEditText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.gray
            )
        )
        binding.confirmPasswordEditText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.gray
            )
        )

    }

    // Función para validar la seguridad de la contraseña
    private fun isPasswordSecure(password: String): Boolean {
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }
        val isValidLength = password.length >= 8

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar && isValidLength
    }


}
