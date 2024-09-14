package com.example.carhive

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.models.Car
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class CarViewModel(application: Application) : AndroidViewModel(application) {

    private val _carList = MutableStateFlow<List<Car>>(emptyList())
    val carList: StateFlow<List<Car>> = _carList

    private val crud = CRUD(FirebaseDatabase.getInstance(), null)
    var carIdToUpdate: String? = null

    private val _imageUris = MutableStateFlow<List<Uri>>(emptyList())
    val imageUris: StateFlow<List<Uri>> = _imageUris

    init {
        loadCars()  // Se llama al iniciar el ViewModel
    }

    fun addImageUri(uri: Uri) {
        _imageUris.value = _imageUris.value + uri
    }

    fun removeImageUri(uri: Uri) {
        _imageUris.value = _imageUris.value - uri
    }

    fun createCar(name: String) {
        if (name.isNotEmpty() && _imageUris.value.size == 3) {
            val generatedId = crud.generateId("Car") ?: return
            val car = Car(id = generatedId, name = name)

            viewModelScope.launch {
                uploadImagesToFirebase(car.id) {
                    car.imageUrls.addAll(it) // Añadir las URLs de las imágenes al carro
                    crud.create(Car::class.java, car, getApplication())
                    loadCars()  // Recarga los carros después de crear uno
                }
            }
        } else {
            showToast("Please enter a car name and select 3 images")
        }
    }

    private fun loadCars() {
        viewModelScope.launch {
            crud.readAll(Car::class.java, getApplication()) { carList ->
                _carList.value = carList.sortedBy { it.name }
            }
        }
    }

    private fun uploadImagesToFirebase(carId: String, onSuccess: (List<String>) -> Unit) {
        val urls = mutableListOf<String>()
        _imageUris.value.forEachIndexed { index, uri ->
            val storageRef = FirebaseStorage.getInstance().reference.child("car_images/${carId}_$index.jpg")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { url ->
                        urls.add(url.toString())
                        if (urls.size == 3) onSuccess(urls)  // Solo continuar si ya se subieron las 3 imágenes
                    }
                }
                .addOnFailureListener {
                    showToast("Failed to upload image")
                }
        }
    }

    fun resetForm() {
        carIdToUpdate = null
        _imageUris.value = emptyList()  // Resetea las imágenes seleccionadas
    }

    private fun showToast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }
}
