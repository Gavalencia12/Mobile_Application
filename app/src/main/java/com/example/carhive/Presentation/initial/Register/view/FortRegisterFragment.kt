package com.example.carhive.Presentation.initial.Register.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.R
import com.example.carhive.databinding.FragmentRegisterFourthBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class FortRegisterFragment : Fragment() {

    private var _binding: FragmentRegisterFourthBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FortRegisterViewModel by viewModels()

    private var isEmailVerified = false
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterFourthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailVerificationStatus: TextView = binding.emailVerificationStatus
        val loadingIndicator: ProgressBar = binding.loadingIndicator
        val continueButton: Button = binding.continueButton

        // Observa el LiveData para verificar el estado del correo electrónico
        viewModel.isEmailVerified.observe(viewLifecycleOwner) { isVerified ->
            isEmailVerified = isVerified
            updateUI(isVerified)
        }

        // Inicia la verificación del correo electrónico
        coroutineScope.launch {
            while (!isEmailVerified) {
                delay(3000) // Verifica cada 3 segundos
                viewModel.checkEmailVerification()
            }
        }

        continueButton.setOnClickListener {
            if (isEmailVerified) {
                navigateToNext()
            }
        }
    }

    private fun updateUI(isVerified: Boolean) {
        if (isVerified) {
            binding.emailVerificationStatus.text = "Correo verificado con éxito"
            binding.emailVerificationStatus.visibility = View.VISIBLE
            binding.loadingIndicator.visibility = View.GONE
            binding.continueButton.isEnabled = true
        } else {
            binding.emailVerificationStatus.visibility = View.GONE
            binding.loadingIndicator.visibility = View.VISIBLE
            binding.continueButton.isEnabled = false
        }
    }

    private fun navigateToNext() {
        findNavController().navigate(R.id.action_fortRegisterFragment_to_fiveRegisterFragment) // Cambia a tu siguiente fragmento
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Evitar fugas de memoria
        job.cancel() // Cancelar el job de corutina al destruir la vista
    }
}
