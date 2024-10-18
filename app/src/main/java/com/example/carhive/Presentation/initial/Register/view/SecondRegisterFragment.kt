package com.example.carhive.Presentation.initial.Register.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

            // Limpia los mensajes de error previos
            clearErrors()

            var errorMessage = ""

            // Validación de campos vacíos
            if(curp.isEmpty() || phoneNumber.isEmpty() || voterID.isEmpty()){
                errorMessage = "Enter the data correctly to continue."
                if (curp.isEmpty()) {
                    setErrorHint(binding.curpEditText, "CURP is required")
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
                }
                // Validación de campos vacíos
                if (voterID.isEmpty()) {
                    setErrorHint(binding.voterIDEditText, "Voter ID is required")
                } else if (voterID.length < 18) {
                    setErrorTextAndHint(
                        binding.voterIDEditText,
                        "Voter Id must be less than 18 characters"
                    )
                    errorMessage = "Voter Id must be less than 18 characters."
                }
            }




            // Si hay errores, muestra el mensaje en la parte superior
            if (errorMessage.isNotEmpty()) {
                binding.instruction.text = errorMessage.trim()
                binding.instruction.visibility = View.VISIBLE
            } else {
                // Si no hay errores, navega a la siguiente pantalla
                viewModel.saveSecondPartOfUserData(curp, phoneNumber, voterID)
                findNavController().navigate(R.id.action_secondRegisterFragment_to_thirdRegisterFragment)
            }
        }

        // Configura el enlace para regresar
        binding.goBackLink.setOnClickListener {
            findNavController().navigate(R.id.action_secondRegisterFragment_to_firstRegisterFragment) // Cambia a tu fragmento anterior
        }
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
        editText.setHintTextColor(ContextCompat.getColor(requireContext(),R.color.red)) // Cambia el hint a rojo
        editText.hint = message // Cambia el hint temporalmente
    }

    // Función para cambiar el texto y hint a rojo
    private fun setErrorTextAndHint(editText: EditText, message: String) {
        editText.setHintTextColor(ContextCompat.getColor(requireContext(),R.color.red)) // Cambia el hint a rojo
        editText.setTextColor(ContextCompat.getColor(requireContext(),R.color.red)) // Cambia el texto a rojo
        editText.hint = message // Cambia el hint temporalmente
    }

    // Función para limpiar los errores anteriores
    private fun clearErrors() {
        binding.instruction.visibility = View.GONE
        binding.curpEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
        binding.phoneNumberEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
        binding.voterIDEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.gray))

        // Devuelve el color del texto a su estado normal si ha sido cambiado
        binding.phoneNumberEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }
}
