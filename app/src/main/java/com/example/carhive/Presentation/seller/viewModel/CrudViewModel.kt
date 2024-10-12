package com.example.carhive.Presentation.seller.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.example.carhive.Data.mapper.CarMapper
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
    private val carMapper: CarMapper
) : ViewModel() {

    private val _cars = MutableLiveData<List<Car>>()
    val cars: LiveData<List<Car>> get() = _cars

//    fun loadCarsForUser() {
//        viewModelScope.launch {
//            val currentUser = getCurrentUserIdUseCase()
//            val userId = currentUser.getOrNull() ?: return@launch
//
//            // Cargar coches del usuario
//            val result = getCarUserInDatabaseUseCase(userId) // Asegúrate de que esta función ahora devuelva CarEntity con ID
//            result.fold(
//                onSuccess = { carEntities ->
//                    // Mapea CarEntity a Car y agrega los IDs
//                    val cars = carEntities.map { carEntity ->
//                        carMapper.mapToDomain(carEntity) // Asegúrate de que el ID esté en Car
//                    }
//                    _cars.value = cars // Suponiendo que _cars es un MutableLiveData<List<Car>>
//                },
//                onFailure = { error ->
//                    // Manejar error
//                }
//            )
//        }
//    }
// NO JALA PIPI

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