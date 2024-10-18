package com.example.carhive.Presentation.seller.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.Presentation.seller.viewModel.CrudViewModel
import com.example.carhive.R
import com.example.carhive.databinding.DialogCarOptionsBinding
import com.example.carhive.databinding.ItemSelectedImageBinding

class CrudDialogFragment : DialogFragment() {

    private val viewModel: CrudViewModel by activityViewModels() // Get the ViewModel
    private var _binding: DialogCarOptionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent> // Launcher for image picking
    private val selectedImages = mutableListOf<Uri>() // Store selected image URIs
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter

    private val maxImages = 5 // Maximum number of images allowed

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCarOptionsBinding.inflate(inflater, container, false)
        return binding.root // Inflate the dialog layout
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
                            selectedImages.add(imageUri) // Add the selected image URI
                        }
                    } else {
                        showMaxImagesError() // Show error if limit exceeded
                    }
                } else {
                    // Handle single image selection
                    result.data?.data?.let { uri ->
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
            val modelo = binding.etModelo.text.toString()
            val color = binding.etColor.text.toString()
            val speed = binding.etSpeed.text.toString()
            val addOn = binding.etAddOn.text.toString()
            val description = binding.etDescription.text.toString()
            val price = binding.etPrice.text.toString()

            // Check that all fields are filled
            if (modelo.isEmpty() || color.isEmpty() || speed.isEmpty() || addOn.isEmpty() ||
                description.isEmpty() || price.isEmpty() || selectedImages.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show() // Show error message
            } else {
                // Add car to the database
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
        selectedImages.removeAt(position) // Remove image from the list
        selectedImagesAdapter.notifyItemRemoved(position) // Notify the adapter of removal
        updateImageCounter() // Update the image counter display
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

    override fun getTheme(): Int {
        return R.style.AppTheme_Dialog // You can customize the theme if necessary
    }
}

// Adapter for displaying selected images in a RecyclerView
class SelectedImagesAdapter(
    private val images: List<Uri>, // List of image URIs
    private val onRemoveImage: (Int) -> Unit // Callback for removing an image
) : RecyclerView.Adapter<SelectedImagesAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val selectedImageView: ImageView = itemView.findViewById(R.id.iv_selected_image) // ImageView for displaying image
        val removeImageButton: ImageButton = itemView.findViewById(R.id.btn_remove_image) // Button to remove image
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_image, parent, false) // Inflate item layout
        return ImageViewHolder(view) // Return the ViewHolder
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUri = images[position] // Get the URI for the current image

        if (imageUri.scheme == "content") {
            // Load from local URI (new images)
            Glide.with(holder.itemView.context)
                .load(imageUri)
                .placeholder(R.drawable.ic_img) // Placeholder while loading
                .error(R.drawable.ic_error) // Image to show on error
                .into(holder.selectedImageView) // Load the image into the ImageView
        } else {
            // Load from a URL (existing images from Firebase)
            Glide.with(holder.itemView.context)
                .load(imageUri.toString()) // Convert URI to String for Glide
                .placeholder(R.drawable.ic_img) // Placeholder while loading
                .error(R.drawable.ic_error) // Image to show on error
                .into(holder.selectedImageView) // Load the image into the ImageView
        }

        // Remove image button click listener
        holder.removeImageButton.setOnClickListener {
            onRemoveImage(position) // Call the remove image callback
        }
    }

    override fun getItemCount(): Int {
        return images.size // Return the total number of images
    }
}
