package com.example.carhive

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.models.Car
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class CarViewModel(application: Application) : AndroidViewModel(application) {

    private val _carList = MutableStateFlow<List<Car>>(emptyList())
    val carList: StateFlow<List<Car>> = _carList

    private val crud = CRUD(FirebaseDatabase.getInstance(), null)
    private var carIdToUpdate: String? = null

    private val _imageUris = MutableStateFlow<List<Uri>>(emptyList())
    val imageUris: StateFlow<List<Uri>> = _imageUris

    init {
        loadCars()  // Se llama al iniciar el ViewModel
    }

    fun addImageUri(uri: Uri) {
        _imageUris.value += uri
    }

    fun removeImageUri(uri: Uri) {
        _imageUris.value -= uri
    }

    fun createCar(name: String) {
        if (name.isNotEmpty() && _imageUris.value.size == 5) {
            val generatedId = crud.generateId("Car") ?: return
            val car = Car(id = generatedId, name = name)

            viewModelScope.launch {
                uploadImagesToFirebase(car.id) {
                    car.imageUrls.addAll(it) // Añadir las URLs de las imágenes al carro
                    crud.create(Car::class.java, car, getApplication())
                    loadCars()  // Recarga los carros después de crear uno
                    resetForm()  // Limpiar las imágenes seleccionadas y otros datos
                    showToast("Car created successfully")
                }
            }
        } else {
            showToast("Please enter a car name and select 3 images")
        }
    }


    private fun loadCars() {
        val reference = FirebaseDatabase.getInstance().getReference("Car")

        // Escucha en tiempo real
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val carList = mutableListOf<Car>()
                for (carSnapshot in snapshot.children) {
                    val car = carSnapshot.getValue(Car::class.java)
                    car?.let { carList.add(it) }
                }
                _carList.value = carList.sortedBy { it.name }  // Actualizar la lista de carros
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to load cars: ${error.message}")
            }
        })
    }


    fun updateCar(car: Car, name: String) {
        if (name.isNotEmpty()) {
            carIdToUpdate = car.id
            car.name = name

            viewModelScope.launch {
                val updates = mapOf("name" to name)
                crud.updateEntityIfExists(Car::class.java, car.id, updates, getApplication(), {
                    showToast("Car updated successfully")
                    loadCars()  // Refrescar la lista después de la actualización
                }, {
                    showToast("Failed to update car")
                })
            }
        } else {
            showToast("Car name cannot be empty")
        }
    }

    fun deleteCar(car: Car) {
        viewModelScope.launch {
            crud.delete(Car::class.java, car.id, getApplication())
            loadCars()  // Refrescar la lista después de eliminar
        }
    }

    private fun uploadImagesToFirebase(carId: String, onSuccess: (List<String>) -> Unit) {
        val urls = mutableListOf<String>()
        _imageUris.value.forEachIndexed { index, uri ->
            val storageRef = FirebaseStorage.getInstance().reference.child("car_images/$carId/${carId}_$index.jpg")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { url ->
                        urls.add(url.toString())
                        if (urls.size == _imageUris.value.size) onSuccess(urls)  // Continuar cuando se suben todas las imágenes
                    }
                }
                .addOnFailureListener {
                    showToast("Failed to upload image")
                }
        }
    }


    private fun resetForm() {
        carIdToUpdate = null
        _imageUris.value = emptyList()  // Resetea las imágenes seleccionadas
    }



    private fun showToast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }
}
