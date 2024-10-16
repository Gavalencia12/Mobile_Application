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

    // LiveData para almacenar la lista de coches del usuario
    private val _carList = MutableLiveData<List<CarEntity>>()
    val carList: LiveData<List<CarEntity>> get() = _carList

    // LiveData para manejar errores
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Función para obtener los coches del usuario desde la base de datos
    fun fetchCarsForUser() {
        viewModelScope.launch {
            val currentUserResult = getCurrentUserIdUseCase()
            val userId = currentUserResult.getOrNull()

            userId?.let {
                val result = getCarUserInDatabaseUseCase(it)
                result.onSuccess { cars ->
                    _carList.value = cars // Actualiza el LiveData con la lista de coches
                }.onFailure { exception ->
                    _error.value = "Error fetching cars: ${exception.message}" // Actualiza el LiveData de error
                }
            } ?: run {
                _error.value = "User not authenticated"
            }
        }
    }

    // Método para añadir un coche a la base de datos
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

            // Crear el objeto del coche
            val car = Car(
                modelo = modelo,
                color = color,
                speed = speed,
                addOn = addOn,
                description = description,
                price = price
            )

            // Guardar el coche en la base de datos
            val result = saveCarToDatabaseUseCase(userId, car)
            result.fold(
                onSuccess = { carId ->
                    // Subir imágenes a Firebase y obtener las URLs
                    val imageUploadResult = uploadToCarImageUseCase(userId, carId, images)
                    val imageUrls = imageUploadResult.getOrNull()

                    if (imageUrls != null) {
                        // Actualizar el coche con las URLs de las imágenes
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

    // Función para actualizar el coche en la base de datos y refrescar la lista
    private fun updateCarInDatabase(userId: String, carId: String, updatedCar: Car) {
        viewModelScope.launch {
            val result = updateCarToDatabaseUseCase(userId, carId, updatedCar)
            result.fold(
                onSuccess = {
                    fetchCarsForUser() // Refrescar la lista de coches
                },
                onFailure = { error ->
                    _error.value = "Error updating car: ${error.message}"
                }
            )
        }
    }

    // Método para eliminar un coche
    fun deleteCar(userId: String, carId: String) {
        viewModelScope.launch {
            val result = deleteCarInDatabaseUseCase(userId, carId)
            result.fold(
                onSuccess = {
                    fetchCarsForUser() // Refrescar la lista después de eliminar
                },
                onFailure = { error ->
                    _error.value = "Error deleting car: ${error.message}"
                }
            )
        }
    }

    // Método para actualizar un coche
    fun updateCar(
        userId: String,
        carId: String,
        modelo: String,
        color: String,
        speed: String,
        addOn: String,
        description: String,
        price: String,
        images: List<Uri>
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

            // Subir imágenes nuevas y actualizar en la base de datos
            val imageUploadResult = uploadToCarImageUseCase(userId, carId, images)
            val imageUrls = imageUploadResult.getOrNull()

            if (imageUrls != null) {
                val updatedCar = car.copy(imageUrls = imageUrls)
                updateCarInDatabase(userId, carId, updatedCar)
            } else {
                _error.value = "Error uploading images"
            }
        }
    }
    // Agrega esta función en CrudViewModel
    suspend fun getCurrentUserId(): String {
        val currentUser = getCurrentUserIdUseCase()
        return currentUser.getOrNull() ?: throw IllegalArgumentException("User not authenticated")
    }

}
