package com.example.carhive.presentation.user.view

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carhive.Presentation.user.adapter.BrandAdapter
import com.example.carhive.presentation.user.adapter.CarHomeAdapter
import com.example.carhive.presentation.user.viewModel.UserViewModel
import com.example.carhive.R
import com.example.carhive.data.model.CarEntity
import com.example.carhive.databinding.FragmentUserHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class UserHomeFragment : Fragment() {

    private var _binding: FragmentUserHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels()
    private lateinit var carAdapter: CarHomeAdapter
    private lateinit var brandAdapter: BrandAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        carAdapter = CarHomeAdapter(
            emptyList(),
            onFavoriteChecked = { car, isFavorite -> viewModel.toggleFavorite(car, isFavorite) },
            isCarFavorite = { carId, callback -> viewModel.isCarFavorite(carId, callback) },
            onCarClick = { car -> navigateToCarDetail(car) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = carAdapter
        }

        viewModel.carList.observe(viewLifecycleOwner) { cars -> carAdapter.updateCars(cars) }
        viewModel.fetchCars()

        setupLocationFilter()
        binding.filtrers.setOnClickListener { showFilterDialog() }
    }

    /**
     * Sets up the location filter using a Spinner with options from location_options.
     */
    private fun setupLocationFilter() {
        val locationOptions = resources.getStringArray(R.array.location_options)
        val locationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locationOptions)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.ubication.adapter = locationAdapter

        // Set "All" as the initial selection
        binding.ubication.setSelection(0)

        // Apply filter based on selected location
        binding.ubication.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLocation = locationOptions[position]
                if (selectedLocation == "All") {
                    viewModel.clearLocationFilter()  // Clear filter to show all cars
                } else {
                    viewModel.filterByLocation(selectedLocation)  // Apply location filter
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }
    }

    /**
     * Navigates to the car detail fragment with selected car's details.
     */
    private fun navigateToCarDetail(car: CarEntity) {
        val bundle = Bundle().apply {
            putString("carId", car.id)
            putString("ownerId", car.ownerId)
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

    /**
     * Displays the filter dialog and sets up filter options.
     */
    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        setupYearFilter(dialogView)
        setupModelFilter(dialogView)
        setupBrandAutoCompleteFilter(dialogView)
        setupColorFilter(dialogView)
        setupPriceMileageFilter(dialogView)
        setupSectionNavigation(dialogView)

        // Apply filters and dismiss dialog
        dialogView.findViewById<Button>(R.id.btn_apply_filters).setOnClickListener {
            applyFilters(dialogView)
            dialog.dismiss()
        }

        // Restore all filters to initial state and dismiss dialog
        dialogView.findViewById<Button>(R.id.btn_restore_filters).setOnClickListener {
            showResetConfirmationDialog(dialog)
        }

        // Cancel and dismiss dialog
        dialogView.findViewById<Button>(R.id.cancel_action).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

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

        viewModel.applyFilters()
    }

    /**
     * Sets up brand filter with an AutoCompleteTextView and RecyclerView for dynamic filtering.
     */
    private fun setupBrandAutoCompleteFilter(view: View) {
        val autoCompleteBrand: AutoCompleteTextView = view.findViewById(R.id.autoComplete_brand)
        val recyclerViewBrands: RecyclerView = view.findViewById(R.id.recyclerView_brands)

        // Load brands from string-array and initialize the brand adapter
        val brandOptions = resources.getStringArray(R.array.brand_options).toMutableList()
        brandAdapter = BrandAdapter(brandOptions, viewModel.selectedBrands) { selectedBrands ->
            viewModel.selectedBrands = selectedBrands.toMutableSet()
        }

        recyclerViewBrands.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewBrands.adapter = brandAdapter

        // Set up AutoCompleteTextView with brand options for search functionality
        autoCompleteBrand.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, brandOptions))

        // Filter the RecyclerView based on search input in the AutoCompleteTextView
        autoCompleteBrand.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase(Locale.getDefault())
                val filteredBrands = brandOptions.filter { it.lowercase(Locale.getDefault()).contains(query) }
                brandAdapter.updateBrands(filteredBrands)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Set selection from AutoCompleteTextView to RecyclerView
        autoCompleteBrand.setOnItemClickListener { parent, _, position, _ ->
            val selectedBrand = parent.getItemAtPosition(position) as String
            val filteredBrands = brandOptions.filter { it == selectedBrand }
            brandAdapter.updateBrands(filteredBrands)
        }
    }

    /**
     * Sets up color filter with an AutoCompleteTextView populated with unique colors.
     */
    private fun setupColorFilter(view: View) {
        val autoCompleteColor: AutoCompleteTextView = view.findViewById(R.id.autoComplete_color)
        viewModel.uniqueCarColors.observe(viewLifecycleOwner) { colors ->
            autoCompleteColor.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, colors))
        }

        autoCompleteColor.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedColor = parent.getItemAtPosition(position) as String
        }
    }

    /**
     * Sets up price and mileage filters with formatted EditTexts for user-friendly input.
     */
    private fun setupPriceMileageFilter(view: View) {
        val minPrice: EditText = view.findViewById(R.id.editText_min_price)
        val maxPrice: EditText = view.findViewById(R.id.editText_max_price)
        val minMileage: EditText = view.findViewById(R.id.editText_min_mileage)
        val maxMileage: EditText = view.findViewById(R.id.editText_max_mileage)

        listOf(minPrice, maxPrice, minMileage, maxMileage).forEach { addThousandSeparator(it) }
    }

    /**
     * Adds thousand separator formatting to EditText inputs.
     */
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

    /**
     * Sets up navigation within the filter dialog to scroll to different sections.
     */
    private fun setupSectionNavigation(view: View) {
        val scrollView: ScrollView = view.findViewById(R.id.scrollView)
        val sectionsMap = mapOf(
            view.findViewById<TextView>(R.id.nav_option_models) to view.findViewById<View>(R.id.section_models),
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

    /**
     * Shows a confirmation dialog to reset filters and refresh car list.
     */
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

    /**
     * Sets up the year filter with AutoCompleteTextViews for selecting start and end years.
     */
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

    /**
     * Sets up the model filter with an AutoCompleteTextView populated with unique car models.
     */
    private fun setupModelFilter(view: View) {
        val autoCompleteModel: AutoCompleteTextView = view.findViewById(R.id.autoComplete_model)
        viewModel.uniqueCarModels.observe(viewLifecycleOwner) { models ->
            autoCompleteModel.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, models))
        }

        autoCompleteModel.setText(viewModel.selectedModel, false)

        autoCompleteModel.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedModel = parent.getItemAtPosition(position) as String
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}