package com.example.carhive.presentation.initial.Register.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.carhive.R
import com.example.carhive.databinding.FragmentRegisterFiveBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FiveRegisterFragment : Fragment() {

    private var _binding: FragmentRegisterFiveBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterFiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val getStartedButton: Button = binding.getStartedButton

        // Configurar el botón de "Get Started"
        getStartedButton.setOnClickListener {
            navigateToNext()
        }
    }

    private fun navigateToNext() {
        findNavController().navigate(R.id.action_fiveRegisterFragment_to_userHomeFragment) // Cambia a tu siguiente fragmento
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Evitar fugas de memoria
    }
}
