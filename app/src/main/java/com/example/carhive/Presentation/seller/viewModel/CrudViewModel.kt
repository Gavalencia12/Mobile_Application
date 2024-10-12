package com.example.carhive.Presentation.seller.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.example.carhive.Data.mapper.CarMapper
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Domain.model.Car
import com.example.carhive.Domain.usecase.auth.GetCurrentUserIdUseCase
import com.example.carhive.Domain.usecase.database.DeleteCarInDatabaseUseCase
import com.example.carhive.Domain.usecase.database.GetCarUserInDatabaseUseCase
import com.example.carhive.Domain.usecase.database.SaveCarToDatabaseUseCase
import com.example.carhive.Domain.usecase.database.UpdateCarToDatabaseUseCase
import com.example.carhive.Domain.usecase.database.UploadToCarImageUseCase
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

//    // Método para actualizar un coche
//    fun updateCar(userId: String, carId: String, car: Car) {
//        viewModelScope.launch {
//            val result = updateCarToDatabaseUseCase(userId, carId, car)
//            result.onSuccess {
//                // Manejar éxito
//            }.onFailure {
//                // Manejar error
//            }
//        }
//    }
//
//    // Método para eliminar un coche
//    fun deleteCar(userId: String, carId: String) {
//        viewModelScope.launch {
//            val result = deleteCarInDatabaseUseCase(userId, carId)
//            result.onSuccess {
//                // Manejar éxito, por ejemplo, recargar la lista
//            }.onFailure {
//                // Manejar error
//            }
//        }
//    }

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

            // Guardar el coche en la base de datos
            val result = saveCarToDatabaseUseCase(userId, car)
            result.fold(
                onSuccess = { carId ->
                    // Subir imágenes y obtener sus URLs
                    val imageUploadResult = uploadToCarImageUseCase(userId, carId, images)
                    val imageUrls = imageUploadResult.getOrNull()

                    // Actualizar el coche en la base de datos con las URLs de las imágenes
                    if (imageUrls != null) {
                        // Actualizar el objeto Car con las nuevas URLs de las imágenes
                        val updatedCar = car.copy(imageUrls = imageUrls, id = carId)

                        // Usar el ID del coche guardado para actualizar
                        updateCarToDatabaseUseCase(userId, carId, updatedCar)
                    }
                },
                onFailure = { error ->
                    // Manejar errores en la creación del coche
                }
            )
        }
    }



}