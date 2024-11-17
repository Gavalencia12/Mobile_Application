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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carhive.Presentation.user.adapter.BrandAdapter
import com.example.carhive.Presentation.user.adapter.CarHomeAdapter
import com.example.carhive.Presentation.user.viewModel.UserViewModel
import com.example.carhive.R
import com.example.carhive.data.model.CarEntity
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

    private var _binding: FragmentUserHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels()
    private lateinit var carAdapter: CarHomeAdapter
    private lateinit var recommendedCarAdapter: CarHomeAdapter
    private lateinit var brandAdapter: BrandAdapter
    private val selectedBrandFilters: MutableSet<String> = mutableSetOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize car and recommended car adapters with click listeners
        carAdapter = CarHomeAdapter(
            carList = emptyList(),
            viewModel = viewModel,
            onFavoriteChecked = { car, isFavorite -> viewModel.toggleFavorite(car, isFavorite) },
            isCarFavorite = { carId, callback -> viewModel.isCarFavorite(carId, callback) },
            onCarClick = { car -> navigateToCarDetail(car) }
        )

        recommendedCarAdapter = CarHomeAdapter(
            carList = emptyList(),
            viewModel = viewModel,
            onFavoriteChecked = { car, isFavorite -> viewModel.toggleFavorite(car, isFavorite) },
            isCarFavorite = { carId, callback -> viewModel.isCarFavorite(carId, callback) },
            onCarClick = { car -> navigateToCarDetail(car) }
        )

        // Set up recyclers with horizontal layouts for car lists
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = carAdapter
        }

        binding.recyclerViewRecomendations.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendedCarAdapter
        }

        // Variable to track if we are in "all cars" view or "default" view
        var isShowingAllCars = false

        // Set up the allCars button to toggle between views
        binding.allCars.setOnClickListener {
            if (isShowingAllCars) {
                // Revert to the default view
                binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                binding.recyclerViewRecomendations.visibility = View.VISIBLE
                binding.recommendedTitle.visibility = View.VISIBLE
                binding.userText.visibility = View.VISIBLE
                binding.recyclerView.setPadding(0, 0, 0, 0) // Remove padding in the default view
                binding.allCars.text = getString(R.string.all)

                // Fetch recommended and nearby cars
                viewModel.fetchCars()
                carAdapter.notifyDataSetChanged()

                isShowingAllCars = false
            } else {
                // Switch to a grid layout and show all cars
                binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
                binding.recyclerViewRecomendations.visibility = View.GONE
                binding.recommendedTitle.visibility = View.GONE
                binding.userText.visibility = View.GONE
                binding.recyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(R.dimen.bottom_padding)) // Add padding at the bottom
                binding.recyclerView.clipToPadding = false // Allow padding in the RecyclerView
                binding.allCars.text = getString(R.string.return_text)

                // Fetch all cars without filters
                viewModel.fetchCars()
                carAdapter.notifyDataSetChanged()

                isShowingAllCars = true
            }
        }

        // Initialize the brand filter buttons
        setupBrandButtons()

        // Observe car list changes
        viewModel.carList.observe(viewLifecycleOwner) { cars ->
            carAdapter.updateCars(cars)
        }

        // Observe recommended car list changes
        viewModel.recommendedCarList.observe(viewLifecycleOwner) { recommendedCars ->
            recommendedCarAdapter.updateCars(recommendedCars)
        }

        // Load cars and set up filter controls
        viewModel.fetchCars()
        setupLocationFilter()

        // Set up asynchronous model search
        setupModelSearch()

        // Show filter dialog when the filters button is clicked
        binding.filtrers.setOnClickListener { showFilterDialog() }
    }

    private fun setupBrandButtons() {
        val brandButtons = mapOf(
            binding.toyota to "Toyota",
            binding.honda to "Honda",
            binding.chevrolet to "Chevrolet",
            binding.ford to "Ford",
            binding.nissan to "Nissan",
            binding.volkswagen to "Volkswagen",
            binding.bmw to "BMW",
            binding.mercedes to "Mercedes",
            binding.audi to "Audi",
            binding.hyundai to "Hyundai"
        )

        brandButtons.forEach { (button, brand) ->
            button.setOnClickListener {
                button.isSelected = !button.isSelected // Alternar el estado seleccionado

                if (selectedBrandFilters.contains(brand)) {
                    // Deseleccionar marca
                    selectedBrandFilters.remove(brand)
                    button.setBackgroundResource(R.drawable.default_button_background)
                } else {
                    // Seleccionar marca
                    selectedBrandFilters.add(brand)
                    button.setBackgroundResource(R.drawable.selected_button_background)
                }

                // Reorganizar botones y aplicar filtros
                reorganizeBrandButtons(brandButtons)
                applyBrandFilters()
            }
        }
    }


    private fun reorganizeBrandButtons(brandButtons: Map<ImageButton, String>) {
        val linearLayout = binding.llBrands.findViewById<LinearLayout>(R.id.llBrandsContainer)

        // Elimina todos los botones del `LinearLayout`
        linearLayout.removeAllViews()

        // Lista de botones seleccionados primero, seguidos por los no seleccionados
        val sortedButtons = brandButtons.entries
            .sortedBy { if (selectedBrandFilters.contains(it.value)) 0 else 1 }
            .map { it.key }

        // Agrega los botones al contenedor en el nuevo orden
        sortedButtons.forEach { button ->
            linearLayout.addView(button)

            // Agregar espacio entre botones
            val space = Space(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(10, LinearLayout.LayoutParams.MATCH_PARENT)
            }
            linearLayout.addView(space)
        }
    }


    private fun applyBrandFilters() {
        if (selectedBrandFilters.isEmpty()) {
            // Mostrar todos los autos si no hay filtros activos
            binding.recyclerViewRecomendations.visibility = View.VISIBLE
            binding.recommendedTitle.visibility = View.VISIBLE
            binding.userText.visibility = View.VISIBLE
            binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            viewModel.fetchCars()
        } else {
            // Ocultar "Recommended Cars" y mostrar solo autos filtrados
            binding.recyclerViewRecomendations.visibility = View.GONE
            binding.recommendedTitle.visibility = View.GONE
            binding.userText.visibility = View.GONE
            binding.recyclerView.layoutManager = GridLayoutManager(context, 2)

            viewModel.filterCarsBySelectedBrands(selectedBrandFilters) { filteredCars ->
                carAdapter.updateCars(filteredCars)
            }
        }
    }




    // Setup for model search with autocomplete
    private fun setupModelSearch() {
        val autoCompleteModelSearch: AutoCompleteTextView = binding.autoCompleteModelSearch

        // Set unique car models in the adapter for the AutoCompleteTextView
        viewModel.uniqueCarModels.observe(viewLifecycleOwner) { models ->
            autoCompleteModelSearch.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, models)
            )
        }

        // Apply filters with delay on text change
        var searchJob: Job? = null
        autoCompleteModelSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                val query = s.toString()
                if (query.isEmpty()) {
                    viewModel.selectedModel = null
                    viewModel.fetchCars()
                } else {
                    searchJob = viewLifecycleOwner.lifecycleScope.launch {
                        delay(300)
                        viewModel.selectedModel = query
                        viewModel.applyFilters()
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Display the filter dialog for filtering options
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

        // Apply or reset filters based on dialog actions
        dialogView.findViewById<Button>(R.id.btn_apply_filters).setOnClickListener {
            applyFilters(dialogView)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_restore_filters).setOnClickListener {
            showResetConfirmationDialog(dialog)
        }

        // Cancel and close the dialog
        dialogView.findViewById<Button>(R.id.cancel_action).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Apply selected filters to the car list
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

    // Show confirmation dialog to reset filters
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

    // Set up brand filter with autocomplete and filtered options
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
            Pair(R.id.black_button, "Black")
        )

        colorButtons.forEach { (buttonId, color) ->
            val button = view.findViewById<Button>(buttonId)

            button.setBackgroundResource(R.drawable.selected_circle)
            button.isSelected = viewModel.selectedColors.contains(color)

            button.setOnClickListener {
                if (viewModel.selectedColors.contains(color)) {
                    viewModel.selectedColors.remove(color)
                    button.isSelected = false
                    Toast.makeText(requireContext(), "Color deselected: $color", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.selectedColors.add(color)
                    button.isSelected = true
                    Toast.makeText(requireContext(), "Color selected: $color", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Set up input filters with thousand separator formatting
    private fun setupPriceMileageFilter(view: View) {
        val minPrice: EditText = view.findViewById(R.id.editText_min_price)
        val maxPrice: EditText = view.findViewById(R.id.editText_max_price)
        val minMileage: EditText = view.findViewById(R.id.editText_min_mileage)
        val maxMileage: EditText = view.findViewById(R.id.editText_max_mileage)

        listOf(minPrice, maxPrice, minMileage, maxMileage).forEach { addThousandSeparator(it) }
    }

    // Adds thousand separators for price and mileage inputs
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

    // Sets up navigation between filter sections
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

    // Initialize location filter options in the dropdown
    private fun setupLocationFilter() {
        val locationOptions = resources.getStringArray(R.array.location_options)
        val locationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locationOptions)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.ubication.adapter = locationAdapter
        binding.ubication.setSelection(0)
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

    // Navigate to car detail screen with car details
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



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
