package com.example.carhive.Presentation.user.viewModel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Data.model.UserEntity
import com.example.carhive.Domain.usecase.auth.GetCurrentUserIdUseCase
import com.example.carhive.Domain.usecase.database.GetAllCarsFromDatabaseUseCase
import com.example.carhive.Domain.usecase.database.GetUserDataUseCase
import com.example.carhive.Domain.usecase.favorites.AddCarToFavoritesUseCase
import com.example.carhive.Domain.usecase.favorites.RemoveCarFromFavoritesUseCase
import com.example.carhive.R
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    private val getAllCarsFromDatabaseUseCase: GetAllCarsFromDatabaseUseCase,
    private val addCarToFavoritesUseCase: AddCarToFavoritesUseCase,
    private val removeCarFromFavoritesUseCase: RemoveCarFromFavoritesUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getUserDataUseCase: GetUserDataUseCase,
    private val firebaseDatabase: FirebaseDatabase
) : AndroidViewModel(application) {

    // Default list of car brands retrieved from string resources
    val defaultBrands = application.resources.getStringArray(R.array.brand_options).toList()

    // LiveData to hold the list of cars
    private val _carList = MutableLiveData<List<CarEntity>>()
    val carList: LiveData<List<CarEntity>> get() = _carList

    // LiveData to hold user data
    private val _userData = MutableLiveData<UserEntity?>()
    val userData: LiveData<UserEntity?> get() = _userData

    private var allCars: List<CarEntity> = emptyList()

    // Selected filters
    var selectedBrands: MutableSet<String> = mutableSetOf()
    var selectedModel: String? = null
    var yearRange: Pair<Int, Int>? = null
    var priceRange: Pair<Int, Int?> = 0 to null
    var mileageRange: Pair<Int, Int?> = 0 to null

    // Standardized selected color (first letter capitalized)
    var selectedColor: String? = null
        set(value) {
            field = value?.replaceFirstChar { it.uppercase() }
        }

    // Current location filter
    private var selectedLocation: String? = null

    // Unique car models and colors for filter options
    private val _uniqueCarModels = MutableLiveData<List<String>>()
    val uniqueCarModels: LiveData<List<String>> get() = _uniqueCarModels

    private val _uniqueCarColors = MutableLiveData<List<String>>()
    val uniqueCarColors: LiveData<List<String>> get() = _uniqueCarColors

    /**
     * Fetches the list of cars and updates unique model and color lists for filters.
     */
    fun fetchCars() {
        viewModelScope.launch {
            val result = getAllCarsFromDatabaseUseCase()
            if (result.isSuccess) {
                allCars = result.getOrNull()?.filter { !it.sold && it.approved } ?: emptyList()
                _carList.value = allCars
                loadUniqueCarModels()
                loadUniqueCarColors()
            } else {
                showToast(R.string.error_fetching_cars)
            }
        }
    }

    // Load unique car models from the list of cars
    private fun loadUniqueCarModels() {
        val models = allCars.map { it.modelo }.distinct()
        _uniqueCarModels.value = models
    }

    // Load unique car colors from the list of cars, capitalized
    private fun loadUniqueCarColors() {
        val colors = allCars.map { it.color.replaceFirstChar { it.uppercase() } }.distinct()
        _uniqueCarColors.value = colors
    }

    /**
     * Applies filters to the list of cars based on selected criteria, including location.
     */
    fun applyFilters() {
        val filteredCars = allCars.filter { car ->
            val matchesBrand = selectedBrands.isEmpty() || selectedBrands.contains(car.brand)
            val matchesModel = selectedModel == null || car.modelo == selectedModel
            val matchesYear = yearRange?.let { (min, max) -> car.year.toIntOrNull()?.let { it in min..max } } ?: true
            val matchesColor = selectedColor == null || car.color.equals(selectedColor, ignoreCase = true)
            val matchesLocation = selectedLocation == null || car.location == selectedLocation

            val carPrice = car.price.toIntOrNull() ?: 0
            val matchesPrice = when {
                priceRange.second != null -> carPrice in priceRange.first..priceRange.second!!
                else -> carPrice >= priceRange.first
            }

            val carMileage = car.mileage.toIntOrNull() ?: 0
            val matchesMileage = when {
                mileageRange.second != null -> carMileage in mileageRange.first..mileageRange.second!!
                else -> carMileage >= mileageRange.first
            }

            matchesBrand && matchesModel && matchesYear && matchesColor && matchesLocation && matchesPrice && matchesMileage
        }
        _carList.value = filteredCars
    }

    /**
     * Resets all filters and shows the complete list of cars.
     */
    fun clearFilters() {
        selectedBrands.clear()
        selectedModel = null
        yearRange = null
        selectedColor = null
        priceRange = 0 to null
        mileageRange = 0 to null
        selectedLocation = null
        _carList.value = allCars
    }

    /**
     * Filters cars by location.
     */
    fun filterByLocation(location: String) {
        selectedLocation = location
        applyFilters() // Reapply filters with updated location
    }

    /**
     * Clears the location filter.
     */
    fun clearLocationFilter() {
        selectedLocation = null
        applyFilters() // Reapply filters without location restriction
    }

    /**
     * Checks if a car is marked as a favorite for the current user.
     */
    fun isCarFavorite(carId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val currentUser = getCurrentUserIdUseCase()
            val userId = currentUser.getOrNull() ?: return@launch

            firebaseDatabase.getReference("Favorites/UserFavorites")
                .child(userId)
                .child(carId)
                .get()
                .addOnSuccessListener { snapshot -> callback(snapshot.exists()) }
                .addOnFailureListener {
                    showToast(R.string.error_fetching_favorite_status)
                    callback(false)
                }
        }
    }

    /**
     * Toggles a car's favorite status for the current user.
     */
    fun toggleFavorite(car: CarEntity, isFavorite: Boolean) {
        viewModelScope.launch {
            val currentUser = getCurrentUserIdUseCase()
            val userId = currentUser.getOrNull() ?: return@launch

            val resultUser = getUserDataUseCase(userId)
            if (resultUser.isSuccess) {
                val user = resultUser.getOrNull()?.firstOrNull()
                val fullName = "${user?.firstName} ${user?.lastName}"

                if (isFavorite) {
                    val result = addCarToFavoritesUseCase(userId, fullName, car.id, car.id, car.ownerId)
                    if (result.isSuccess) showToast(R.string.car_added_to_favorites)
                    else showToast(R.string.error_adding_favorite)
                } else {
                    val result = removeCarFromFavoritesUseCase(userId, car.id)
                    if (result.isSuccess) showToast(R.string.car_removed_from_favorites)
                    else showToast(R.string.error_removing_favorite)
                }
            } else {
                showToast(R.string.error_fetching_user_data)
            }
        }
    }

    // Show toast message with the specified string resource ID
    private fun showToast(messageResId: Int) {
        Toast.makeText(getApplication(), getApplication<Application>().getString(messageResId), Toast.LENGTH_SHORT).show()
    }
}
