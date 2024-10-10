package com.example.carhive.Presentation.initial.Register.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

            // Llama al ViewModel para guardar la información del usuario
            viewModel.saveSecondPartOfUserData(curp, phoneNumber, voterID)
            // Navega a la siguiente pantalla
            findNavController().navigate(R.id.action_secondRegisterFragment_to_thirdRegisterFragment) // Cambia a tu siguiente fragmento
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
}
