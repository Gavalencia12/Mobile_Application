package com.example.carhive.Presentation.initial.Register.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.carhive.Presentation.initial.Register.viewModel.ThirdRegisterViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentRegisterThirdBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class ThirdRegisterFragment : Fragment() {

    private var _binding: FragmentRegisterThirdBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ThirdRegisterViewModel by viewModels()
    private var imageUri: Uri? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterThirdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.finishRegistrationButton.isEnabled = false

        // Initialize the camera launcher with intent for front camera
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                imageUri?.let { uri ->
                    binding.selectedImageView.visibility = View.VISIBLE
                    binding.selectedImageView.load(uri) {
                        transformations(coil.transform.CircleCropTransformation())
                    }
                }
                binding.finishRegistrationButton.isEnabled = true
            }
        }

        binding.chooseImageButton.setOnClickListener {
            imageUri = createImageUri(requireContext())
            imageUri?.let { uri ->
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    // Request front camera
                    putExtra("android.intent.extras.CAMERA_FACING", 1) // For older devices
                    putExtra("android.intent.extras.LENS_FACING_FRONT", 1) // For newer devices
                    putExtra("android.intent.extra.USE_FRONT_CAMERA", true) // Extra fallback
                }
                cameraLauncher.launch(cameraIntent)
            }
        }

        binding.finishRegistrationButton.setOnClickListener {
            imageUri?.let { uri ->
                viewModel.uploadProfileImage(uri)
                findNavController().navigate(R.id.action_thirdRegisterFragment_to_fortRegisterFragment)
            }
        }

        binding.backToSecondPartLink.setOnClickListener {
            findNavController().navigate(R.id.action_thirdRegisterFragment_to_secondRegisterFragment)
        }
    }

    private fun createImageUri(context: Context): Uri? {
        val image = File(context.filesDir, "camera_photo.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            image
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
