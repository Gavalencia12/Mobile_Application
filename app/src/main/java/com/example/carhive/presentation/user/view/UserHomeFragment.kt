package com.example.carhive.presentation.user.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
    private val viewModel: UserViewModel by activityViewModels()
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        brandAdapter = BrandAdapter(mutableListOf(), viewModel.selectedBrands) { selectedBrands ->
            viewModel.selectedBrands = selectedBrands.toMutableSet()
            viewModel.applyFilters()
        }

        // Configurar RecyclerViews con layouts horizontales para listas de autos
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = carAdapter
        }

        binding.recyclerViewRecomendations.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendedCarAdapter
        }

        // Observar cambios en la lista de marcas para actualizar el BrandAdapter
        viewModel.brandList.observe(viewLifecycleOwner) { brands ->
            brandAdapter.updateBrands(brands) // Actualiza el brandAdapter con las marcas obtenidas dinámicamente de Firebase
        }

        // Variable para rastrear si estamos en la vista "todos los autos" o en la vista "predeterminada"
        var isShowingAllCars = false

        // Obtener autos y marcas
        viewModel.fetchCars()
        viewModel.fetchBrandsFromCars()
        viewModel.fetchUniqueCarModels()

        // Configurar el botón "Todos los Autos" para alternar entre vistas
        binding.allCars.setOnClickListener {
            if (isShowingAllCars) {
                // Revertir a la vista predeterminada
                binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                binding.recyclerViewRecomendations.visibility = View.VISIBLE
                binding.recommendedTitle.visibility = View.VISIBLE
                binding.userText.visibility = View.VISIBLE
                binding.recyclerView.setPadding(0, 0, 0, 0) // Quitar padding en la vista predeterminada
                binding.allCars.text = getString(R.string.all)

                // Obtener autos recomendados y cercanos
                viewModel.fetchCars()
                carAdapter.notifyDataSetChanged()

                isShowingAllCars = false
            } else {
                // Cambiar a un layout de cuadrícula y mostrar todos los autos
                binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
                binding.recyclerViewRecomendations.visibility = View.GONE
                binding.recommendedTitle.visibility = View.GONE
                binding.userText.visibility = View.GONE
                binding.recyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(R.dimen.bottom_padding)) // Añadir padding al fondo
                binding.recyclerView.clipToPadding = false // Permitir padding en el RecyclerView
                binding.allCars.text = getString(R.string.return_text)

                // Obtener todos los autos sin filtros
                viewModel.fetchCars()
                carAdapter.notifyDataSetChanged()

                isShowingAllCars = true
            }
        }

        // Inicializar los botones de filtro de marca
        setupBrandButtons()

        // Observar cambios en la lista de autos
        viewModel.carList.observe(viewLifecycleOwner) { cars ->
            Log.d("UserHomeFragment", "Recibiendo lista de autos filtrados: ${cars.size} autos")
            carAdapter.updateCars(cars)
        }

        // Observar cambios en la lista de autos recomendados
        viewModel.recommendedCarList.observe(viewLifecycleOwner) { recommendedCars ->
            recommendedCarAdapter.updateCars(recommendedCars)
        }

        setupLocationFilter()
        setupModelSearch() // Llamada al método que configura la búsqueda de modelos de carros

        // Mostrar el cuadro de diálogo de filtros cuando se hace clic en el botón de filtros
        binding.filtrers.setOnClickListener { showFilterDialog() }
    }

    private fun navigateToCarDetail(car: CarEntity) {
        val bundle = Bundle().apply {
            putString("carId", car.id)
            putString("carModel", car.modelo)
            putString("carBrand", car.brand)
            putString("carPrice", car.price.toString())
            putString("carColor", car.color)
            putString("carDescription", car.description)
            putString("carTransmission", car.transmission)
            putString("carFuelType", car.fuelType)
            putInt("carDoors", car.doors)
            putString("carEngineCapacity", car.engineCapacity.toString())
            putString("carLocation", car.location)
            putString("carCondition", car.condition)
            putInt("carPreviousOwners", car.previousOwners)
            putInt("carViews", car.views)
            putString("carMileage", car.mileage.toString())
            putString("carYear", car.year.toString())
            putString("carOwnerId", car.ownerId)
            putStringArrayList("carImageUrls", car.imageUrls?.let { ArrayList(it) })
        }
        findNavController().navigate(R.id.action_userHomeFragment_to_carDetailFragment, bundle)
    }

    // Método para configurar el filtro de ubicación
    private fun setupLocationFilter() {
        val locationOptions = resources.getStringArray(R.array.location_options)
        val locationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locationOptions)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.ubication.adapter = locationAdapter
        binding.ubication.setSelection(0)
        binding.ubication.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLocation = locationOptions[position]
                if (selectedLocation.equals("All", ignoreCase = true)) {
                    viewModel.clearLocationFilter()
                } else {
                    viewModel.filterByLocation(selectedLocation)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // Método para configurar el filtro de color
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
            Pair(R.id.brown_button, "Brown"),
        )

        colorButtons.forEach { (buttonId, color) ->
            val button = view.findViewById<Button>(buttonId)

            // Establecer el estado inicial del botón
            button.isSelected = viewModel.selectedColors.contains(color)
            button.setBackgroundResource(
                if (button.isSelected) R.drawable.selected_circle else R.drawable.selected_circle
            )

            button.setOnClickListener {
                if (viewModel.selectedColors.contains(color)) {
                    viewModel.selectedColors.remove(color)
                    button.isSelected = false
                    button.setBackgroundResource(R.drawable.selected_circle)
                    Toast.makeText(requireContext(), "Color deseleccionado: $color", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.selectedColors.add(color)
                    button.isSelected = true
                    button.setBackgroundResource(R.drawable.selected_circle)
                    Toast.makeText(requireContext(), "Color seleccionado: $color", Toast.LENGTH_SHORT).show()
                }
                viewModel.applyFilters()
            }
        }
    }

    // Método para configurar el filtro de precio y kilometraje
    private fun setupPriceMileageFilter(view: View) {
        val minPrice: EditText = view.findViewById(R.id.editText_min_price)
        val maxPrice: EditText = view.findViewById(R.id.editText_max_price)
        val minMileage: EditText = view.findViewById(R.id.editText_min_mileage)
        val maxMileage: EditText = view.findViewById(R.id.editText_max_mileage)

        minPrice.setText(formatWithThousandSeparator(viewModel.priceRange.first))
        maxPrice.setText(viewModel.priceRange.second?.let { formatWithThousandSeparator(it) } ?: "")

        minMileage.setText(formatWithThousandSeparator(viewModel.mileageRange.first))
        maxMileage.setText(viewModel.mileageRange.second?.let { formatWithThousandSeparator(it) } ?: "")

        listOf(minPrice, maxPrice, minMileage, maxMileage).forEach { addThousandSeparator(it) }

        // Añadir listeners para actualizar el ViewModel cuando cambian los valores
        minPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val min = s.toString().replace(",", "").toIntOrNull() ?: 0
                viewModel.priceRange = min to viewModel.priceRange.second
                viewModel.applyFilters()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        maxPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val max = s.toString().replace(",", "").toIntOrNull()
                viewModel.priceRange = viewModel.priceRange.first to max
                viewModel.applyFilters()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        minMileage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val min = s.toString().replace(",", "").toIntOrNull() ?: 0
                viewModel.mileageRange = min to viewModel.mileageRange.second
                viewModel.applyFilters()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        maxMileage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val max = s.toString().replace(",", "").toIntOrNull()
                viewModel.mileageRange = viewModel.mileageRange.first to max
                viewModel.applyFilters()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Formatea los valores con separadores de miles
    private fun formatWithThousandSeparator(value: Int): String {
        return NumberFormat.getNumberInstance(Locale.US).format(value)
    }

    // Agrega separadores de miles a los campos de texto de precio y kilometraje
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

    // Navega entre las secciones de filtro
    private fun setupSectionNavigation(view: View) {
        val scrollView: ScrollView = view.findViewById(R.id.scrollView)
        val sectionsMap = mapOf(
            view.findViewById<TextView>(R.id.nav_option_brands) to view.findViewById(R.id.section_brands),
            view.findViewById<TextView>(R.id.nav_option_price) to view.findViewById(R.id.section_price),
            view.findViewById<TextView>(R.id.nav_option_mileage) to view.findViewById(R.id.section_mileage),
            view.findViewById<TextView>(R.id.nav_option_color) to view.findViewById(R.id.section_color),
            view.findViewById<TextView>(R.id.nav_option_year) to view.findViewById<View>(R.id.section_year),
            view.findViewById<TextView>(R.id.nav_option_transmission) to view.findViewById<View>(R.id.section_transmision),
            view.findViewById<TextView>(R.id.nav_option_fuelType) to view.findViewById<View>(R.id.section_fuelType),
            view.findViewById<TextView>(R.id.nav_option_engineCapacity) to view.findViewById<View>(R.id.section_engineCapacity),
            view.findViewById<TextView>(R.id.nav_option_condition) to view.findViewById<View>(R.id.section_condition)
        )

        sectionsMap.forEach { (navOption, targetSection) ->
            navOption.setOnClickListener {
                scrollView.post { scrollView.smoothScrollTo(0, targetSection.top) }
            }
        }
    }

    // Método para configurar los filtros de condición
    private fun setupConditionFilter(view: View) {
        val spinnerCondition: Spinner = view.findViewById(R.id.spinner_condition)

        val conditionOptions = resources.getStringArray(R.array.condition_options).toList()
        val selectedIndex = conditionOptions.indexOf(viewModel.selectedCondition ?: "All")
        spinnerCondition.setSelection(if (selectedIndex >= 0) selectedIndex else 0)

        spinnerCondition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCondition = parent.getItemAtPosition(position).toString()
                viewModel.selectedCondition = if (selectedCondition.equals("All", ignoreCase = true)) null else selectedCondition
                viewModel.applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                viewModel.selectedCondition = null
                viewModel.applyFilters()
            }
        }
    }

    // Método para configurar la búsqueda del modelo
    private fun setupModelSearch() {
        val autoCompleteModelSearch: AutoCompleteTextView = binding.autoCompleteModelSearch

        // Establecer modelos únicos en el adaptador para el AutoCompleteTextView
        viewModel.uniqueCarModels.observe(viewLifecycleOwner) { models ->
            autoCompleteModelSearch.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, models)
            )
        }

        // Aplicar filtros con retraso al cambiar el texto
        var searchJob: Job? = null
        autoCompleteModelSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                val query = s.toString()
                if (query.isEmpty()) {
                    viewModel.selectedModel = null
                    viewModel.applyFilters()
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

    // Mostrar el cuadro de diálogo para los filtros
    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Configurar los filtros dentro del diálogo con el estado actual del ViewModel
        setupYearFilter(dialogView)
        setupBrandAutoCompleteFilter(dialogView)
        setupColorFilter(dialogView)
        setupPriceMileageFilter(dialogView)
        setupSectionNavigation(dialogView)
        setupConditionFilter(dialogView)

        // Configurar los nuevos filtros
        setupTransmissionFilter(dialogView)
        setupFuelTypeFilter(dialogView)
        setupEngineCapacityFilter(dialogView)

        // Aplicar o restaurar filtros según las acciones del diálogo
        dialogView.findViewById<Button>(R.id.btn_apply_filters).setOnClickListener {
            applyFilters(dialogView)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_restore_filters).setOnClickListener {
            showResetConfirmationDialog(dialog)
        }

        // Cancelar y cerrar el diálogo
        dialogView.findViewById<Button>(R.id.cancel_action).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Método para mostrar el cuadro de confirmación para restaurar los filtros
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

    private fun setupYearFilter(view: View) {
        // Rango de años disponibles
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (1970..currentYear).map { it.toString() }
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, years)

        val startYearView: AutoCompleteTextView = view.findViewById(R.id.start_year)
        val endYearView: AutoCompleteTextView = view.findViewById(R.id.end_year)

        // Configuración de los adaptadores para el filtro de año
        startYearView.setAdapter(yearAdapter)
        endYearView.setAdapter(yearAdapter)

        // Establece los valores preexistentes en los filtros si ya se aplicaron
        viewModel.yearRange?.let { (start, end) ->
            startYearView.setText(start.toString(), false)
            endYearView.setText(end.toString(), false)
        }

        // Añadir listeners para actualizar el ViewModel cuando cambian los valores
        startYearView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val startYear = s.toString().toIntOrNull()
                val endYear = endYearView.text.toString().toIntOrNull()
                viewModel.yearRange = if (startYear != null || endYear != null) {
                    (startYear ?: 1970) to (endYear ?: Calendar.getInstance().get(Calendar.YEAR))
                } else {
                    null
                }
                viewModel.applyFilters()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        endYearView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val startYear = startYearView.text.toString().toIntOrNull()
                val endYear = s.toString().toIntOrNull()
                viewModel.yearRange = if (startYear != null || endYear != null) {
                    (startYear ?: 1970) to (endYear ?: Calendar.getInstance().get(Calendar.YEAR))
                } else {
                    null
                }
                viewModel.applyFilters()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupBrandAutoCompleteFilter(view: View) {
        val autoCompleteBrand: AutoCompleteTextView = view.findViewById(R.id.autoComplete_brand)
        val recyclerViewBrands: RecyclerView = view.findViewById(R.id.recyclerView_brands)

        // Inicializa el BrandAdapter con la lista vacía y el callback para los cambios
        brandAdapter = BrandAdapter(mutableListOf(), viewModel.selectedBrands) { selectedBrands ->
            viewModel.selectedBrands = selectedBrands.toMutableSet()
            viewModel.applyFilters()
        }

        // Configura el RecyclerView para mostrar las marcas de autos
        recyclerViewBrands.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewBrands.adapter = brandAdapter

        // Espera a que la lista de marcas se cargue dinámicamente desde Firebase
        viewModel.brandList.observe(viewLifecycleOwner) { brands ->
            // Actualiza el BrandAdapter con las marcas obtenidas desde Firebase
            brandAdapter.updateBrands(brands)

            // Configura el AutoCompleteTextView para filtrar las marcas a medida que el usuario escribe
            autoCompleteBrand.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, brands)
            )
        }

        // Filtra las marcas mientras el usuario escribe en el AutoCompleteTextView
        autoCompleteBrand.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase(Locale.getDefault())
                val filteredBrands = viewModel.brandList.value?.filter { it.lowercase(Locale.getDefault()).contains(query) }
                filteredBrands?.let { brandAdapter.updateBrands(it) }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Maneja la selección de una marca desde la lista de sugerencias
        autoCompleteBrand.setOnItemClickListener { parent, _, position, _ ->
            val selectedBrand = parent.getItemAtPosition(position) as String
            val filteredBrands = viewModel.brandList.value?.filter { it.equals(selectedBrand, ignoreCase = true) }
            filteredBrands?.let { brandAdapter.updateBrands(it) }
            viewModel.selectedBrands.add(selectedBrand)
            viewModel.applyFilters()
        }
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
            // Establecer el estado inicial de cada botón basado en el ViewModel
            button.isSelected = viewModel.selectedBrands.contains(brand)
            button.setBackgroundResource(
                if (button.isSelected) R.drawable.selected_button_background else R.drawable.default_button_background
            )

            button.setOnClickListener {
                button.isSelected = !button.isSelected

                if (viewModel.selectedBrands.contains(brand)) {
                    viewModel.selectedBrands.remove(brand)
                    button.setBackgroundResource(R.drawable.default_button_background)
                } else {
                    viewModel.selectedBrands.add(brand)
                    button.setBackgroundResource(R.drawable.selected_button_background)
                }

                // Reorganizar los botones si es necesario
                reorganizeBrandButtons(brandButtons)

                // Aplicar los filtros después de la selección
                viewModel.applyFilters()
            }
        }
    }

    private fun reorganizeBrandButtons(brandButtons: Map<ImageButton, String>) {
        val linearLayout = binding.llBrands.findViewById<LinearLayout>(R.id.llBrandsContainer)

        linearLayout.removeAllViews()

        val sortedButtons = brandButtons.entries
            .sortedBy { if (viewModel.selectedBrands.contains(it.value)) 0 else 1 }
            .map { it.key }

        sortedButtons.forEach { button ->
            linearLayout.addView(button)

            val space = Space(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(10, LinearLayout.LayoutParams.MATCH_PARENT)
            }
            linearLayout.addView(space)
        }
    }

    // Método para aplicar filtros y actualizar el ViewModel
    private fun applyFilters(view: View) {
        val startYearInput = view.findViewById<AutoCompleteTextView>(R.id.start_year).text.toString()
        val endYearInput = view.findViewById<AutoCompleteTextView>(R.id.end_year).text.toString()

        val startYear = startYearInput.toIntOrNull()
        val endYear = endYearInput.toIntOrNull()

        viewModel.yearRange = if (startYear != null || endYear != null) {
            (startYear ?: viewModel.yearRange?.first ?: 1970) to
                    (endYear ?: viewModel.yearRange?.second ?: Calendar.getInstance().get(Calendar.YEAR))
        } else {
            null
        }

        val minPrice = view.findViewById<EditText>(R.id.editText_min_price).text.toString().replace(",", "").toIntOrNull() ?: 0
        val maxPrice = view.findViewById<EditText>(R.id.editText_max_price).text.toString().replace(",", "").toIntOrNull()
        viewModel.priceRange = minPrice to maxPrice

        val minMileage = view.findViewById<EditText>(R.id.editText_min_mileage).text.toString().replace(",", "").toIntOrNull() ?: 0
        val maxMileage = view.findViewById<EditText>(R.id.editText_max_mileage).text.toString().replace(",", "").toIntOrNull()
        viewModel.mileageRange = minMileage to maxMileage

        // Obtener los valores mínimos y máximos de la capacidad del motor
        val minEngineCapacity = view.findViewById<EditText>(R.id.editText_min_engineCapacity).text.toString().toDoubleOrNull()
        val maxEngineCapacity = view.findViewById<EditText>(R.id.editText_max_engineCapacity).text.toString().toDoubleOrNull()

        viewModel.engineCapacityRange = minEngineCapacity to maxEngineCapacity

        viewModel.applyFilters()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Implementación de los tres nuevos métodos de filtro
    private fun setupTransmissionFilter(view: View) {
        val spinnerTransmission: Spinner = view.findViewById(R.id.spinner_transmision)

        // Obtener las opciones de transmisión desde los recursos
        val transmissionOptions = resources.getStringArray(R.array.transmision_options)
        val transmissionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, transmissionOptions)
        transmissionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTransmission.adapter = transmissionAdapter

        // Determinar la posición seleccionada basada en el ViewModel
        val selectedTransmission = viewModel.selectedTransmission ?: "All"
        val selectedIndex = transmissionOptions.indexOf(selectedTransmission)
        spinnerTransmission.setSelection(if (selectedIndex >= 0) selectedIndex else 0)

        // Establecer el listener para manejar cambios de selección
        spinnerTransmission.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = transmissionOptions[position]
                viewModel.selectedTransmission = if (selected.equals("All", ignoreCase = true)) null else selected
                viewModel.applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                viewModel.selectedTransmission = null
                viewModel.applyFilters()
            }
        }
    }

    private fun setupFuelTypeFilter(view: View) {
        val spinnerFuelType: Spinner = view.findViewById(R.id.spinner_fuelType)

        // Obtener las opciones de tipo de combustible desde los recursos
        val fuelTypeOptions = resources.getStringArray(R.array.fuel_type_options_filter)
        val fuelTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, fuelTypeOptions)
        fuelTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFuelType.adapter = fuelTypeAdapter

        // Determinar la posición seleccionada basada en el ViewModel
        val selectedFuelType = viewModel.selectedFuelType ?: "All"
        val selectedIndex = fuelTypeOptions.indexOf(selectedFuelType)
        spinnerFuelType.setSelection(if (selectedIndex >= 0) selectedIndex else 0)

        // Establecer el listener para manejar cambios de selección
        spinnerFuelType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = fuelTypeOptions[position]
                viewModel.selectedFuelType = if (selected.equals("All", ignoreCase = true)) null else selected
                viewModel.applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                viewModel.selectedFuelType = null
                viewModel.applyFilters()
            }
        }
    }

    private fun setupEngineCapacityFilter(view: View) {
        val minEngineCapacity: EditText = view.findViewById(R.id.editText_min_engineCapacity)
        val maxEngineCapacity: EditText = view.findViewById(R.id.editText_max_engineCapacity)

        // Prellenar los campos con los valores actuales del ViewModel
        minEngineCapacity.setText(viewModel.engineCapacityRange.first?.toString() ?: "")
        maxEngineCapacity.setText(viewModel.engineCapacityRange.second?.toString() ?: "")

        // Añadir TextWatchers para actualizar el ViewModel cuando cambian los valores
        minEngineCapacity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val min = s.toString().toDoubleOrNull()
                viewModel.engineCapacityRange = min to viewModel.engineCapacityRange.second
                viewModel.applyFilters()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        maxEngineCapacity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val max = s.toString().toDoubleOrNull()
                viewModel.engineCapacityRange = viewModel.engineCapacityRange.first to max
                viewModel.applyFilters()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Opcional: Formatear los campos para permitir solo un decimal
        listOf(minEngineCapacity, maxEngineCapacity).forEach { addDecimalSeparator(it) }
    }

    // Método para agregar un separador decimal (opcional)
    private fun addDecimalSeparator(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    editText.removeTextChangedListener(this)
                    val cleanString = s.toString().replace(".", "")
                    val formatted = cleanString.toDoubleOrNull()?.let {
                        String.format(Locale.US, "%.1f", it / 10)
                    } ?: ""
                    current = formatted
                    editText.setText(formatted)
                    if (formatted.isNotEmpty()) {
                        editText.setSelection(formatted.length)
                    }
                    editText.addTextChangedListener(this)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}
