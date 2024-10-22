package com.example.carhive.Presentation.seller.view

import SelectedImagesAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.Presentation.seller.viewModel.CrudViewModel
import com.example.carhive.R
import com.example.carhive.databinding.DialogCarOptionsBinding

class CrudDialogFragment : DialogFragment() {

    // Get the ViewModel for managing UI-related data
    private val viewModel: CrudViewModel by activityViewModels()
    private var _binding: DialogCarOptionsBinding? = null
    private val binding get() = _binding!! // Getter for binding to avoid null checks

    // Launcher for image picking
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    // List to store selected image URIs
    private val selectedImages = mutableListOf<Uri>()
    // Adapter for displaying selected images in a RecyclerView
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter

    private val maxImages = 5 // Maximum number of images allowed

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the dialog layout
        _binding = DialogCarOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and adapter for displaying selected images
        selectedImagesAdapter = SelectedImagesAdapter(selectedImages) { position ->
            removeImage(position) // Handle image removal
        }

        binding.rvSelectedImages.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = selectedImagesAdapter // Set the adapter for the RecyclerView
        }

        // Initialize the image picker activity result launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val clipData = result.data?.clipData
                if (clipData != null) {
                    // Allow multiple image selections, with a limit
                    val newImagesCount = clipData.itemCount
                    if (selectedImages.size + newImagesCount <= maxImages) {
                        for (i in 0 until newImagesCount) {
                            val imageUri = clipData.getItemAt(i).uri
                            // Persist URI permission for the selected image
                            requireContext().contentResolver.takePersistableUriPermission(
                                imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                            selectedImages.add(imageUri) // Add the selected image URI
                        }
                    } else {
                        showMaxImagesError() // Show error if limit exceeded
                    }
                } else {
                    // Handle single image selection
                    result.data?.data?.let { uri ->
                        requireContext().contentResolver.takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        if (selectedImages.size < maxImages) {
                            selectedImages.add(uri) // Add the selected image URI
                        } else {
                            showMaxImagesError() // Show error if limit exceeded
                        }
                    }
                }
                selectedImagesAdapter.notifyDataSetChanged() // Notify the adapter of changes
                updateImageCounter() // Update the image counter display
            }
        }

        // Button to select images
        binding.buttonSelectImages.setOnClickListener {
            if (selectedImages.size < maxImages) {
                openImagePicker() // Open the image picker
            } else {
                showMaxImagesError() // Show error if limit exceeded
            }
        }

        // Button to create the car with selected images
        binding.buttonCreate.setOnClickListener {
            // Get input data from fields
            val modelo = binding.etModelo.text.toString()
            val color = binding.etColor.text.toString()
            val speed = binding.etSpeed.text.toString()
            val addOn = binding.etAddOn.text.toString()
            val description = binding.etDescription.text.toString()
            val price = binding.etPrice.text.toString()

            // Validate that all fields are filled
            if (modelo.isEmpty() || color.isEmpty() || speed.isEmpty() || addOn.isEmpty() ||
                description.isEmpty() || price.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show() // Show error message
            } else if (selectedImages.size != 5) {
                // Show message if the number of selected images is not exactly 5
                Toast.makeText(requireContext(), "You must select exactly 5 images", Toast.LENGTH_SHORT).show()
            } else {
                // If everything is correct, perform the action
                viewModel.addCarToDatabase(
                    modelo = modelo,
                    color = color,
                    speed = speed,
                    addOn = addOn,
                    description = description,
                    price = price,
                    images = selectedImages
                )

                dismiss() // Close the dialog
            }
        }

        // Cancel button to close the dialog
        binding.buttonCancel.setOnClickListener {
            dismiss() // Close the dialog
        }

        // Update the image counter on startup
        updateImageCounter()
    }

    // Function to open the image picker
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*" // Allow image selection
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Allow multiple images
            addCategory(Intent.CATEGORY_OPENABLE) // Make the intent openable
        }
        imagePickerLauncher.launch(intent) // Launch the image picker
    }

    // Function to remove a selected image
    private fun removeImage(position: Int) {
        if (position in selectedImages.indices) { // Check if position is valid
            selectedImages.removeAt(position) // Remove image from the list
            selectedImagesAdapter.notifyItemRemoved(position) // Notify the adapter of the removal
            selectedImagesAdapter.notifyItemRangeChanged(position, selectedImages.size) // Update the range of items
            updateImageCounter() // Update the image counter
        } else {
            Toast.makeText(requireContext(), "Invalid position for removal.", Toast.LENGTH_SHORT).show()
        }
    }

    // Show error if the maximum number of images is exceeded
    private fun showMaxImagesError() {
        Toast.makeText(requireContext(), "You can only select a maximum of $maxImages images", Toast.LENGTH_SHORT).show()
    }

    // Function to update the count of selected images
    private fun updateImageCounter() {
        binding.tvImageCount.text = "${selectedImages.size}/$maxImages images selected"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding to prevent memory leaks
    }

    // Function to customize the dialog theme
    override fun getTheme(): Int {
        return R.style.AppTheme_Dialog // You can customize the theme if necessary
    }

    //function to the complete model
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent) // Fondo transparente
    }
}
