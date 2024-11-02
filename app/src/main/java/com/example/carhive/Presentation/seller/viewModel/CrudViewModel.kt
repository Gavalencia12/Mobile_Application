package com.example.carhive.Presentation.seller.viewModel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Domain.model.Car
import com.example.carhive.Domain.usecase.auth.GetCurrentUserIdUseCase
import com.example.carhive.Domain.usecase.database.*
import com.example.carhive.Domain.usecase.favorites.GetCarFavoriteCountAndUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.carhive.R

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

    // LiveData to hold the list of cars for the user
    private val _carList = MutableLiveData<List<CarEntity>>()
    val carList: LiveData<List<CarEntity>> get() = _carList

    // LiveData to hold error messages
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // LiveData to hold the map of car favorite counts
    private val _favoriteCounts = MutableLiveData<Map<String, Int>>()
    val favoriteCounts: LiveData<Map<String, Int>> get() = _favoriteCounts

    // Function to display a Toast message using a string resource ID
    private fun showToast(messageResId: Int) {
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_LONG).show()
    }

    // Fetches the favorite count for each car using a list of car IDs
    fun fetchFavoriteCountsForCars(carIds: List<String>) {
        viewModelScope.launch {
            val favoriteCountMap = mutableMapOf<String, Int>()
            for (carId in carIds) {
                val result = getCarFavoriteCountAndUsersUseCase(carId)
                result.onSuccess { (count, _) ->
                    favoriteCountMap[carId] = count
                }.onFailure {
                    showToast(R.string.error_fetching_favorite_count)
                }
            }
            _favoriteCounts.value = favoriteCountMap
        }
    }

    // Fetches the list of cars owned by the current user
    fun fetchCarsForUser() {
        viewModelScope.launch {
            val currentUserResult = getCurrentUserIdUseCase()
            val userId = currentUserResult.getOrNull()

            userId?.let {
                val result = getCarUserInDatabaseUseCase(it)
                result.onSuccess { cars ->
                    _carList.value = cars
                    fetchFavoriteCountsForCars(cars.map { car -> car.id })
                }.onFailure {
                    showToast(R.string.error_fetching_cars)
                }
            }
        }
    }

    // Adds a new car to the database
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
        viewModelScope.launch {
            // Gets the current user's ID
            val currentUser = getCurrentUserIdUseCase()
            val userId = currentUser.getOrNull() ?: return@launch

            // Formats the color input (capitalize the first letter, lowercase the rest)
            val formattedColor = color.lowercase().replaceFirstChar { it.uppercase() }

            // Creates a Car object with the provided details
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

            // Attempts to save the car to the database
            val result = saveCarToDatabaseUseCase(userId, car)
            result.fold(
                onSuccess = { carId ->
                    // Uploads car images if saving was successful
                    val imageUploadResult = uploadToCarImageUseCase(userId, carId, images)
                    val imageUrls = imageUploadResult.getOrNull()

                    if (imageUrls != null) {
                        val updatedCar = car.copy(imageUrls = imageUrls, id = carId)
                        updateCarInDatabase(userId, carId, updatedCar)
                    }
                },
                onFailure = {
                    showToast(R.string.error_adding_car)
                }
            )
        }
    }

    // Updates the car information in the database
    private fun updateCarInDatabase(userId: String, carId: String, updatedCar: Car) {
        viewModelScope.launch {
            val result = updateCarToDatabaseUseCase(userId, carId, updatedCar)
            result.fold(
                onSuccess = {
                    fetchCarsForUser()
                },
                onFailure = {
                    showToast(R.string.error_updating_car)
                }
            )
        }
    }

    // Deletes a car from the database
    fun deleteCar(userId: String, carId: String) {
        viewModelScope.launch {
            val result = deleteCarInDatabaseUseCase(userId, carId)
            result.fold(
                onSuccess = {
                    fetchCarsForUser()
                },
                onFailure = {
                    showToast(R.string.error_deleting_car)
                }
            )
        }
    }

    // Updates a car with new details
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
        viewModelScope.launch {
            // Formats the color input
            val formattedColor = color.lowercase().replaceFirstChar { it.uppercase() }

            // Creates a Car object with updated details
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

            // Combines existing and new image URLs
            val combinedImageUrls = existingImages + newImages
            val updatedCar = car.copy(imageUrls = combinedImageUrls)
            updateCarInDatabase(userId, carId, updatedCar)
        }
    }

    // Updates the sold status of a car
    fun updateCarSoldStatus(userId: String, carId: String, sold: Boolean) {
        viewModelScope.launch {
            val result = updateCarSoldStatusUseCase(userId, carId, sold)
            result.onFailure {
                showToast(R.string.error_updating_sold_status)
            }
        }
    }

    // Gets the current user ID, throwing an error if the user is not authenticated
    suspend fun getCurrentUserId(): String {
        val currentUser = getCurrentUserIdUseCase()
        return currentUser.getOrNull()
            ?: throw IllegalArgumentException(context.getString(R.string.error_user_not_authenticated))
    }
}
