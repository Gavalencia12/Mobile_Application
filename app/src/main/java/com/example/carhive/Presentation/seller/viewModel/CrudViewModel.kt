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
    private val updateCarSoldStatusUseCase: UpdateCarSoldStatusUseCase
) : ViewModel() {

    // LiveData to hold the list of cars for the user
    private val _carList = MutableLiveData<List<CarEntity>>()
    val carList: LiveData<List<CarEntity>> get() = _carList

    // LiveData to manage errors
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Function to fetch cars for the current user from the database
    fun fetchCarsForUser() {
        viewModelScope.launch {
            val currentUserResult = getCurrentUserIdUseCase()
            val userId = currentUserResult.getOrNull()

            userId?.let {
                // Fetch cars associated with the current user from the database
                val result = getCarUserInDatabaseUseCase(it)
                result.onSuccess { cars ->
                    _carList.value = cars  // Update the LiveData with the list of cars
                }.onFailure { exception ->
                    _error.value = "Error fetching cars: ${exception.message}"  // Handle error
                }
            } ?: run {
                _error.value = "User not authenticated"  // Handle case where user is not logged in
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
            val currentUser = getCurrentUserIdUseCase()
            val userId = currentUser.getOrNull() ?: return@launch  // Abort if no user ID is available

            // Create a new Car object
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
                    // If car is successfully added, upload images associated with the car
                    val imageUploadResult = uploadToCarImageUseCase(userId, carId, images)
                    val imageUrls = imageUploadResult.getOrNull()

                    // If image upload is successful, update the car record with image URLs
                    if (imageUrls != null) {
                        val updatedCar = car.copy(imageUrls = imageUrls, id = carId)
                        updateCarInDatabase(userId, carId, updatedCar)
                    }
                },
                onFailure = { error ->
                    _error.value = "Error adding car: ${error.message}"  // Handle error when adding car
                }
            )
        }
    }

    // Function to update a car in the database and refresh the list of cars
    private fun updateCarInDatabase(userId: String, carId: String, updatedCar: Car) {
        viewModelScope.launch {
            val result = updateCarToDatabaseUseCase(userId, carId, updatedCar)
            result.fold(
                onSuccess = {
                    fetchCarsForUser()  // Refresh the list of cars after a successful update
                },
                onFailure = { error ->
                    _error.value = "Error updating car: ${error.message}"  // Handle error when updating car
                }
            )
        }
    }

    // Method to delete a car from the database
    fun deleteCar(userId: String, carId: String) {
        viewModelScope.launch {
            val result = deleteCarInDatabaseUseCase(userId, carId)
            result.fold(
                onSuccess = {
                    fetchCarsForUser()  // Refresh the list of cars after a successful deletion
                },
                onFailure = { error ->
                    _error.value = "Error deleting car: ${error.message}"  // Handle error when deleting car
                }
            )
        }
    }

    // Method to update car details and images in the database
    fun updateCar(
        userId: String,
        carId: String,
        modelo: String,
        color: String,
        speed: String,
        addOn: String,
        description: String,
        price: String,
        newImages: List<Uri>,
        existingImages: List<Uri>
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

            // Upload any new images for the car
            val imageUploadResult = uploadToCarImageUseCase(userId, carId, newImages)
            val newImageUrls = imageUploadResult.getOrNull() ?: emptyList()

            // Combine existing images with newly uploaded ones
            val combinedImageUrls = existingImages.map { it.toString() } + newImageUrls

            // Update the car record with the combined list of images
            val updatedCar = car.copy(imageUrls = combinedImageUrls)
            updateCarInDatabase(userId, carId, updatedCar)
        }
    }

    // Method to update the sold status of a car
    fun updateCarSoldStatus(userId: String, carId: String, sold: Boolean) {
        viewModelScope.launch {
            val result = updateCarSoldStatusUseCase(userId, carId, sold)
            result.onFailure { exception ->
                _error.value = "Error updating car sold status: ${exception.message}"  // Handle error when updating sold status
            }
        }
    }

    // Suspend function to get the current user ID
    suspend fun getCurrentUserId(): String {
        val currentUser = getCurrentUserIdUseCase()
        return currentUser.getOrNull()
            ?: throw IllegalArgumentException("User not authenticated")  // Throw error if no user is authenticated
    }
}
