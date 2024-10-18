package com.example.carhive.Presentation.seller.view

import SelectedImagesAdapter
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Presentation.seller.viewModel.CrudViewModel
import com.example.carhive.R
import com.example.carhive.databinding.DialogCarOptionsBinding
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditCarDialogFragment : DialogFragment() {

    private var _binding: DialogCarOptionsBinding? = null
    private val binding get() = _binding!! // Getter for binding to avoid null checks

    private lateinit var viewModel: CrudViewModel // ViewModel for managing UI-related data
    private lateinit var car: CarEntity // Car entity to be edited
    private val selectedImages = mutableListOf<Uri>() // Store selected images
    private val existingImageUrls = mutableListOf<String>() // Store URLs of existing images
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter // Adapter for selected images

    // Launcher for image picking
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.let { data ->
                    val clipData = data.clipData
                    if (clipData != null) {
                        // Handle multiple image selection
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            requireContext().contentResolver.takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                            selectedImages.add(uri) // Add selected image URI to the list
                        }
                    } else {
                        // Handle single image selection
                        data.data?.let { uri ->
                            requireContext().contentResolver.takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                            selectedImages.add(uri) // Add selected image URI to the list
                        }
                    }
                    selectedImagesAdapter.notifyDataSetChanged() // Notify the adapter of data changes
                    updateImageCounter() // Update the image counter display
                }
            }
        }

    // Companion object for creating a new instance of the fragment with parameters
    companion object {
        fun newInstance(car: CarEntity, viewModel: CrudViewModel): EditCarDialogFragment {
            val fragment = EditCarDialogFragment()
            fragment.car = car // Set the car to be edited
            fragment.viewModel = viewModel // Set the ViewModel
            return fragment
        }
    }

    // Create a dialog with a custom theme
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.DialogTheme_FullScreen)
    }

    // Inflate the dialog layout
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogCarOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Called after the view has been created, set up UI and listeners
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI() // Initialize UI components
        setupListeners() // Set up button listeners
    }

    // Function to set up UI components with existing car data
    private fun setupUI() {
        // Populate fields with existing car details
        binding.etModelo.setText(car.modelo)
        binding.etColor.setText(car.color)
        binding.etSpeed.setText(car.speed)
        binding.etAddOn.setText(car.addOn)
        binding.etDescription.setText(car.description)
        binding.etPrice.setText(car.price)

        // Add existing images to the list
        car.imageUrls?.forEach { imageUrl ->
            existingImageUrls.add(imageUrl)
            selectedImages.add(Uri.parse(imageUrl)) // Parse and add existing image URI
        }

        // Initialize the adapter for displaying selected images
        selectedImagesAdapter = SelectedImagesAdapter(selectedImages) { position ->
            removeImage(position) // Handle image removal
        }

        binding.rvSelectedImages.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = selectedImagesAdapter // Set the adapter for the RecyclerView
        }

        updateImageCounter() // Update the image counter display
    }

    // Function to set up button listeners
    private fun setupListeners() {
        binding.buttonSelectImages.setOnClickListener {
            openImagePicker() // Open the image picker
        }

        binding.buttonCreate.setOnClickListener {
            updateCar() // Update car details
        }

        binding.buttonCancel.setOnClickListener {
            dismiss() // Close the dialog
        }
    }

    // Function to open the image picker for selecting images
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
            Log.e("EditCarDialogFragment", "Attempted to remove image at invalid position: $position") // Log error
        }
    }

    // Function to update the count of selected images
    private fun updateImageCounter() {
        val maxImages = 5 // Maximum number of images allowed
        binding.tvImageCount.text = "${selectedImages.size} / $maxImages images selected" // Display count
    }

    // Function to update car details
    private fun updateCar() {
        // Retrieve input values from fields
        val modelo = binding.etModelo.text.toString()
        val color = binding.etColor.text.toString()
        val speed = binding.etSpeed.text.toString()
        val addOn = binding.etAddOn.text.toString()
        val description = binding.etDescription.text.toString()
        val price = binding.etPrice.text.toString()

        // Check that all fields are filled
        if (modelo.isNotEmpty() && color.isNotEmpty() && speed.isNotEmpty() && addOn.isNotEmpty() &&
            description.isNotEmpty() && price.isNotEmpty()) {

            // Check that at least one image is selected
            if (selectedImages.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one image.", Toast.LENGTH_SHORT).show()
                return
            }

            // Check that the number of selected images is within limits
            if (selectedImages.size > 5) {
                Toast.makeText(requireContext(), "You can only select up to 5 images.", Toast.LENGTH_SHORT).show()
                return
            }

            if (selectedImages.size < 5) {
                Toast.makeText(requireContext(), "You have to select 5 images.", Toast.LENGTH_SHORT).show()
                return
            }

            // Separate new images from existing ones
            val newImages = selectedImages.filter { uri -> !existingImageUrls.contains(uri.toString()) }
            val imagesToKeep = selectedImages.filter { uri -> existingImageUrls.contains(uri.toString()) }

            // Show progress dialog while uploading images
            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setMessage("Uploading images, please wait...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            // Coroutine for uploading images and updating the car
            viewModel.viewModelScope.launch {
                try {
                    val userId = viewModel.getCurrentUserId() // Get the current user ID

                    val uploadedImageUrls = mutableListOf<String>() // List to store uploaded image URLs
                    newImages.forEach { uri ->
                        // Create a reference for the image in Firebase Storage
                        val imageRef = FirebaseStorage.getInstance().reference.child("cars/${uri.lastPathSegment}")
                        val uploadTask = imageRef.putFile(uri) // Upload the image
                        // Get the download URL after the upload is complete
                        val downloadUrl = uploadTask.await().storage.downloadUrl.await()
                        uploadedImageUrls.add(downloadUrl.toString()) // Add the URL to the list
                    }

                    // Update car details in the database
                    viewModel.updateCar(
                        userId = userId,
                        carId = car.id,
                        modelo = modelo,
                        color = color,
                        speed = speed,
                        addOn = addOn,
                        description = description,
                        price = price,
                        newImages = newImages,
                        existingImages = imagesToKeep
                    )

                    progressDialog.dismiss() // Dismiss the progress dialog
                    Toast.makeText(requireContext(), "Changes saved successfully.", Toast.LENGTH_SHORT).show() // Show success message
                    dismiss() // Close the dialog

                } catch (e: Exception) {
                    progressDialog.dismiss() // Dismiss the dialog on error
                    Toast.makeText(requireContext(), "Error uploading images or saving changes. Please try again.", Toast.LENGTH_LONG).show() // Show error message
                }
            }
        } else {
            Toast.makeText(requireContext(), "Please complete all fields.", Toast.LENGTH_SHORT).show() // Warn if fields are incomplete
        }
    }

    // Clean up and release binding references
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Release binding reference
    }
}
