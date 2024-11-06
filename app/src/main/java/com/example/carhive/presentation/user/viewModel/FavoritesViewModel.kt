package com.example.carhive.presentation.user.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.data.model.CarEntity
import com.example.carhive.Domain.usecase.auth.GetCurrentUserIdUseCase
import com.example.carhive.Domain.usecase.database.GetCarUserInDatabaseUseCase
import com.example.carhive.Domain.usecase.favorites.GetUserFavoritesUseCase
import com.example.carhive.Domain.usecase.favorites.RemoveCarFromFavoritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getCarUserInDatabaseUseCase: GetCarUserInDatabaseUseCase,
    private val getUserFavoritesUseCase: GetUserFavoritesUseCase,
    private val removeCarFromFavoritesUseCase: RemoveCarFromFavoritesUseCase
) : ViewModel() {

    private val _favoriteCars = MutableLiveData<List<CarEntity>>()
    val favoriteCars: LiveData<List<CarEntity>> get() = _favoriteCars

    // Function to fetch the current user's favorite cars with full details
    fun fetchFavoriteCars() {
        viewModelScope.launch {
            val currentUser = getCurrentUserIdUseCase()
            val userId = currentUser.getOrNull() ?: return@launch

            val userFavoritesResult = getUserFavoritesUseCase(userId)
            if (userFavoritesResult.isSuccess) {
                val favoriteCarsList = mutableListOf<CarEntity>()
                val favoriteCars = userFavoritesResult.getOrNull() ?: return@launch

                for (favorite in favoriteCars) {
                    val carOwner = favorite.carOwner
                    val carId = favorite.carModel

                    val carResult = getCarUserInDatabaseUseCase(carOwner)
                    if (carResult.isSuccess) {
                        val carList = carResult.getOrNull()
                        val car = carList?.find { it.id == carId }
                        if (car != null) {
                            // Ensure all required fields are populated for each CarEntity
                            val detailedCar = CarEntity(
                                id = car.id,
                                modelo = car.modelo,
                                color = car.color,
                                mileage = car.mileage,
                                brand = car.brand,
                                description = car.description,
                                price = car.price,
                                year = car.year,
                                sold = car.sold,
                                imageUrls = car.imageUrls,
                                ownerId = car.ownerId,
                                transmission = car.transmission,
                                fuelType = car.fuelType,
                                doors = car.doors,
                                engineCapacity = car.engineCapacity,
                                location = car.location,
                                condition = car.condition,
                                features = car.features,
                                vin = car.vin,
                                previousOwners = car.previousOwners,
                            )
                            favoriteCarsList.add(detailedCar)
                        }
                    }
                }
                _favoriteCars.value = favoriteCarsList
            } else {
                Log.e("FavoritesViewModel", "Error fetching favorite cars: ${userFavoritesResult.exceptionOrNull()}")
            }
        }
    }

    // Removes a car from the favorites list and refreshes the list
    fun removeFavoriteCar(car: CarEntity) {
        viewModelScope.launch {
            val currentUser = getCurrentUserIdUseCase()
            val userId = currentUser.getOrNull() ?: return@launch

            val result = removeCarFromFavoritesUseCase(userId, car.id)
            if (result.isSuccess) {
                fetchFavoriteCars() // Refresh the list of favorite cars after removal
                Log.d("FavoritesViewModel", "Car removed from favorites successfully")
            } else {
                Log.e("FavoritesViewModel", "Error removing car from favorites: ${result.exceptionOrNull()}")
            }
        }
    }
}
