package com.example.carhive.Presentation.initial.Register.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.Presentation.initial.Register.viewModel.SecondRegisterViewModel
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
            } else if (curp.length < 18) {
                setErrorTextAndHint(
                    binding.curpEditText,
                    "CURP must be less than 18 characters"
                )
                errorMessage = "CURP must be less than 18 characters."
            }
            // Validación de campos vacíos
            if (phoneNumber.isEmpty()) {
                setErrorHint(binding.phoneNumberEditText, "Phone number is required")
                errorMessage = "Enter the data correctly to continue."
            }
            // Validación de campos vacíos
            if (voterID.isEmpty()) {
                setErrorHint(binding.voterIDEditText, "Voter ID is required")
                errorMessage = "Enter the data correctly to continue."
            } else if (voterID.length < 18) {
                setErrorTextAndHint(
                    binding.voterIDEditText,
                    "Voter Id must be less than 18 characters"
                )
                errorMessage = "Voter Id must be less than 18 characters."
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
            val termsDialog = TermsAndConditionsDialog()
            termsDialog.show(parentFragmentManager, "TermsAndConditionsDialog")
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
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black))

        // Cambiar el color del texto del CheckBox a negro
        binding.cbTerms.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        // Devuelve el color del texto a su estado normal si ha sido cambiado
        binding.phoneNumberEditText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
    }
}
