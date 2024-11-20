package com.example.carhive.presentation.seller.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.data.model.CarEntity
import com.example.carhive.data.model.UserEntity
import com.example.carhive.data.repository.AuthRepository
import com.example.carhive.Domain.usecase.auth.GetCurrentUserIdUseCase
import com.example.carhive.Domain.usecase.database.GetCarUserInDatabaseUseCase
import com.example.carhive.Domain.usecase.database.GetUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerHomeViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase, // Asegúrate de que esta use case esté implementada
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase, // Asegúrate de que esta use case esté implementada
    private val getCarUserInDatabaseUseCase: GetCarUserInDatabaseUseCase, // Asegúrate de que esta use case esté implementada
    private val authRepository: AuthRepository // Inyección del repositorio
) : ViewModel() {

    private val _favoriteReactionsCount = MutableLiveData<Int>()
    val favoriteReactionsCount: LiveData<Int> get() = _favoriteReactionsCount

    // LiveData para mantener los datos del usuario actual
    private val _userData = MutableLiveData<Result<UserEntity>>()
    val userData: LiveData<Result<UserEntity>> get() = _userData

    // LiveData para mantener la lista de carros del usuario
    private val _carList = MutableLiveData<List<CarEntity>>()
    val carList: LiveData<List<CarEntity>> get() = _carList

    // LiveData para mantener el total de carros
    private val _totalCarsCount = MutableLiveData<Int>()
    val totalCarsCount: LiveData<Int> get() = _totalCarsCount

    // LiveData para mantener el número de carros vendidos
    private val _soldCarsCount = MutableLiveData<Int>()
    val soldCarsCount: LiveData<Int> get() = _soldCarsCount

    // LiveData para mantener el número de carros no vendidos
    private val _unsoldCarsCount = MutableLiveData<Int>()
    val unsoldCarsCount: LiveData<Int> get() = _unsoldCarsCount

    private val _unapprovedCarsCount = MutableLiveData<Int>()
    val unapprovedCarsCount: LiveData<Int> get() = _unapprovedCarsCount

    // Obtener los datos del usuario usando el ID recuperado desde el caso de uso
    fun fetchUserData() {
        viewModelScope.launch {
            val userIdResult = getCurrentUserIdUseCase() // Obtener el ID del usuario actual
            val userId = userIdResult.getOrNull() // Obtener el ID del resultado

            userIdResult.onSuccess {
                if (userId != null) {
                    // Obtener los datos del usuario desde la base de datos si el ID es válido
                    val result = getUserDataUseCase(userId)
                    _userData.value = result.map { userList -> userList.firstOrNull() ?: throw Exception("User not found") }
                } else {
                    // Manejar el error al obtener el ID del usuario
                    _userData.value = Result.failure(Exception("Failed to get user ID"))
                }
            }.onFailure {
                // Manejar el error en la obtención del ID del usuario
                _userData.value = Result.failure(it)
            }
        }
    }

    // Obtener la lista de carros para el usuario actual
    fun fetchCarsForUser() {
        viewModelScope.launch {
            val currentUserResult = getCurrentUserIdUseCase() // Obtener el ID del usuario actual
            val userId = currentUserResult.getOrNull() // Obtener el ID del resultado

            userId?.let {
                // Obtener los carros asociados al ID del usuario
                val result = getCarUserInDatabaseUseCase(it)
                result.onSuccess { cars ->
                    _carList.value = cars // Actualizar la lista de carros
                    updateCarCounts(cars) // Actualizar los conteos de carros
                }.onFailure { exception ->
                    // Manejar errores al obtener los carros
                    Log.e("SellerHomeViewModel", "Failed to fetch cars: ${exception.message}")
                }
            }
        }
    }

    // Actualizar los conteos de total, vendidos y no vendidos, solo para los carros aprobados
    private fun updateCarCounts(cars: List<CarEntity>) {
        val totalCars = cars.size // Total de carros en la base de datos
        val unapprovedCars = cars.filter { !it.approved } // Carros no aprobados
        val approvedCars = cars.filter { it.approved } // Carros aprobados

        val soldCount = approvedCars.count { it.sold } // Carros aprobados y vendidos
        val unsoldCount = approvedCars.count { !it.sold } // Carros aprobados y no vendidos

        // Actualizar los LiveData con los conteos
        _totalCarsCount.value = totalCars // Todos los carros, independientemente del estado
        _soldCarsCount.value = soldCount // Carros vendidos entre los aprobados
        _unsoldCarsCount.value = unsoldCount // Carros no vendidos entre los aprobados
        _unapprovedCarsCount.value = unapprovedCars.size // Carros no aprobados
    }


    // Obtener el número de carros con al menos un favorito
    // Función para obtener el número de carros con al menos una reacción (favorito) de otros usuarios
    fun fetchFavoriteReactionsForUserCars() {
        viewModelScope.launch {
            // Obtener el ID del usuario actual
            val currentUserIdResult = getCurrentUserIdUseCase()
            val userId = currentUserIdResult.getOrNull()

            // Solo proceder si el ID de usuario es válido
            userId?.let {
                // Llamar al repositorio para obtener el conteo de reacciones en los carros del usuario
                val result = authRepository.getFavoriteReactionsForUserCars(it)
                result.onSuccess { reactionsCount ->
                    _favoriteReactionsCount.value = reactionsCount // Actualizar el LiveData con el contador
                }.onFailure { exception ->
                    Log.e("SellerHomeViewModel", "Error fetching favorite reactions count: ${exception.message}")
                }
            }
        }
    }
}
