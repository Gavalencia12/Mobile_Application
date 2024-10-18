package com.example.carhive.Presentation.seller.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Domain.model.Car
import com.example.carhive.Domain.usecase.auth.GetCurrentUserIdUseCase
import com.example.carhive.Domain.usecase.database.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrudViewModel @Inject constructor(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val saveCarToDatabaseUseCase: SaveCarToDatabaseUseCase,
    private val updateCarToDatabaseUseCase: UpdateCarToDatabaseUseCase,
    private val deleteCarInDatabaseUseCase: DeleteCarInDatabaseUseCase,
    private val getCarUserInDatabaseUseCase: GetCarUserInDatabaseUseCase,
    private val uploadToCarImageUseCase: UploadToCarImageUseCase,
) : ViewModel() {

    // LiveData to hold the list of cars for the user
    private val _carList = MutableLiveData<List<CarEntity>>()
    val carList: LiveData<List<CarEntity>> get() = _carList

    // LiveData to manage errors
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Function to fetch cars for the user from the database
    fun fetchCarsForUser() {
        viewModelScope.launch {
            // Get the current user's ID
            val currentUserResult = getCurrentUserIdUseCase()
            val userId = currentUserResult.getOrNull()

            userId?.let {
                // Fetch cars for the current user
                val result = getCarUserInDatabaseUseCase(it)
                result.onSuccess { cars ->
                    _carList.value = cars // Update LiveData with the list of cars
                }.onFailure { exception ->
                    _error.value = "Error fetching cars: ${exception.message}" // Update error LiveData
                }
            } ?: run {
                _error.value = "User not authenticated" // Handle case where user is not authenticated
            }
        }
    }

    // Method to add a car to the database
    fun addCarToDatabase(
        modelo: String,
        color: String,
        speed: String,
        addOn: String,
        description: String,
        price: String,
        images: List<Uri>
    ) {
        viewModelScope.launch {
            // Get the current user's ID
            val currentUser = getCurrentUserIdUseCase()
            val userId = currentUser.getOrNull() ?: return@launch

            // Create a Car object
            val car = Car(
                modelo = modelo,
                color = color,
                speed = speed,
                addOn = addOn,
                description = description,
                price = price
            )

            // Save the car in the database
            val result = saveCarToDatabaseUseCase(userId, car)
            result.fold(
                onSuccess = { carId ->
                    // Upload images to Firebase and get URLs
                    val imageUploadResult = uploadToCarImageUseCase(userId, carId, images)
                    val imageUrls = imageUploadResult.getOrNull()

                    if (imageUrls != null) {
                        // Update the car with the image URLs
                        val updatedCar = car.copy(imageUrls = imageUrls, id = carId)
                        updateCarInDatabase(userId, carId, updatedCar)
                    }
                },
                onFailure = { error ->
                    _error.value = "Error adding car: ${error.message}" // Update error LiveData
                }
            )
        }
    }

    // Function to update a car in the database and refresh the list
    private fun updateCarInDatabase(userId: String, carId: String, updatedCar: Car) {
        viewModelScope.launch {
            // Update the car in the database
            val result = updateCarToDatabaseUseCase(userId, carId, updatedCar)
            result.fold(
                onSuccess = {
                    fetchCarsForUser() // Refresh the list of cars
                },
                onFailure = { error ->
                    _error.value = "Error updating car: ${error.message}" // Update error LiveData
                }
            )
        }
    }

    // Method to delete a car
    fun deleteCar(userId: String, carId: String) {
        viewModelScope.launch {
            // Delete the car from the database
            val result = deleteCarInDatabaseUseCase(userId, carId)
            result.fold(
                onSuccess = {
                    fetchCarsForUser() // Refresh the list after deletion
                },
                onFailure = { error ->
                    _error.value = "Error deleting car: ${error.message}" // Update error LiveData
                }
            )
        }
    }

    // Method to update a car
    fun updateCar(
        userId: String,
        carId: String,
        modelo: String,
        color: String,
        speed: String,
        addOn: String,
        description: String,
        price: String,
        newImages: List<Uri>, // New images to upload
        existingImages: List<Uri> // Existing images that do not need to be uploaded
    ) {
        viewModelScope.launch {
            // Create a Car object with updated details
            val car = Car(
                id = carId,
                modelo = modelo,
                color = color,
                speed = speed,
                addOn = addOn,
                description = description,
                price = price
            )

            // Upload only new images
            val imageUploadResult = uploadToCarImageUseCase(userId, carId, newImages)
            val newImageUrls = imageUploadResult.getOrNull() ?: emptyList()

            // Combine existing URLs with new ones
            val combinedImageUrls = existingImages.map { it.toString() } + newImageUrls

            // Update the car with all image URLs
            val updatedCar = car.copy(imageUrls = combinedImageUrls)
            updateCarInDatabase(userId, carId, updatedCar)
        }
    }

    // Add this function in CrudViewModel
    suspend fun getCurrentUserId(): String {
        // Get the current user's ID
        val currentUser = getCurrentUserIdUseCase()
        return currentUser.getOrNull() ?: throw IllegalArgumentException("User not authenticated") // Handle not authenticated case
    }
}
