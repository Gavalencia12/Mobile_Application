package com.example.carhive.Presentation.user.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Presentation.user.adapter.BrandAdapter
import com.example.carhive.Presentation.user.adapter.CarHomeAdapter
import com.example.carhive.Presentation.user.viewModel.UserViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentUserHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class UserHomeFragment : Fragment() {

    private var _binding: FragmentUserHomeBinding? = null // ViewBinding reference for fragment layout
    private val binding get() = _binding!! // Non-null binding reference
    private val viewModel: UserViewModel by viewModels() // ViewModel to handle UI-related data
    private lateinit var carAdapter: CarHomeAdapter // Adapter for displaying cars
    private lateinit var brandAdapter: BrandAdapter // Adapter for displaying brands

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the RecyclerView adapter for car listings
        carAdapter = CarHomeAdapter(
            emptyList(),
            onFavoriteChecked = { car, isFavorite -> viewModel.toggleFavorite(car, isFavorite) },
            isCarFavorite = { carId, callback -> viewModel.isCarFavorite(carId, callback) },
            onCarClick = { car -> navigateToCarDetail(car) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = carAdapter
        }

        // Observe changes in the car list to update the RecyclerView
        viewModel.carList.observe(viewLifecycleOwner) { cars ->
            carAdapter.updateCars(cars)
        }
        viewModel.fetchCars() // Fetch initial list of cars

        // Set up location filter
        setupLocationFilter()

        // Set up asynchronous model search
        setupModelSearch()

        // Show filter dialog when filters button is clicked
        binding.filtrers.setOnClickListener { showFilterDialog() }
    }

    // Configures the AutoCompleteTextView for asynchronous model search
    private fun setupModelSearch() {
        val autoCompleteModelSearch: AutoCompleteTextView = binding.autoCompleteModelSearch

        // Set unique car models in the adapter for the AutoCompleteTextView
        viewModel.uniqueCarModels.observe(viewLifecycleOwner) { models ->
            autoCompleteModelSearch.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, models)
            )
        }

        var searchJob: Job? = null // Variable to manage search delay

        // Add a TextWatcher to apply asynchronous filtering
        autoCompleteModelSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel() // Cancel any pending search

                val query = s.toString()
                if (query.isEmpty()) {
                    viewModel.selectedModel = null  // Clear model filter
                    viewModel.fetchCars()           // Show all cars
                } else {
                    // Delay search to avoid unnecessary calls
                    searchJob = viewLifecycleOwner.lifecycleScope.launch {
                        delay(300) // Delay of 300 ms before executing search
                        viewModel.selectedModel = query
                        viewModel.applyFilters() // Apply filter if text is present
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Shows the filter dialog and configures filter options
    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        setupYearFilter(dialogView)
        setupBrandAutoCompleteFilter(dialogView)
        setupColorFilter(dialogView)
        setupPriceMileageFilter(dialogView)
        setupSectionNavigation(dialogView)

        // Apply filters and close the dialog
        dialogView.findViewById<Button>(R.id.btn_apply_filters).setOnClickListener {
            applyFilters(dialogView)
            dialog.dismiss()
        }

        // Reset filters to the initial state and close the dialog
        dialogView.findViewById<Button>(R.id.btn_restore_filters).setOnClickListener {
            showResetConfirmationDialog(dialog)
        }

        // Cancel and close the dialog
        dialogView.findViewById<Button>(R.id.cancel_action).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Method to apply filters from the dialog
    private fun applyFilters(view: View) {
        val startYearInput = view.findViewById<AutoCompleteTextView>(R.id.start_year).text.toString()
        val endYearInput = view.findViewById<AutoCompleteTextView>(R.id.end_year).text.toString()

        val startYear = startYearInput.toIntOrNull()
        val endYear = endYearInput.toIntOrNull()

        viewModel.yearRange = if (startYear != null || endYear != null) {
            (startYear ?: viewModel.yearRange?.first ?: 1970) to (endYear ?: viewModel.yearRange?.second ?: Calendar.getInstance().get(Calendar.YEAR))
        } else {
            null
        }

        viewModel.priceRange = Pair(
            view.findViewById<EditText>(R.id.editText_min_price).text.toString().replace(",", "").toIntOrNull() ?: 0,
            view.findViewById<EditText>(R.id.editText_max_price).text.toString().replace(",", "").toIntOrNull()
        )

        viewModel.mileageRange = Pair(
            view.findViewById<EditText>(R.id.editText_min_mileage).text.toString().replace(",", "").toIntOrNull() ?: 0,
            view.findViewById<EditText>(R.id.editText_max_mileage).text.toString().replace(",", "").toIntOrNull()
        )

        viewModel.applyFilters() // Apply all selected filters
    }

    // Shows a confirmation dialog to reset filters
    private fun showResetConfirmationDialog(dialog: AlertDialog) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_reset)
            .setMessage(R.string.reset_filters_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.clearFilters()
                viewModel.fetchCars()
                dialog.dismiss()
                Toast.makeText(requireContext(), R.string.filters_reset_successfully, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    // Sets up location filter in the Spinner
    private fun setupLocationFilter() {
        val locationOptions = resources.getStringArray(R.array.location_options)
        val locationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locationOptions)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.ubication.adapter = locationAdapter

        // Select "All" location by default
        binding.ubication.setSelection(0)

        // Apply location filter based on selected option
        binding.ubication.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLocation = locationOptions[position]
                if (selectedLocation == "All") {
                    viewModel.clearLocationFilter()
                } else {
                    viewModel.filterByLocation(selectedLocation)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // Navigates to the car detail screen with selected car information
    private fun navigateToCarDetail(car: CarEntity) {
        val bundle = Bundle().apply {
            putString("carModel", car.modelo)
            putString("carPrice", car.price)
            putString("carColor", car.color)
            putString("carDescription", car.description)
            putString("carTransmission", car.transmission)
            putString("carFuelType", car.fuelType)
            putInt("carDoors", car.doors)
            putString("carEngineCapacity", car.engineCapacity)
            putString("carLocation", car.location)
            putString("carCondition", car.condition)
            putString("carVin", car.vin)
            putInt("carPreviousOwners", car.previousOwners)
            putInt("carViews", car.views)
            putString("carMileage", car.mileage)
            putString("carYear", car.year)
            putStringArrayList("carImageUrls", car.imageUrls?.let { ArrayList(it) })
        }
        findNavController().navigate(R.id.action_userHomeFragment_to_carDetailFragment, bundle)
    }

    // Sets up year filter options in the dialog
    private fun setupYearFilter(view: View) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (1970..currentYear).map { it.toString() }
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, years)

        val startYearView: AutoCompleteTextView = view.findViewById(R.id.start_year)
        val endYearView: AutoCompleteTextView = view.findViewById(R.id.end_year)
        startYearView.setAdapter(yearAdapter)
        endYearView.setAdapter(yearAdapter)

        viewModel.yearRange?.let { (start, end) ->
            startYearView.setText(start.toString(), false)
            endYearView.setText(end.toString(), false)
        }
    }

    // Sets up brand autocomplete filter in the dialog
    private fun setupBrandAutoCompleteFilter(view: View) {
        val autoCompleteBrand: AutoCompleteTextView = view.findViewById(R.id.autoComplete_brand)
        val recyclerViewBrands: RecyclerView = view.findViewById(R.id.recyclerView_brands)

        val brandOptions = resources.getStringArray(R.array.brand_options).toMutableList()
        brandAdapter = BrandAdapter(brandOptions, viewModel.selectedBrands) { selectedBrands ->
            viewModel.selectedBrands = selectedBrands.toMutableSet()
        }

        recyclerViewBrands.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewBrands.adapter = brandAdapter

        autoCompleteBrand.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, brandOptions))

        autoCompleteBrand.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase(Locale.getDefault())
                val filteredBrands = brandOptions.filter { it.lowercase(Locale.getDefault()).contains(query) }
                brandAdapter.updateBrands(filteredBrands)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        autoCompleteBrand.setOnItemClickListener { parent, _, position, _ ->
            val selectedBrand = parent.getItemAtPosition(position) as String
            val filteredBrands = brandOptions.filter { it == selectedBrand }
            brandAdapter.updateBrands(filteredBrands)
        }
    }

    // Sets up color filter with an AutoCompleteTextView populated with unique colors
    private fun setupColorFilter(view: View) {
//        val autoCompleteColor: AutoCompleteTextView = view.findViewById(R.id.autoComplete_color)
//        val colorGrid = view.findViewById<GridLayout>(R.id.color_grid)
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
            Pair(R.id.black_button, "Black")
        )

        // Click listener for each color button
        colorButtons.forEach { (buttonId, color) ->
            view.findViewById<Button>(buttonId).setOnClickListener {
                // Update the selected color in the ViewModel
                viewModel.selectedColor = color
                Toast.makeText(requireContext(), "Color selected: $color", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Sets up price and mileage filters with formatted EditTexts
    private fun setupPriceMileageFilter(view: View) {
        val minPrice: EditText = view.findViewById(R.id.editText_min_price)
        val maxPrice: EditText = view.findViewById(R.id.editText_max_price)
        val minMileage: EditText = view.findViewById(R.id.editText_min_mileage)
        val maxMileage: EditText = view.findViewById(R.id.editText_max_mileage)

        listOf(minPrice, maxPrice, minMileage, maxMileage).forEach { addThousandSeparator(it) }
    }

    // Adds thousand separators to EditText inputs
    private fun addThousandSeparator(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    editText.removeTextChangedListener(this)
                    val cleanString = s.toString().replace(",", "")
                    val formatted = NumberFormat.getNumberInstance(Locale.US).format(cleanString.toLongOrNull() ?: 0)
                    current = formatted
                    editText.setText(formatted)
                    editText.setSelection(formatted.length)
                    editText.addTextChangedListener(this)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Sets up section navigation within the filter dialog
    private fun setupSectionNavigation(view: View) {
        val scrollView: ScrollView = view.findViewById(R.id.scrollView)
        val sectionsMap = mapOf(
            view.findViewById<TextView>(R.id.nav_option_brands) to view.findViewById<View>(R.id.section_brands),
            view.findViewById<TextView>(R.id.nav_option_price) to view.findViewById<View>(R.id.section_price),
            view.findViewById<TextView>(R.id.nav_option_mileage) to view.findViewById<View>(R.id.section_mileage),
            view.findViewById<TextView>(R.id.nav_option_color) to view.findViewById<View>(R.id.section_color),
            view.findViewById<TextView>(R.id.nav_option_year) to view.findViewById<View>(R.id.section_year)
        )

        sectionsMap.forEach { (navOption, targetSection) ->
            navOption.setOnClickListener {
                scrollView.post { scrollView.smoothScrollTo(0, targetSection.top) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to prevent memory leaks
    }
}
