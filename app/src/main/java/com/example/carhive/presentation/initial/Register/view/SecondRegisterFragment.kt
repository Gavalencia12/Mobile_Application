package com.example.carhive.presentation.initial.Register.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.presentation.initial.Register.viewModel.SecondRegisterViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentRegisterSecondBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SecondRegisterFragment : Fragment() {

    private var _binding: FragmentRegisterSecondBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SecondRegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addTextChangeListener(binding.curpEditText, R.drawable.ic_person)
        addTextChangeListener(binding.phoneNumberEditText, R.drawable.ic_phone)
        addTextChangeListener(binding.voterIDEditText, R.drawable.ic_person)

        // Configura el botón de siguiente
        binding.nextButton.setOnClickListener {
            val curp = binding.curpEditText.text.toString()
            val phoneNumber = binding.phoneNumberEditText.text.toString()
            val voterID = binding.voterIDEditText.text.toString()
            val termsResult = binding.cbTerms.isChecked

            // Limpia los mensajes de error previos
            clearErrors()

            var errorMessage = ""

            // Validación de campos vacíos
            if (curp.isEmpty()) {
                setErrorHint(binding.curpEditText, "CURP is required")
                errorMessage = "Enter the data correctly to continue."
                setErrorDrawable(binding.curpEditText, R.drawable.ic_person)
            } else if (curp.length < 18) {
                setErrorTextAndHint(
                    binding.curpEditText,
                    "CURP must be less than 18 characters"
                )
                errorMessage = "CURP must be less than 18 characters."
                setErrorDrawable(binding.curpEditText, R.drawable.ic_person)
            }
            // Validación de campos vacíos
            if (phoneNumber.isEmpty()) {
                setErrorHint(binding.phoneNumberEditText, "Phone number is required")
                errorMessage = "Enter the data correctly to continue."
                setErrorDrawable(binding.phoneNumberEditText, R.drawable.ic_phone)
            }
            // Validación de campos vacíos
            if (voterID.isEmpty()) {
                setErrorHint(binding.voterIDEditText, "Voter ID is required")
                errorMessage = "Enter the data correctly to continue."
                setErrorDrawable(binding.voterIDEditText, R.drawable.ic_person)
            } else if (voterID.length < 18) {
                setErrorTextAndHint(
                    binding.voterIDEditText,
                    "Voter Id must be less than 18 characters"
                )
                errorMessage = "Voter Id must be less than 18 characters."
                setErrorDrawable(binding.voterIDEditText, R.drawable.ic_person)
            } else if (!termsResult) {
                errorMessage = "Terms is not selected."
                setErrorButton(binding.cbTerms)
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
                val termsResult = true
                viewModel.saveSecondPartOfUserData(curp, phoneNumber, voterID, termsResult)
                findNavController().navigate(R.id.action_secondRegisterFragment_to_thirdRegisterFragment)
            }
        }

        // Configura el enlace para regresar
        binding.goBackLink.setOnClickListener {
            findNavController().navigate(R.id.action_secondRegisterFragment_to_firstRegisterFragment) // Cambia a tu fragmento anterior
        }

        // Mostrar el modal de términos y condiciones cuando se hace clic en el TextView
        binding.termsTextView.setOnClickListener {
            findNavController().navigate(R.id.action_secondRegisterFragment_to_termsUserFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Evitar fugas de memoria
    }

    // Función para cambiar el hint temporalmente a rojo
    private fun setErrorButton(editText: CheckBox) {
        // Cambiar el color del botón del CheckBox a rojo
        binding.cbTerms.buttonTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))

        // Cambiar el color del texto del CheckBox a rojo
        binding.cbTerms.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
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
        binding.curpEditText.setHintTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.gray
            )
        )
        binding.phoneNumberEditText.setHintTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.gray
            )
        )
        binding.voterIDEditText.setHintTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.gray
            )
        )

        // Cambiar el color del botón del CheckBox a rojo
        binding.cbTerms.buttonTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue))

        // Cambiar el color del texto del CheckBox a negro
        binding.cbTerms.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))

        // Devuelve el color del texto a su estado normal si ha sido cambiado
        binding.phoneNumberEditText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.gray
            )
        )
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
}
