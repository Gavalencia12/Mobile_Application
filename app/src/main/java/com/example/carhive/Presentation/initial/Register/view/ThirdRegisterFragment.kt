package com.example.carhive.Presentation.initial.Register.view

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.Presentation.initial.Register.viewModel.ThirdRegisterViewModel
import com.example.carhive.R
import dagger.hilt.android.AndroidEntryPoint
import coil.load
import com.example.carhive.databinding.FragmentRegisterThirdBinding

@AndroidEntryPoint
class ThirdRegisterFragment : Fragment() {

    private var _binding: FragmentRegisterThirdBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ThirdRegisterViewModel by viewModels()
    private lateinit var imageUri: Uri
    private lateinit var launcher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterThirdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageUri = uri
            }
            binding.selectedImageView.visibility = View.VISIBLE
            binding.selectedImageView.load(uri) // Carga la imagen seleccionada
            binding.finishRegistrationButton.isEnabled = uri != null // Habilitar el botón si hay imagen
        }

        binding.chooseImageButton.setOnClickListener {
            launcher.launch("image/*") // Lanza el selector de imágenes
        }

        binding.finishRegistrationButton.setOnClickListener {
            imageUri.let { uri ->
                viewModel.uploadProfileImage(uri) // Llama al método de carga de la imagen
                findNavController().navigate(R.id.action_thirdRegisterFragment_to_fortRegisterFragment) // Cambia a tu siguiente fragmento
            }
        }

        binding.backToSecondPartLink.setOnClickListener {
            findNavController().navigate(R.id.action_thirdRegisterFragment_to_secondRegisterFragment) // Cambia a tu fragmento anterior
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Evitar fugas de memoria
    }
}
