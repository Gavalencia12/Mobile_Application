package com.example.carhive.presentation.initial.Register.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.presentation.initial.Register.viewModel.ThirdRegisterViewModel
import coil.load
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
        Log.d("ThirdRegisterFragment", "onCreateView: View created")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ThirdRegisterFragment", "onViewCreated: Fragment view created")

        binding.finishRegistrationButton.isEnabled = false

        // Initialize the camera launcher with intent for front camera
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                Log.d("ThirdRegisterFragment", "Camera result OK, loading image")
                imageUri?.let { uri ->
                    binding.selectedImageView.visibility = View.VISIBLE
                    binding.selectedImageView.load(uri) {
                        transformations(coil.transform.CircleCropTransformation())
                    }
                    Log.d("ThirdRegisterFragment", "Image loaded with URI: $uri")
                }
                binding.finishRegistrationButton.isEnabled = true
            } else {
                Log.w("ThirdRegisterFragment", "Camera result was not OK, resultCode: ${result.resultCode}")
            }
        }

        binding.chooseImageButton.setOnClickListener {
            Log.d("ThirdRegisterFragment", "Choose Image button clicked")
            imageUri = createImageUri(requireContext())
            Log.d("ThirdRegisterFragment", "Image URI created: $imageUri")
            imageUri?.let { uri ->
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    // Request front camera
                    putExtra("android.intent.extras.CAMERA_FACING", 1) // For older devices
                    putExtra("android.intent.extras.LENS_FACING_FRONT", 1) // For newer devices
                    putExtra("android.intent.extra.USE_FRONT_CAMERA", true) // Extra fallback
                }
                Log.d("ThirdRegisterFragment", "Launching camera intent with URI: $uri")
                cameraLauncher.launch(cameraIntent)
            }
        }

        binding.finishRegistrationButton.setOnClickListener {
            Log.d("ThirdRegisterFragment", "Finish Registration button clicked")
            imageUri?.let { uri ->
                Log.d("ThirdRegisterFragment", "Uploading profile image with URI: $uri")
                viewModel.uploadProfileImage(uri)
                findNavController().navigate(R.id.action_thirdRegisterFragment_to_fortRegisterFragment)
                Log.d("ThirdRegisterFragment", "Navigated to Fourth Register Fragment")
            }
        }

        binding.backToSecondPartLink.setOnClickListener {
            Log.d("ThirdRegisterFragment", "Back to Second Part link clicked")
            findNavController().navigate(R.id.action_thirdRegisterFragment_to_secondRegisterFragment)
        }
    }

    private fun createImageUri(context: Context): Uri? {
        val image = File(context.filesDir, "camera_photo.jpg")
        Log.d("ThirdRegisterFragment", "Creating image file at path: ${image.absolutePath}")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            image
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("ThirdRegisterFragment", "onDestroyView: View destroyed")
        _binding = null
    }
}
