package com.example.carhive.Presentation.seller.viewModel

import android.content.Context
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.lifecycle.*
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Domain.model.Car
import com.example.carhive.Domain.usecase.auth.GetCurrentUserIdUseCase
import com.example.carhive.Domain.usecase.database.*
import com.example.carhive.Domain.usecase.favorites.GetCarFavoriteCountAndUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.example.carhive.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@HiltViewModel
class CrudViewModel @Inject constructor(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val saveCarToDatabaseUseCase: SaveCarToDatabaseUseCase,
    private val updateCarToDatabaseUseCase: UpdateCarToDatabaseUseCase,
    private val deleteCarInDatabaseUseCase: DeleteCarInDatabaseUseCase,
    private val getCarUserInDatabaseUseCase: GetCarUserInDatabaseUseCase,
    private val uploadToCarImageUseCase: UploadToCarImageUseCase,
    private val updateCarSoldStatusUseCase: UpdateCarSoldStatusUseCase,
    private val getCarFavoriteCountAndUsersUseCase: GetCarFavoriteCountAndUsersUseCase,
    private val context: Context
) : ViewModel() {

    private val _carList = MutableLiveData<List<CarEntity>>() // LiveData for car list
    val carList: LiveData<List<CarEntity>> get() = _carList

    private val _favoriteCounts = MutableLiveData<Map<String, Int>>() // LiveData for favorite counts per car
    val favoriteCounts: LiveData<Map<String, Int>> get() = _favoriteCounts

    private val _error = MutableLiveData<String>() // LiveData for error messages
    val error: LiveData<String> get() = _error

    // Helper function to show a toast message
    private fun showToast(messageResId: Int) {
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_LONG).show()
    }

    // Function to fetch favorite counts for each car
    suspend fun fetchFavoriteCountsForCars(carIds: List<String>) {
        withContext(Dispatchers.IO) {
            val favoriteCountMap = mutableMapOf<String, Int>()
            for (carId in carIds) {
                val result = getCarFavoriteCountAndUsersUseCase(carId)
                result.onSuccess { (count, _) ->
                    favoriteCountMap[carId] = count
                }.onFailure {
                    withContext(Dispatchers.Main) {
                        showToast(R.string.error_fetching_favorite_count)
                    }
                }
            }
            _favoriteCounts.postValue(favoriteCountMap) // Update LiveData with favorite counts
        }
    }

    // Function to fetch cars for the current user
    fun fetchCarsForUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserResult = getCurrentUserIdUseCase()
            val userId = currentUserResult.getOrNull()

            userId?.let {
                val result = getCarUserInDatabaseUseCase(it)
                result.onSuccess { cars ->
                    _carList.postValue(cars)
                    fetchFavoriteCountsForCars(cars.map { car -> car.id }) // Fetch favorite counts for each car
                }.onFailure {
                    withContext(Dispatchers.Main) {
                        showToast(R.string.error_fetching_cars)
                    }
                }
            }
        }
    }

    // Function to set up real-time search for car models
    fun setupModelSearch(autoCompleteTextView: AutoCompleteTextView) {
        // Fetch car models for suggestions
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserResult = getCurrentUserIdUseCase()
            val userId = currentUserResult.getOrNull()

            userId?.let {
                val result = getCarUserInDatabaseUseCase(it)
                result.onSuccess { cars ->
                    val models = cars.map { car -> car.modelo }.distinct()
                    withContext(Dispatchers.Main) {
                        val adapter = ArrayAdapter(
                            autoCompleteTextView.context,
                            android.R.layout.simple_dropdown_item_1line,
                            models
                        )
                        autoCompleteTextView.setAdapter(adapter) // Set adapter with model suggestions
                    }
                }
            }
        }

        var searchJob: Job? = null // Variable to manage search delay

        // TextWatcher for real-time asynchronous search
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel() // Cancel any ongoing search

                if (!s.isNullOrEmpty()) {
                    searchJob = viewModelScope.launch {
                        delay(300) // Delay of 300 ms before executing search
                        searchCarsByModel(s.toString())
                    }
                } else {
                    fetchCarsForUser() // Load all cars if the search field is empty
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Function to search cars by model
    private fun searchCarsByModel(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = getCarUserInDatabaseUseCase(getCurrentUserIdUseCase().getOrNull().orEmpty())
            result.onSuccess { cars ->
                val filteredCars = cars.filter { it.modelo.contains(query, ignoreCase = true) }
                withContext(Dispatchers.Main) {
                    if (filteredCars.isNotEmpty()) {
                        _carList.value = filteredCars
                    } else {
                        showToast(R.string.no_results_found) // Show message if no results are found
                    }
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    showToast(R.string.error_fetching_cars)
                }
            }
        }
    }

    // Function to add a car to the database
    fun addCarToDatabase(
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
        features: List<String>?,
        vin: String,
        previousOwners: Int,
        listingDate: String,
        lastUpdated: String,
        images: List<Uri>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = getCurrentUserIdUseCase()
            val userId = currentUser.getOrNull() ?: return@launch

            val formattedColor = color.lowercase().replaceFirstChar { it.uppercase() }

            val car = Car(
                modelo = modelo,
                color = formattedColor,
                mileage = mileage,
                brand = brand,
                description = description,
                price = price,
                year = year,
                sold = false,
                imageUrls = null,
                ownerId = userId,
                transmission = transmission,
                fuelType = fuelType,
                doors = doors,
                engineCapacity = engineCapacity,
                location = location,
                condition = condition,
                features = features,
                vin = vin,
                previousOwners = previousOwners,
                views = 0,
                listingDate = listingDate,
                lastUpdated = lastUpdated
            )

            val result = saveCarToDatabaseUseCase(userId, car)
            result.fold(
                onSuccess = { carId ->
                    val imageUploadResult = uploadToCarImageUseCase(userId, carId, images)
                    val imageUrls = imageUploadResult.getOrNull()

                    if (imageUrls != null) {
                        val updatedCar = car.copy(imageUrls = imageUrls, id = carId)
                        updateCarInDatabase(userId, carId, updatedCar)
                    }
                },
                onFailure = {
                    withContext(Dispatchers.Main) {
                        showToast(R.string.error_adding_car)
                    }
                }
            )
        }
    }

    // Helper function to update car in the database
    private fun updateCarInDatabase(userId: String, carId: String, updatedCar: Car) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = updateCarToDatabaseUseCase(userId, carId, updatedCar)
            result.fold(
                onSuccess = {
                    fetchCarsForUser()
                },
                onFailure = {
                    withContext(Dispatchers.Main) {
                        showToast(R.string.error_updating_car)
                    }
                }
            )
        }
    }

    // Function to delete a car from the database
    fun deleteCar(userId: String, carId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = deleteCarInDatabaseUseCase(userId, carId)
            result.fold(
                onSuccess = {
                    fetchCarsForUser()
                },
                onFailure = {
                    withContext(Dispatchers.Main) {
                        showToast(R.string.error_deleting_car)
                    }
                }
            )
        }
    }

    // Function to update car details
    fun updateCar(
        userId: String,
        carId: String,
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
        previousOwners: Int,
        views: Int,
        listingDate: String,
        lastUpdated: String,
        existingImages: List<String>,
        newImages: List<String>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val formattedColor = color.lowercase().replaceFirstChar { it.uppercase() }

            val car = Car(
                id = carId,
                modelo = modelo,
                color = formattedColor,
                mileage = mileage,
                brand = brand,
                description = description,
                price = price,
                year = year,
                sold = false,
                ownerId = userId,
                transmission = transmission,
                fuelType = fuelType,
                doors = doors,
                engineCapacity = engineCapacity,
                location = location,
                condition = condition,
                features = features,
                vin = vin,
                previousOwners = previousOwners,
                views = views,
                listingDate = listingDate,
                lastUpdated = lastUpdated
            )

            val combinedImageUrls = existingImages + newImages
            val updatedCar = car.copy(imageUrls = combinedImageUrls)
            updateCarInDatabase(userId, carId, updatedCar)
        }
    }

    // Function to update the sold status of a car
    fun updateCarSoldStatus(userId: String, carId: String, sold: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = updateCarSoldStatusUseCase(userId, carId, sold)
            result.onFailure {
                withContext(Dispatchers.Main) {
                    showToast(R.string.error_updating_sold_status)
                }
            }
        }
    }

    // Function to retrieve the current user ID
    suspend fun getCurrentUserId(): String {
        return withContext(Dispatchers.IO) {
            getCurrentUserIdUseCase().getOrNull()
                ?: throw IllegalArgumentException(context.getString(R.string.error_user_not_authenticated))
        }
    }
}
