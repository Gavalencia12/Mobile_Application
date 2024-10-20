package com.example.carhive.Presentation.user.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Data.model.UserEntity
import com.example.carhive.Domain.usecase.auth.GetCurrentUserIdUseCase
import com.example.carhive.Domain.usecase.database.GetAllCarsFromDatabaseUseCase
import com.example.carhive.Domain.usecase.database.GetUserDataUseCase
import com.example.carhive.Domain.usecase.favorites.AddCarToFavoritesUseCase
import com.example.carhive.Domain.usecase.favorites.RemoveCarFromFavoritesUseCase
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getAllCarsFromDatabaseUseCase: GetAllCarsFromDatabaseUseCase,
    private val addCarToFavoritesUseCase: AddCarToFavoritesUseCase,
    private val removeCarFromFavoritesUseCase: RemoveCarFromFavoritesUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getUserDataUseCase: GetUserDataUseCase,
    private val firebaseDatabase: FirebaseDatabase
) : ViewModel() {

    private val _carList = MutableLiveData<List<CarEntity>>()
    val carList: LiveData<List<CarEntity>> get() = _carList

    private val _userData = MutableLiveData<UserEntity?>()
    val userData: LiveData<UserEntity?> get() = _userData

    fun fetchCars() {
        viewModelScope.launch {
            val result = getAllCarsFromDatabaseUseCase()
            if (result.isSuccess) {
                _carList.value = result.getOrNull() ?: emptyList()
            } else {
                Log.e("UserViewModel", "Error fetching cars: ${result.exceptionOrNull()}")
            }
        }
    }

    // Verificar si un coche está en favoritos
    fun isCarFavorite(carId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val currentUser = getCurrentUserIdUseCase()
            val userId = currentUser.getOrNull() ?: return@launch

            val reference = firebaseDatabase.getReference("Favorites/UserFavorites")
                .child(userId)
                .child(carId)

            reference.get().addOnSuccessListener { snapshot ->
                callback(snapshot.exists()) // Retorna true si existe el coche en favoritos
            }.addOnFailureListener {
                Log.e("UserViewModel", "Error fetching favorite status: ${it.message}")
                callback(false) // En caso de error, asume que no está en favoritos
            }
        }
    }

    fun toggleFavorite(car: CarEntity, isFavorite: Boolean) {
        viewModelScope.launch {
            val currentUser = getCurrentUserIdUseCase()
            val userId = currentUser.getOrNull() ?: return@launch

            // Obtener los datos del usuario actual
            val resultUser = getUserDataUseCase(userId)
            if (resultUser.isSuccess) {
                val userList = resultUser.getOrNull() ?: emptyList()
                if (userList.isNotEmpty()) {
                    val user = userList.first()
                    val fullName = "${user.firstName} ${user.lastName}" // Concatenar firstName y lastName

                    if (isFavorite) {
                        // Si está seleccionado, agregar a favoritos
                        val result = addCarToFavoritesUseCase(userId, fullName, car.id, car.id, car.ownerId)
                        if (result.isSuccess) {
                            Log.d("UserViewModel", "Car added to favorites successfully")
                        } else {
                            Log.e("UserViewModel", "Error adding car to favorites: ${result.exceptionOrNull()}")
                        }
                    } else {
                        // Si no está seleccionado, eliminar de favoritos
                        val result = removeCarFromFavoritesUseCase(userId, car.id)
                        if (result.isSuccess) {
                            Log.d("UserViewModel", "Car removed from favorites successfully")
                        } else {
                            Log.e("UserViewModel", "Error removing car from favorites: ${result.exceptionOrNull()}")
                        }
                    }
                } else {
                    Log.e("UserViewModel", "User not found")
                }
            } else {
                Log.e("UserViewModel", "Error fetching user data: ${resultUser.exceptionOrNull()}")
            }
        }
    }
}
