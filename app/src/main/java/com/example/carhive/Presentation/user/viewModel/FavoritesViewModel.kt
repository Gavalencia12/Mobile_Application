package com.example.carhive.Presentation.user.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Domain.usecase.auth.GetCurrentUserIdUseCase
import com.example.carhive.Domain.usecase.database.GetAllCarsFromDatabaseUseCase
import com.example.carhive.Domain.usecase.database.GetCarUserInDatabaseUseCase
import com.example.carhive.Domain.usecase.database.GetUserDataUseCase
import com.example.carhive.Domain.usecase.favorites.AddCarToFavoritesUseCase
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

    // Función para obtener los coches favoritos del usuario actual
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
                            favoriteCarsList.add(car)
                        }
                    }
                }
                _favoriteCars.value = favoriteCarsList
            } else {
                Log.e("FavoritesViewModel", "Error fetching favorite cars: ${userFavoritesResult.exceptionOrNull()}")
            }
        }
    }

    // Eliminar un coche de favoritos
    fun removeFavoriteCar(car: CarEntity) {
        viewModelScope.launch {
            val currentUser = getCurrentUserIdUseCase()
            val userId = currentUser.getOrNull() ?: return@launch

            val result = removeCarFromFavoritesUseCase(userId, car.id)
            if (result.isSuccess) {
                // Refresca la lista de coches favoritos después de eliminar uno
                fetchFavoriteCars()
                Log.d("FavoritesViewModel", "Car removed from favorites successfully")
            } else {
                Log.e("FavoritesViewModel", "Error removing car from favorites: ${result.exceptionOrNull()}")
            }
        }
    }
}
