package com.example.carhive.Presentation.seller.view

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private val binding get() = _binding!!

    private lateinit var viewModel: CrudViewModel
    private lateinit var car: CarEntity
    private val selectedImages = mutableListOf<Uri>() // Store selected images
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter
    private val existingImageUrls = mutableListOf<String>() // Store URLs of existing images

    // Activity result launcher for selecting images
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.let { data ->
                    // Handle multiple images selected
                    val clipData = data.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            // Persist URI permission
                            requireContext().contentResolver.takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                            selectedImages.add(uri) // Add URI to selected images
                        }
                    } else {
                        data.data?.let { uri ->
                            requireContext().contentResolver.takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                            selectedImages.add(uri) // Add single URI to selected images
                        }
                    }
                    selectedImagesAdapter.notifyDataSetChanged() // Notify adapter of changes
                    updateImageCounter() // Update image count display
                }
            }
        }

    companion object {
        // Factory method to create an instance of the dialog with car data
        fun newInstance(car: CarEntity, viewModel: CrudViewModel): EditCarDialogFragment {
            val fragment = EditCarDialogFragment()
            fragment.car = car
            fragment.viewModel = viewModel
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.DialogTheme_FullScreen) // Full-screen dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogCarOptionsBinding.inflate(inflater, container, false)
        return binding.root // Inflate and return the dialog view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI() // Initialize UI elements
        setupListeners() // Set up button listeners
    }

    private fun setupUI() {
        // Load current car data into input fields
        binding.etModelo.setText(car.modelo)
        binding.etColor.setText(car.color)
        binding.etSpeed.setText(car.speed)
        binding.etAddOn.setText(car.addOn)
        binding.etDescription.setText(car.description)
        binding.etPrice.setText(car.price)

        // Add existing image URLs to the selected images list
        car.imageUrls?.forEach { imageUrl ->
            existingImageUrls.add(imageUrl)
            selectedImages.add(Uri.parse(imageUrl)) // Convert to URI for adapter
        }

        // Set up RecyclerView for displaying selected images
        selectedImagesAdapter = SelectedImagesAdapter(selectedImages) { position ->
            selectedImages.removeAt(position) // Remove image from list
            selectedImagesAdapter.notifyItemRemoved(position) // Notify adapter
            updateImageCounter() // Update image count display
        }

        binding.rvSelectedImages.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = selectedImagesAdapter // Set adapter for RecyclerView
        }

        updateImageCounter() // Update image count display
    }

    private fun setupListeners() {
        // Set up listener for selecting images button
        binding.buttonSelectImages.setOnClickListener {
            openImagePicker() // Open image picker
        }

        // Set up listener for create button
        binding.buttonCreate.setOnClickListener {
            updateCar() // Attempt to update the car
        }

        // Set up listener for cancel button
        binding.buttonCancel.setOnClickListener {
            dismiss() // Close the dialog
        }
    }

    private fun openImagePicker() {
        // Launch an intent to pick images
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Allow multiple selections
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        imagePickerLauncher.launch(intent) // Launch the image picker
    }

    private fun updateImageCounter() {
        val maxImages = 5
        // Update the display of the number of selected images
        binding.tvImageCount.text = "${selectedImages.size} / $maxImages images selected"
    }

    private fun updateCar() {
        // Get input values from the text fields
        val modelo = binding.etModelo.text.toString()
        val color = binding.etColor.text.toString()
        val speed = binding.etSpeed.text.toString()
        val addOn = binding.etAddOn.text.toString()
        val description = binding.etDescription.text.toString()
        val price = binding.etPrice.text.toString()

        // Check that all fields are filled
        if (modelo.isNotEmpty() && color.isNotEmpty() && speed.isNotEmpty() && addOn.isNotEmpty() &&
            description.isNotEmpty() && price.isNotEmpty()) {

            // Filter new images and images to keep
            val newImages = selectedImages.filter { uri -> !existingImageUrls.contains(uri.toString()) }
            val imagesToKeep = selectedImages.filter { uri -> existingImageUrls.contains(uri.toString()) }

            // Show progress dialog
            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setMessage("Uploading images, please wait...")
            progressDialog.setCancelable(false) // Prevent canceling
            progressDialog.show()

            viewModel.viewModelScope.launch {
                try {
                    val userId = viewModel.getCurrentUserId() // Get current user ID

                    // Upload new images to Firebase Storage
                    val uploadedImageUrls = mutableListOf<String>()
                    newImages.forEach { uri ->
                        val imageRef = FirebaseStorage.getInstance().reference.child("cars/${uri.lastPathSegment}")
                        val uploadTask = imageRef.putFile(uri) // Upload the image
                        val downloadUrl = uploadTask.await().storage.downloadUrl.await() // Get download URL
                        uploadedImageUrls.add(downloadUrl.toString()) // Store uploaded image URL
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

                    // Close progress dialog and show success message
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Changes saved successfully.", Toast.LENGTH_SHORT).show()
                    dismiss() // Close the dialog

                } catch (e: Exception) {
                    // If an error occurs, close progress dialog and show error message
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Error uploading images or saving changes. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            // Show error if fields are not completed
            Toast.makeText(requireContext(), "Please complete all fields.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding to prevent memory leaks
    }
}
