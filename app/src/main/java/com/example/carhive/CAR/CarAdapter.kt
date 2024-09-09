package com.example.carhive

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.models.Car
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CarViewModel(application: Application) : AndroidViewModel(application) {

    private val _carList = MutableStateFlow<List<Car>>(emptyList())
    val carList: StateFlow<List<Car>> = _carList

    private val crud = CRUD(FirebaseDatabase.getInstance(), null)
    var carIdToUpdate: String? = null

    init {
        loadCars()
    }

    fun createCar(name: String) {
        if (name.isNotEmpty()) {
            val generatedId = crud.generateId("Car") ?: return
            val car = Car(id = generatedId, name = name)
            crud.create(Car::class.java, car, getApplication())
            loadCars()
        } else {
            showToast("Please enter a car name")
        }
    }

    fun updateCar(name: String) {
        if (carIdToUpdate != null && name.isNotEmpty()) {
            crud.updateEntityIfExists(Car::class.java, carIdToUpdate!!, mapOf("name" to name), getApplication(),
                onSuccess = {
                    resetForm()
                    loadCars()
                },
                onError = {
                    showToast("Car does not exist anymore.")
                    resetForm()
                }
            )
        } else {
            showToast("Please enter a car name")
        }
    }

    fun deleteCar(car: Car) {
        crud.delete(Car::class.java, car.id, getApplication())
        loadCars()
    }

    fun prepareForUpdate(car: Car) {
        carIdToUpdate = car.id
    }

    private fun resetForm() {
        carIdToUpdate = null
    }

    private fun loadCars() {
        crud.read(Car::class.java, { cars ->
            _carList.value = cars
        }, getApplication())
    }

    private fun showToast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }
}