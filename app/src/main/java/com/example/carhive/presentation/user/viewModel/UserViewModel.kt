package com.example.carhive.Presentation.user.viewModel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.model.Car
import com.example.carhive.data.model.CarEntity
import com.example.carhive.data.model.HistoryEntity
import com.example.carhive.data.model.UserEntity
import com.example.carhive.Domain.usecase.auth.GetCurrentUserIdUseCase
import com.example.carhive.Domain.usecase.database.GetAllCarsFromDatabaseUseCase
import com.example.carhive.Domain.usecase.database.GetCarUserInDatabaseUseCase
import com.example.carhive.Domain.usecase.database.GetUserDataUseCase
import com.example.carhive.Domain.usecase.database.UpdateCarToDatabaseUseCase
import com.example.carhive.Domain.usecase.favorites.AddCarToFavoritesUseCase
import com.example.carhive.Domain.usecase.favorites.RemoveCarFromFavoritesUseCase
import com.example.carhive.Domain.usecase.notifications.AddNotificationUseCase
import com.example.carhive.Domain.usecase.notifications.ListenForNewFavoritesUseCase
import com.example.carhive.Presentation.user.adapter.CarHomeAdapter
import com.example.carhive.R
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.example.carhive.Presentation.user.adapter.BrandAdapter

@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    private val getAllCarsFromDatabaseUseCase: GetAllCarsFromDatabaseUseCase,
    private val addCarToFavoritesUseCase: AddCarToFavoritesUseCase,
    private val removeCarFromFavoritesUseCase: RemoveCarFromFavoritesUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getUserDataUseCase: GetUserDataUseCase,
    private val firebaseDatabase: FirebaseDatabase,
    private val updateCarToDatabaseUseCase: UpdateCarToDatabaseUseCase,
    private val addNotificationUseCase: AddNotificationUseCase,
    private val listenForNewFavoritesUseCase: ListenForNewFavoritesUseCase,
    private val getCarUserInDatabaseUseCase: GetCarUserInDatabaseUseCase
) : AndroidViewModel(application) {

    // Default list of car brands retrieved from string resources
    val defaultBrands = application.resources.getStringArray(R.array.brand_options).toList()

    // LiveData to hold the list of cars
    private val _carList = MutableLiveData<List<CarEntity>>()
    val carList: LiveData<List<CarEntity>> get() = _carList

    private val _recommendedCarList = MutableLiveData<List<CarEntity>>()
    val recommendedCarList: LiveData<List<CarEntity>> get() = _recommendedCarList

    // LiveData to hold user data
    private val _userData = MutableLiveData<UserEntity?>()
    val userData: LiveData<UserEntity?> get() = _userData

    private var allCars: List<CarEntity> = emptyList()
    private var favoriteCounts: Map<String, Int> = emptyMap() // Map for favorite counts

    private lateinit var recommendedCarAdapter: CarHomeAdapter

    private val _brandList = MutableLiveData<List<String>>()
    val brandList: LiveData<List<String>> get() = _brandList


    // Selected filters
    var selectedBrands: MutableSet<String> = mutableSetOf()
    var selectedModel: String? = null
    var yearRange: Pair<Int, Int>? = null
    var priceRange: Pair<Int, Int?> = 0 to null
    var mileageRange: Pair<Int, Int?> = 0 to null
    var selectedColors: MutableSet<String> = mutableSetOf()

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
                fetchRecommendedCars() // Update recommended cars after fetching all cars
            } else {
                showToast(R.string.error_fetching_cars)
            }
        }
    }

    fun fetchBrandsFromCars() {
        viewModelScope.launch {
            val result = getAllCarsFromDatabaseUseCase()
            if (result.isSuccess) {
                val cars = result.getOrNull()
                val uniqueBrands = cars?.map { it.brand }?.distinct() ?: emptyList()
                _brandList.value = uniqueBrands // Actualiza el LiveData de marcas
            } else {
                Log.e("UserViewModel", "Error al obtener los autos")
            }
        }
    }

    /**
     * Fetches the recommended cars based on views, favorite counts, and model name.
     */
    fun fetchRecommendedCars() {
        viewModelScope.launch {
            // Order by views, reactions, and alphabetical order by model name
            val sortedCars = allCars.sortedWith(
                compareByDescending<CarEntity> { it.views }
                    .thenByDescending { favoriteCounts[it.id] ?: 0 }
                    .thenBy { it.modelo }
            )

            // Take the top 10 cars
            _recommendedCarList.value = sortedCars.take(5)
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

    var selectedCondition: String? = null
    /**
     * Applies filters to the list of cars based on selected criteria, including location.
     */
    fun applyFilters() {
        val filteredCars = allCars.filter { car ->
            val matchesBrand = selectedBrands.isEmpty() || selectedBrands.contains(car.brand)
            val matchesModel = selectedModel == null || car.modelo == selectedModel
            val matchesYear = yearRange?.let { (min, max) -> car.year.toIntOrNull()?.let { it in min..max } } ?: true
            val matchesColor = selectedColors.isEmpty() || selectedColors.contains(car.color.capitalize())
            val matchesLocation = selectedLocation == null || car.location == selectedLocation
            val matchesCondition = selectedCondition == null || car.condition == selectedCondition

            val carPrice = car.price.toIntOrNull() ?: 0
            val matchesPrice = carPrice in (priceRange.first)..(priceRange.second ?: Int.MAX_VALUE)

            val carMileage = car.mileage.toIntOrNull() ?: 0
            val matchesMileage = carMileage in (mileageRange.first)..(mileageRange.second ?: Int.MAX_VALUE)

            matchesBrand && matchesModel && matchesYear && matchesColor && matchesLocation && matchesPrice && matchesMileage && matchesCondition
        }
        _carList.value = filteredCars
    }

    fun filterCarsBySelectedBrands(selectedBrands: Set<String>, onFilterApplied: (List<CarEntity>) -> Unit) {
        val filteredCars = if (selectedBrands.isEmpty()) {
            allCars
        } else {
            allCars.filter { selectedBrands.contains(it.brand) }
        }
        onFilterApplied(filteredCars)
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
        selectedCondition = null // Reinicia la condición seleccionada
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
                    if (result.isSuccess) {
                        showToast(R.string.car_added_to_favorites)
                        listenForNewFavorites(car.id, car.ownerId, car.modelo) // Inicia la lógica de notificaciones
                        addHistoryEvent(
                            userId,
                            "Add to Favorite",
                            "Car ${car.modelo} (${car.id}) added to favorites by $fullName."
                        )
                    }
                } else {
                    val result = removeCarFromFavoritesUseCase(userId, car.id)
                    if (result.isSuccess) {
                        showToast(R.string.car_removed_from_favorites)
                        addHistoryEvent(
                            userId,
                            "Remove to Favorite",
                            "Car ${car.modelo} (${car.id}) removed from favorites by $fullName."
                        )
                    }
                }
            } else {
                showToast(R.string.error_fetching_user_data)
            }
        }
    }

    /**
     * Adds an event to the user's history in the database.
     */
    private fun addHistoryEvent(userId: String, eventType: String, message: String) {
        val timestamp = System.currentTimeMillis()
        val historyEntity = HistoryEntity(userId, timestamp, eventType, message)

        // Push the history event directly under the 'History/userHistory' node
        firebaseDatabase.getReference("History/userHistory")
            .push()
            .setValue(historyEntity)
            .addOnSuccessListener {
                // Optional: Handle successful addition if needed
            }
            .addOnFailureListener { exception ->
                // Log or handle failure here
                exception.printStackTrace()
            }
    }

    fun listenForNewFavorites(carId: String, sellerId: String, name: String) {
        viewModelScope.launch {
            listenForNewFavoritesUseCase(carId) { buyerId, buyerName, carId ->
                createBuyerNotification(buyerId, name)
                createSellerNotification(sellerId, buyerName, carId, name)
            }
        }
    }

    private fun createBuyerNotification(buyerId: String, name: String) {
        viewModelScope.launch {
            addNotificationUseCase(
                userId = buyerId,
                title = "Car added to favorites",
                message = "You have added the car $name to your favorites."
            )
        }
    }

    private fun createSellerNotification(sellerId: String, buyerName: String, carId: String, name: String) {
        viewModelScope.launch {
            addNotificationUseCase(
                userId = sellerId,
                title = "New favorite for your car",
                message = "Your car $name has been added to favorites by $buyerName."
            )
        }
    }

    // Show toast message with the specified string resource ID
    private fun showToast(messageResId: Int) {
        Toast.makeText(getApplication(), getApplication<Application>().getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    private fun mapCarEntityToCar(carEntity: CarEntity): Car {
        return Car(
            id = carEntity.id,
            modelo = carEntity.modelo,
            color = carEntity.color,
            mileage = carEntity.mileage,
            brand = carEntity.brand,
            description = carEntity.description,
            price = carEntity.price,
            year = carEntity.year,
            sold = carEntity.sold,
            imageUrls = carEntity.imageUrls,
            ownerId = carEntity.ownerId,
            transmission = carEntity.transmission,
            fuelType = carEntity.fuelType,
            doors = carEntity.doors,
            engineCapacity = carEntity.engineCapacity,
            location = carEntity.location,
            condition = carEntity.condition,
            features = carEntity.features,
            vin = carEntity.vin,
            previousOwners = carEntity.previousOwners,
            views = carEntity.views,
            listingDate = carEntity.listingDate,
            lastUpdated = carEntity.lastUpdated
        )
    }

    /**
     * Handles unique views count for a car by checking if the current user has viewed it.
     */
    fun handleCarView(car: CarEntity) {
        viewModelScope.launch {
            val currentUser = getCurrentUserIdUseCase()
            val userId = currentUser.getOrNull() ?: return@launch

            // Check if the user has already viewed the car in Firebase
            val viewsRef = firebaseDatabase.getReference("views/${car.id}/$userId")

            viewsRef.get().addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    // Increment the view count only if it is a unique view
                    car.views += 1

                    // Add the unique view to Firebase and update the view count in the car's database entry
                    viewsRef.setValue(true).addOnSuccessListener {
                        updateCarViewCountInDatabase(car)
                    }.addOnFailureListener {
                        showToast(R.string.error_updating_view_count)
                    }
                }
            }.addOnFailureListener {
                showToast(R.string.error_fetching_view_status)
            }
        }
    }

    private fun updateCarViewCountInDatabase(car: CarEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val ownerId = car.ownerId

            // Create an updated Car object with the view count only
            val updatedCar = mapCarEntityToCar(car).copy(views = car.views)

            // Update the view count in the car's database entry
            val result = updateCarToDatabaseUseCase(ownerId, car.id, updatedCar)
            result.fold(
                onSuccess = { fetchCars() },
                onFailure = {
                    withContext(Dispatchers.Main) {
                        showToast(R.string.error_updating_car)
                    }
                }
            )
        }
    }
}
