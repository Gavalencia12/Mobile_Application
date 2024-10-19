package com.example.carhive.Presentation.user.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Domain.usecase.database.GetAllCarsFromDatabaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getAllCarsFromDatabaseUseCase: GetAllCarsFromDatabaseUseCase
) : ViewModel() {

    private val _carList = MutableLiveData<List<CarEntity>>()
    val carList: LiveData<List<CarEntity>> get() = _carList

    fun fetchCars() {
        viewModelScope.launch {
            val result = getAllCarsFromDatabaseUseCase()
            if (result.isSuccess) {
                _carList.value = result.getOrNull() ?: emptyList()
            } else {
                // Maneja el error si es necesario
                Log.e("UserViewModel", "Error fetching cars: ${result.exceptionOrNull()}")
            }
        }
    }
}