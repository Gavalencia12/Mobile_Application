package com.example.carhive.Presentation.seller.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Data.model.UserEntity
import com.example.carhive.Domain.usecase.auth.GetCurrentUserIdUseCase
import com.example.carhive.Domain.usecase.database.GetCarUserInDatabaseUseCase
import com.example.carhive.Domain.usecase.database.GetUserDataUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerHomeViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase, // Ensure that this use case is implemented
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase, // Ensure that this use case is implemented
    private val getCarUserInDatabaseUseCase: GetCarUserInDatabaseUseCase // Ensure that this use case is implemented
) : ViewModel() {

    // LiveData to hold the current user's data
    private val _userData = MutableLiveData<Result<UserEntity>>()
    val userData: LiveData<Result<UserEntity>> get() = _userData

    // LiveData to hold the list of cars for the user
    private val _carList = MutableLiveData<List<CarEntity>>()
    val carList: LiveData<List<CarEntity>> get() = _carList

    // LiveData to hold total count of cars
    private val _totalCarsCount = MutableLiveData<Int>()
    val totalCarsCount: LiveData<Int> get() = _totalCarsCount

    // LiveData to hold count of sold cars
    private val _soldCarsCount = MutableLiveData<Int>()
    val soldCarsCount: LiveData<Int> get() = _soldCarsCount

    // LiveData to hold count of unsold cars
    private val _unsoldCarsCount = MutableLiveData<Int>()
    val unsoldCarsCount: LiveData<Int> get() = _unsoldCarsCount

    // Fetch user data using the user ID retrieved from the use case
    fun fetchUserData() {
        viewModelScope.launch {
            val userIdResult = getCurrentUserIdUseCase() // Fetch the current user ID
            val userId = userIdResult.getOrNull() // Get the user ID from the result

            userIdResult.onSuccess {
                if (userId != null) {
                    // Fetch user data from the database if the user ID is valid
                    val result = getUserDataUseCase(userId)
                    _userData.value = result.map { userList -> userList.firstOrNull() ?: throw Exception("User not found") }
                } else {
                    // Handle failure to get user ID
                    _userData.value = Result.failure(Exception("Failed to get user ID"))
                }
            }.onFailure {
                // Handle failure in retrieving user ID
                _userData.value = Result.failure(it)
            }
        }
    }

    // Fetch the list of cars for the current user
    fun fetchCarsForUser() {
        viewModelScope.launch {
            val currentUserResult = getCurrentUserIdUseCase() // Get the current user ID
            val userId = currentUserResult.getOrNull() // Get the user ID from the result

            userId?.let {
                // Fetch cars associated with the user ID
                val result = getCarUserInDatabaseUseCase(it)
                result.onSuccess { cars ->
                    _carList.value = cars // Update the car list
                    updateCarCounts(cars) // Update car counts
                }.onFailure { exception ->
                    // Handle any errors in fetching cars
                    Log.e("SellerHomeViewModel", "Failed to fetch cars: ${exception.message}")
                }
            }
        }
    }

    // Update the counts of total, sold, and unsold cars
    private fun updateCarCounts(cars: List<CarEntity>) {
        val total = cars.size // Total number of cars
        val soldCount = cars.count { it.sold } // Count of sold cars
        val unsoldCount = total - soldCount // Count of unsold cars

        // Update LiveData with the counts
        _totalCarsCount.value = total
        _soldCarsCount.value = soldCount
        _unsoldCarsCount.value = unsoldCount
    }
}
