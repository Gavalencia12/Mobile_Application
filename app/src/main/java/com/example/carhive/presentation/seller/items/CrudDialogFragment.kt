package com.example.carhive.presentation.seller.items

import SelectedImagesAdapter
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.presentation.seller.viewModel.CrudViewModel
import com.example.carhive.R
import com.example.carhive.databinding.DialogCarOptionsBinding
import java.text.SimpleDateFormat
import java.util.Locale

class CrudDialogFragment : DialogFragment() {

    private val viewModel: CrudViewModel by activityViewModels()
    private var _binding: DialogCarOptionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private val selectedImages = mutableListOf<Uri>()
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter

    private var selectedColor: String? = null

    private val maxImages = 5

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCarOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImageRecyclerView()
        setupImagePicker()
        setupSpinners()
        setupColorFilter(binding.root)

        // Handles the Create button click, validating input and triggering car listing creation
        binding.buttonCreate.setOnClickListener {
            validateForm()
        }

        // Handles the Cancel button click
        binding.buttonCancel.setOnClickListener { dismiss() }

        updateImageCounter()
    }

    /**
     * Initializes RecyclerView for displaying selected images
     */
    private fun setupImageRecyclerView() {
        selectedImagesAdapter = SelectedImagesAdapter(selectedImages) { position -> removeImage(position) }
        binding.rvSelectedImages.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = selectedImagesAdapter
        }
    }

    /**
     * Registers image picker launcher and handles selected images from the picker
     */
    private fun setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                handleImageSelection(result.data)
            }
        }

        binding.buttonSelectImages.setOnClickListener {
            if (selectedImages.size < maxImages) openImagePicker() else showMaxImagesError()
        }
    }

    /**
     * Opens the image picker to allow user selection of multiple images
     */
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        imagePickerLauncher.launch(intent)
    }

    /**
     * Processes selected images and updates RecyclerView adapter
     */
    private fun handleImageSelection(data: Intent?) {
        data?.let {
            val clipData = it.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    addImageUri(clipData.getItemAt(i).uri)
                }
            } else {
                it.data?.let { uri -> addImageUri(uri) }
            }
            selectedImagesAdapter.notifyDataSetChanged()
            updateImageCounter()
        }
    }

    /**
     * Adds an image URI to the selected images list, handling max image limit
     */
    private fun addImageUri(uri: Uri) {
        if (selectedImages.size < maxImages) {
            requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedImages.add(uri)
        } else {
            showMaxImagesError()
        }
    }

    /**
     * Removes image from selected images list at the given position
     */
    private fun removeImage(position: Int) {
        if (position in selectedImages.indices) {
            selectedImages.removeAt(position)
            selectedImagesAdapter.notifyItemRemoved(position)
            selectedImagesAdapter.notifyItemRangeChanged(position, selectedImages.size)
            updateImageCounter()
        }
    }

    /**
     * Validates the input fields and selected images to ensure all required data is provided
     */
    private fun isFormValid(): Boolean {
        val requiredFields = listOf(
            binding.etModelo.text.toString(),
            binding.colorGrid.toString(),
            binding.etMileage.text.toString(),
            binding.spinnerBrand.text.toString(),
            binding.etDescription.text.toString(),
            binding.etPrice.text.toString(),
            binding.spinnerYear.selectedItem.toString(),
            binding.etEngineCapacity.text.toString(),
            binding.spinnerLocation.selectedItem.toString(),
            binding.etVin.text.toString()
        )

        return when {
            requiredFields.any { it.isEmpty() } -> {
                Toast.makeText(requireContext(), R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show()
                false
            }
            selectedImages.size != maxImages -> {
                Toast.makeText(requireContext(), R.string.error_select_5_images, Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    /**
     * Initiates the process of creating a new car listing, displaying a progress dialog
     */
    private fun createCarListing() {
        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.creating_listing))
            setCancelable(false)
            show()
        }

        // Directly passing individual fields to addCarToDatabase function
        viewModel.addCarToDatabase(
            modelo = binding.etModelo.text.toString(),
            color = selectedColor ?: "",
            mileage = binding.etMileage.text.toString(),
            brand=  binding.spinnerBrand.text.toString(),
            description = binding.etDescription.text.toString(),
            price = binding.etPrice.text.toString(),
            year = binding.spinnerYear.selectedItem.toString(),
            transmission = binding.spinnerTransmission.selectedItem.toString(),
            fuelType = binding.spinnerFuelType.selectedItem.toString(),
            doors = binding.etDoors.text.toString().toIntOrNull() ?: 0,
            engineCapacity = binding.etEngineCapacity.text.toString(),
            location = binding.spinnerLocation.selectedItem.toString(),
            condition = binding.spinnerCondition.selectedItem.toString(),
            images = selectedImages,
            features = binding.etFeatures.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
            vin = binding.etVin.text.toString(),
            previousOwners = binding.etPreviousOwners.text.toString().toIntOrNull() ?: 0,
            listingDate = getCurrentFormattedDate(),
            lastUpdated = getCurrentFormattedDate()
        )

        Handler(Looper.getMainLooper()).postDelayed({
            progressDialog.dismiss()
            dismiss()
            Toast.makeText(requireContext(), R.string.success_listing_created, Toast.LENGTH_SHORT).show()
        }, 3000)
    }

    private fun getCurrentFormattedDate(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        return dateFormat.format(System.currentTimeMillis())
    }

    private fun showMaxImagesError() {
        Toast.makeText(requireContext(), getString(R.string.error_max_images, maxImages), Toast.LENGTH_SHORT).show()
    }

    private fun updateImageCounter() {
        binding.tvImageCount.text = getString(R.string.image_counter, selectedImages.size, maxImages)
    }

    private fun setupSpinners() {
        setupSpinner(binding.spinnerTransmission, listOf("Manual", "Automatic"))
        setupSpinner(binding.spinnerFuelType, listOf("Gasoline", "Diesel", "Electric", "Hybrid"))
        setupSpinner(binding.spinnerCondition, listOf("New", "Used", "Pre-owned"))
        setupSpinner(
            binding.spinnerLocation,
            listOf(
                "Armería", "Colima", "Comala", "Coquimatlán", "Cuauhtémoc",
                "Ixtlahuacán", "Manzanillo", "Minatitlán", "Tecomán", "Villa de Álvarez"
            )
        )

        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        setupSpinner(binding.spinnerYear, (1970..currentYear).map { it.toString() })

        setupBrandSpinner()
    }

    private fun setupBrandSpinner() {
        // Marcas predeterminadas
        val defaultBrands = listOf("Toyota", "Honda", "Ford", "Chevrolet", "Nissan", "Volkswagen", "BMW", "Mercedes-Benz", "Audi", "Hyundai")

        // Cargar otras marcas desde car_brands.json
        val otherBrands = loadBrandsFromJson()?.filterNot { defaultBrands.contains(it) }
            ?: listOf("No additional brands available")

        // Combinar listas y eliminar el separador
        val combinedBrands = defaultBrands + otherBrands

        // Configura el AutoCompleteTextView con las marcas
        setupAutoCompleteTextView(binding.spinnerBrand, combinedBrands)
    }

    /**
     * Configura un AutoCompleteTextView con la lista de marcas.
     */
    private fun setupAutoCompleteTextView(autoCompleteTextView: AutoCompleteTextView, options: List<String>) {
        // Usamos ArrayAdapter para las sugerencias de autocompletado
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)

        // Configuramos el AutoCompleteTextView con el adapter
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.setThreshold(1) // Comienza a mostrar sugerencias después de un carácter

        // Manejo de la selección de un elemento
        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedBrand = parent.getItemAtPosition(position) as String
            Toast.makeText(requireContext(), "Selected: $selectedBrand", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Muestra un AlertDialog si la marca no existe cuando el usuario presiona "Create"
     */
    private fun showAlertIfInvalidBrand() {
        val userInput = binding.spinnerBrand.text.toString()

        if (!isBrandValid(userInput)) {
            AlertDialog.Builder(requireContext())
                .setMessage("The brand '$userInput' does not exist. Please select a valid brand.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    /**
     * Verifica si la marca ingresada es válida
     */
    private fun isBrandValid(userInput: String): Boolean {
        val allBrands = loadBrandsFromJson() ?: emptyList()
        return allBrands.contains(userInput)
    }

    /**
     * Verifica si los campos están completos y muestra el mensaje adecuado
     */
    private fun validateForm() {
        if (!isFormValid()) {
            return
        }

        // Si todos los campos están completos, verificar la marca
        val userInput = binding.spinnerBrand.text.toString()
        if (isBrandValid(userInput)) {
            createCarListing() // Crear el carro si la marca es válida
        } else {
            showAlertIfInvalidBrand() // Si la marca no es válida, mostrar el AlertDialog
        }
    }

    /**
     * Lee y parsea las marcas adicionales desde un archivo JSON.
     */
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

    /**
     * Configura un spinner con una lista de opciones.
     */
    // Set up color filter buttons
    private fun setupColorFilter(view: View) {
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
            val button = view.findViewById<Button>(buttonId)

            button.isSelected = color == selectedColor // Marca el botón si el color ya está seleccionado
            button.setBackgroundResource(if (button.isSelected) R.drawable.selected_circle else R.drawable.selected_circle)

            button.setOnClickListener {
                // Actualiza el color seleccionado
                if (selectedColor == color) {
                    selectedColor = null
                    button.isSelected = false
                } else {
                    selectedColor = color
                    colorButtons.forEach { (id, _) ->
                        val otherButton = view.findViewById<Button>(id)
                        otherButton.isSelected = false
                        otherButton.setBackgroundResource(R.drawable.selected_circle)
                    }
                    button.isSelected = true
                    button.setBackgroundResource(R.drawable.selected_circle)
                }

                Toast.makeText(requireContext(), "Selected color: $selectedColor", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinner(spinner: Spinner, options: List<String>) {
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
