package com.example.carhive.presentation.seller.view

import SelectedImagesAdapter
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.data.model.CarEntity
import com.example.carhive.presentation.seller.viewModel.CrudViewModel
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
    private val selectedImages = mutableListOf<Uri>()
    private val existingImageUrls = mutableListOf<String>()
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter
    private var selectedColor: String? = null

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
        fun newInstance(car: CarEntity, viewModel: CrudViewModel) = EditCarDialogFragment().apply {
            this.car = car
            this.viewModel = viewModel
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = Dialog(requireContext(), R.style.DialogTheme_FullScreen)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        DialogCarOptionsBinding.inflate(inflater, container, false).also {
            _binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.apply {
            if (::car.isInitialized) {
                dialogTitle.text = getString(R.string.edit_car_title)
                buttonCreate.text = getString(R.string.update_button)
            } else {
                dialogTitle.text = getString(R.string.new_product_title)
                buttonCreate.text = getString(R.string.create_button)
            }

            if (::car.isInitialized) {
                etModelo.setText(car.modelo)
                selectedColor = car.color
                highlightSelectedColor()
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

            selectedImagesAdapter = SelectedImagesAdapter(selectedImages) { position -> removeImage(position) }
            binding.rvSelectedImages.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = selectedImagesAdapter
            }

            setupSpinners()
            updateImageCounter()
            setupAutoCompleteTextView()
        }
    }

    private fun highlightSelectedColor() {
        val colorButtons = listOf(
            Pair(R.id.red_button, "Red"),
            Pair(R.id.orange_button, "Orange"),
            Pair(R.id.yellow_button, "Yellow"),
            Pair(R.id.green_button, "Green"),
            Pair(R.id.blue_button, "Blue"),
            Pair(R.id.purple_button, "Purple"),
            Pair(R.id.pink_button, "Pink"),
            Pair(R.id.white_button, "White"),
            Pair(R.id.gray_button, "Gray"),
            Pair(R.id.black_button, "Black"),
            Pair(R.id.brown_button, "Brown")
        )

        colorButtons.forEach { (buttonId, color) ->
            val button = binding.root.findViewById<Button>(buttonId)
            if (color == selectedColor) {
                button.isSelected = true
                button.setBackgroundResource(R.drawable.selected_circle)
            } else {
                button.isSelected = false
                button.setBackgroundResource(R.drawable.selected_circle)
            }
        }
    }

    private fun setupSpinners() {
        val transmissionOptions = resources.getStringArray(R.array.transmission_options).toList()
        val fuelTypeOptions = resources.getStringArray(R.array.fuel_type_options).toList()
        val conditionOptions = resources.getStringArray(R.array.condition_options).toList()
        val locationOptions = resources.getStringArray(R.array.location_options).toList()
        val yearOptions = (1970..java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)).map { it.toString() }

        binding.apply {
            setupSpinner(spinnerTransmission, transmissionOptions, car.transmission)
            setupSpinner(spinnerFuelType, fuelTypeOptions, car.fuelType)
            setupSpinner(spinnerCondition, conditionOptions, car.condition)
            setupSpinner(spinnerLocation, locationOptions, car.location)
            setupSpinner(spinnerYear, yearOptions, car.year)
        }
    }

    private fun setupSpinner(spinner: Spinner, options: List<String>, selectedOption: String?) {
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        selectedOption?.let { spinner.setSelection(options.indexOf(it)) }
    }

    private fun setupAutoCompleteTextView() {
        // Cargar las marcas del archivo JSON
        val brandOptions = loadBrandsFromJson() ?: listOf()

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, brandOptions)
        binding.spinnerBrand.apply {
            setAdapter(adapter)
            setThreshold(1) // Comienza a mostrar sugerencias después de escribir 1 carácter

            // Manejar la selección de un elemento
            setOnItemClickListener { _, _, position, _ ->
                val selectedBrand = adapter.getItem(position) as String
                binding.spinnerBrand.setText(selectedBrand) // Asegurarse de que se muestra la marca seleccionada
                Toast.makeText(requireContext(), "Selected: $selectedBrand", Toast.LENGTH_SHORT).show()
            }

            // Si ya hay una marca seleccionada (por ejemplo, la marca de un auto ya existente), mostrarla
            if (::car.isInitialized) {
                val selectedBrand = car.brand  // Suponiendo que car.brand tiene la marca seleccionada previamente
                setText(selectedBrand, false)  // Establecer el texto sin activar el foco
            }

            // Validar la marca ingresada al perder el foco
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val userInput = binding.spinnerBrand.text.toString()
                    if (!brandOptions.contains(userInput)) {
                        showAlertIfInvalidBrand()
                    }
                }
            }
        }
    }


    private fun setupListeners() {
        setupColorFilter()

        binding.buttonSelectImages.setOnClickListener { openImagePicker() }
        binding.buttonCreate.setOnClickListener { validateForm() }
        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonSelectImages.setOnClickListener { openImagePicker() }

        binding.buttonCreate.setOnClickListener {
            validateForm()
        }

        binding.buttonCancel.setOnClickListener { dismiss() }
    }

    private fun setupColorFilter() {
        val colorButtons = listOf(
            Pair(R.id.red_button, "Red"),
            Pair(R.id.orange_button, "Orange"),
            Pair(R.id.yellow_button, "Yellow"),
            Pair(R.id.green_button, "Green"),
            Pair(R.id.blue_button, "Blue"),
            Pair(R.id.purple_button, "Purple"),
            Pair(R.id.pink_button, "Pink"),
            Pair(R.id.white_button, "White"),
            Pair(R.id.gray_button, "Gray"),
            Pair(R.id.black_button, "Black"),
            Pair(R.id.brown_button, "Brown")
        )

        colorButtons.forEach { (buttonId, color) ->
            val button = binding.root.findViewById<Button>(buttonId)

            button.setOnClickListener {
                selectedColor = if (selectedColor == color) null else color
                highlightSelectedColor()
            }
        }
    }

    private fun openImagePicker() {
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }.also { imagePickerLauncher.launch(it) }
    }

    private fun addImageUri(uri: Uri) {
        requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        selectedImages.add(uri)
    }

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

    private fun updateImageCounter() {
        binding.tvImageCount.text = getString(R.string.image_count_text, selectedImages.size, 5)
    }

    private fun validateForm() {
        with(binding) {
            val modelo = etModelo.text.toString()
            val mileage = etMileage.text.toString()
            val brand = spinnerBrand.text.toString()
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

            // Check that all fields are filled
            if (validateFields(modelo, mileage, brand, description, price, year, engineCapacity, location, vin)) {
                if (selectedColor.isNullOrEmpty()) {
                    return
                }
                // Check if the selected brand exists
                if (isBrandValid(brand)) {
                    if (selectedImages.size != 5) {
                        Toast.makeText(requireContext(), R.string.select_five_images, Toast.LENGTH_SHORT).show()
                        return
                    }

                    viewModel.viewModelScope.launch {
                        uploadImagesAndSaveCar(
                            modelo, selectedColor!!, mileage, brand, description, price, year, transmission, fuelType,
                            doors, engineCapacity, location, condition, features, vin, previousOwners
                        )
                    }
                } else {
                    showAlertIfInvalidBrand()  // Show error if brand is not valid
                }
            } else {
                Toast.makeText(requireContext(), R.string.complete_all_fields, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateFields(vararg fields: String) = fields.all { it.isNotEmpty() }

    private fun showAlertIfInvalidBrand() {
        val userInput = binding.spinnerBrand.text.toString()
        AlertDialog.Builder(requireContext())
            .setMessage("The brand '$userInput' does not exist. Please select a valid brand.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun isBrandValid(userInput: String): Boolean {
        val allBrands = loadBrandsFromJson() ?: emptyList()
        return allBrands.contains(userInput)
    }

    private fun loadBrandsFromJson(): List<String>? {
        return try {
            val jsonString = requireContext().assets.open("car_brands.json").bufferedReader().use { it.readText() }
            val jsonArray = org.json.JSONArray(jsonString)

            // Convertir el JSONArray en una lista de strings
            (0 until jsonArray.length()).map { jsonArray.getString(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.99).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}