package com.example.carhive.Presentation.seller.view

import SelectedImagesAdapter
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
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

    // View binding to access UI elements
    private var _binding: DialogCarOptionsBinding? = null
    private val binding get() = _binding!!

    // Variables for ViewModel, car to be edited, and lists for images
    private lateinit var viewModel: CrudViewModel
    private lateinit var car: CarEntity
    private val selectedImages = mutableListOf<Uri>()
    private val existingImageUrls = mutableListOf<String>()
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter

    // Activity result launcher for selecting multiple images
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.let { data ->
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        addImageUri(clipData.getItemAt(i).uri)
                    }
                } else {
                    data.data?.let { uri -> addImageUri(uri) }
                }
                selectedImagesAdapter.notifyDataSetChanged()
                updateImageCounter()
            }
        }
    }

    companion object {
        // Factory method to create an instance with a car and view model
        fun newInstance(car: CarEntity, viewModel: CrudViewModel) = EditCarDialogFragment().apply {
            this.car = car
            this.viewModel = viewModel
        }
    }

    // Set dialog theme to fullscreen
    override fun onCreateDialog(savedInstanceState: Bundle?) = Dialog(requireContext(), R.style.DialogTheme_FullScreen)

    // Inflate layout and initialize binding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        DialogCarOptionsBinding.inflate(inflater, container, false).also {
            _binding = it
        }.root

    // Setup UI components and listeners
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
    }

    // Populate UI elements with car details and initialize image adapter
    private fun setupUI() {
        binding.apply {
            // Check if editing an existing car or creating a new one
            if (::car.isInitialized) {
                dialogTitle.text = getString(R.string.edit_car_title) // Set title to "Edit Car"
                buttonCreate.text = getString(R.string.update_button)  // Set button text to "Update"
            } else {
                dialogTitle.text = getString(R.string.new_product_title) // Default title "New Product"
                buttonCreate.text = getString(R.string.create_button)  // Default button "Create"
            }

            // Set car fields if updating an existing car
            if (::car.isInitialized) {
                etModelo.setText(car.modelo)
                etColor.setText(car.color)
                etDescription.setText(car.description)
                etPrice.setText(car.price)
                etMileage.setText(car.mileage)
                etDoors.setText(car.doors.toString())
                etEngineCapacity.setText(car.engineCapacity)
                etFeatures.setText(car.features?.joinToString(", "))
                etVin.setText(car.vin)
                etPreviousOwners.setText(car.previousOwners.toString())
            }

            car.imageUrls?.forEach { imageUrl ->
                existingImageUrls.add(imageUrl)
                selectedImages.add(Uri.parse(imageUrl))
            }

            // Initialize adapter to display selected images
            selectedImagesAdapter = SelectedImagesAdapter(selectedImages) { position -> removeImage(position) }
            binding.rvSelectedImages.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = selectedImagesAdapter
            }

            // Set up dropdown options
            setupSpinners()
            updateImageCounter()
        }
    }

    // Initialize spinners with options and set selected values
    private fun setupSpinners() {
        val transmissionOptions = resources.getStringArray(R.array.transmission_options).toList()
        val fuelTypeOptions = resources.getStringArray(R.array.fuel_type_options).toList()
        val conditionOptions = resources.getStringArray(R.array.condition_options).toList()
        val locationOptions = resources.getStringArray(R.array.location_options).toList()
        val brandOptions = resources.getStringArray(R.array.brand_options).toList()
        val yearOptions = (1970..java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)).map { it.toString() }

        binding.apply {
            setupSpinner(spinnerTransmission, transmissionOptions, car.transmission)
            setupSpinner(spinnerFuelType, fuelTypeOptions, car.fuelType)
            setupSpinner(spinnerCondition, conditionOptions, car.condition)
            setupSpinner(spinnerLocation, locationOptions, car.location)
            setupSpinner(spinnerBrand, brandOptions, car.brand)
            setupSpinner(spinnerYear, yearOptions, car.year)
        }
    }

    // Helper function to configure a spinner with options
    private fun setupSpinner(spinner: Spinner, options: List<String>, selectedOption: String?) {
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        selectedOption?.let { spinner.setSelection(options.indexOf(it)) }
    }

    // Set up button listeners
    private fun setupListeners() {
        binding.buttonSelectImages.setOnClickListener { openImagePicker() }

        binding.buttonCreate.setOnClickListener {
            updateCar()
        }

        binding.buttonCancel.setOnClickListener { dismiss() }
    }

    // Open image picker to select images
    private fun openImagePicker() {
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }.also { imagePickerLauncher.launch(it) }
    }

    // Add selected image URI to the list
    private fun addImageUri(uri: Uri) {
        requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        selectedImages.add(uri)
    }

    // Remove image from the list at the given position
    private fun removeImage(position: Int) {
        if (position in selectedImages.indices) {
            val uriToRemove = selectedImages[position]
            selectedImages.removeAt(position)
            val urlToRemove = uriToRemove.toString()
            existingImageUrls.remove(urlToRemove)
            selectedImagesAdapter.notifyItemRemoved(position)
            selectedImagesAdapter.notifyItemRangeChanged(position, selectedImages.size)
            updateImageCounter()
        }
    }

    // Update displayed image count
    private fun updateImageCounter() {
        binding.tvImageCount.text = getString(R.string.image_count_text, selectedImages.size, 5)
    }

    // Collect input data and update the car record
    private fun updateCar() {
        with(binding) {
            val modelo = etModelo.text.toString()
            val color = etColor.text.toString()
            val mileage = etMileage.text.toString()
            val brand = spinnerBrand.selectedItem.toString()
            val description = etDescription.text.toString()
            val price = etPrice.text.toString()
            val year = spinnerYear.selectedItem.toString()
            val transmission = spinnerTransmission.selectedItem.toString()
            val fuelType = spinnerFuelType.selectedItem.toString()
            val doors = etDoors.text.toString().toIntOrNull() ?: 0
            val engineCapacity = etEngineCapacity.text.toString()
            val location = spinnerLocation.selectedItem.toString()
            val condition = spinnerCondition.selectedItem.toString()
            val features = etFeatures.text.toString().split(",").map { it.trim() }
            val vin = etVin.text.toString()
            val previousOwners = etPreviousOwners.text.toString().toIntOrNull() ?: 0

            if (validateFields(modelo, color, mileage, brand, description, price, year, engineCapacity, location, vin)) {
                if (selectedImages.size != 5) {
                    Toast.makeText(requireContext(), R.string.select_five_images, Toast.LENGTH_SHORT).show()
                    return
                }

                viewModel.viewModelScope.launch {
                    uploadImagesAndSaveCar(
                        modelo, color, mileage, brand, description, price, year, transmission, fuelType,
                        doors, engineCapacity, location, condition, features, vin, previousOwners
                    )
                }
            } else {
                Toast.makeText(requireContext(), R.string.complete_all_fields, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Check if all fields are filled
    private fun validateFields(vararg fields: String) = fields.all { it.isNotEmpty() }

    // Upload images and save car details to Firebase
    private suspend fun uploadImagesAndSaveCar(
        modelo: String,
        color: String,
        mileage: String,
        brand: String,
        description: String,
        price: String,
        year: String,
        transmission: String,
        fuelType: String,
        doors: Int,
        engineCapacity: String,
        location: String,
        condition: String,
        features: List<String>,
        vin: String,
        previousOwners: Int
    ) {
        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.uploading_images))
            isCancelable = false
            show()
        }

        try {
            val userId = viewModel.getCurrentUserId()
            val imagesToDelete = existingImageUrls.filterNot { imageUrl ->
                selectedImages.any { Uri.parse(imageUrl) == it }
            }
            imagesToDelete.forEach { imageUrl ->
                FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).delete().await()
            }

            val uploadedImageUrls = mutableListOf<String>()
            val newImages = selectedImages.filter { !existingImageUrls.contains(it.toString()) }

            for (uri in newImages) {
                val imageRef = FirebaseStorage.getInstance().reference.child("cars/${uri.lastPathSegment}")
                val downloadUrl = imageRef.putFile(uri).await().storage.downloadUrl.await()
                uploadedImageUrls.add(downloadUrl.toString())
            }

            val allImageUrls = selectedImages.map { uri ->
                if (existingImageUrls.contains(uri.toString())) uri.toString() else uploadedImageUrls.find { it.endsWith(uri.lastPathSegment!!) } ?: ""
            }.filter { it.isNotEmpty() }

            viewModel.updateCar(
                userId = userId,
                carId = car.id,
                modelo = modelo,
                color = color,
                mileage = mileage,
                brand = brand,
                description = description,
                price = price,
                year = year,
                transmission = transmission,
                fuelType = fuelType,
                doors = doors,
                engineCapacity = engineCapacity,
                location = location,
                condition = condition,
                features = features,
                vin = vin,
                previousOwners = previousOwners,
                views = car.views,
                listingDate = car.listingDate,
                lastUpdated = car.lastUpdated,
                existingImages = allImageUrls,
                newImages = uploadedImageUrls
            )

            progressDialog.dismiss()
            Toast.makeText(requireContext(), R.string.changes_saved, Toast.LENGTH_SHORT).show()
            dismiss()
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(requireContext(), R.string.error_saving_changes, Toast.LENGTH_LONG).show()
        }
    }

    // Clear binding when the view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Set dialog width to almost full screen when starting
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout((resources.displayMetrics.widthPixels * 0.99).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
