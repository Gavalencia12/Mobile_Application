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

    // Function to fetch cars for the user from the database
    fun fetchCarsForUser() {
        viewModelScope.launch {
            val currentUserResult = getCurrentUserIdUseCase()
            val userId = currentUserResult.getOrNull()

            userId?.let {
                val result = getCarUserInDatabaseUseCase(it)
                result.onSuccess { cars ->
                    _carList.value = cars
                }.onFailure { exception ->
                    _error.value = "Error fetching cars: ${exception.message}"
                }
            } ?: run {
                _error.value = "User not authenticated"
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
            val userId = currentUser.getOrNull() ?: return@launch

            val car = Car(
                modelo = modelo,
                color = color,
                speed = speed,
                addOn = addOn,
                description = description,
                price = price
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
                onFailure = { error ->
                    _error.value = "Error adding car: ${error.message}"
                }
            )
        }
    }

    // Function to update a car in the database and refresh the list
    private fun updateCarInDatabase(userId: String, carId: String, updatedCar: Car) {
        viewModelScope.launch {
            val result = updateCarToDatabaseUseCase(userId, carId, updatedCar)
            result.fold(
                onSuccess = {
                    fetchCarsForUser()
                },
                onFailure = { error ->
                    _error.value = "Error updating car: ${error.message}"
                }
            )
        }
    }

    // Method to delete a car
    fun deleteCar(userId: String, carId: String) {
        viewModelScope.launch {
            val result = deleteCarInDatabaseUseCase(userId, carId)
            result.fold(
                onSuccess = {
                    fetchCarsForUser()
                },
                onFailure = { error ->
                    _error.value = "Error deleting car: ${error.message}"
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
        newImages: List<Uri>,
        existingImages: List<Uri>
    ) {
        viewModelScope.launch {
            val car = Car(
                id = carId,
                modelo = modelo,
                color = color,
                speed = speed,
                addOn = addOn,
                description = description,
                price = price
            )

            val imageUploadResult = uploadToCarImageUseCase(userId, carId, newImages)
            val newImageUrls = imageUploadResult.getOrNull() ?: emptyList()

            val combinedImageUrls = existingImages.map { it.toString() } + newImageUrls

            val updatedCar = car.copy(imageUrls = combinedImageUrls)
            updateCarInDatabase(userId, carId, updatedCar)
        }
    }

    fun updateCarSoldStatus(userId: String, carId: String, sold: Boolean) {
        viewModelScope.launch {
            val result = updateCarSoldStatusUseCase(userId, carId, sold)
            result.onFailure { exception ->
                _error.value = "Error updating car sold status: ${exception.message}"
            }
        }
    }
    suspend fun getCurrentUserId(): String {
        // Get the current user's ID
        val currentUser = getCurrentUserIdUseCase()
        return currentUser.getOrNull()
            ?: throw IllegalArgumentException("User not authenticated") // Handle not authenticated case
    }

}
