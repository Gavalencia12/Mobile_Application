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

    // Flow to hold the list of cars
    private val _carList = MutableStateFlow<List<Car>>(emptyList())
    val carList: StateFlow<List<Car>> = _carList

    private val crud = CRUD(FirebaseDatabase.getInstance(), null)
    private var carIdToUpdate: String? = null

    // Flow to hold selected image URIs
    private val _imageUris = MutableStateFlow<List<Uri>>(emptyList())
    val imageUris: StateFlow<List<Uri>> = _imageUris

    init {
        loadCars()  // Load cars on initialization
    }

    /**
     * Adds an image URI to the list of selected images.
     */
    fun addImageUri(uri: Uri) {
        _imageUris.value += uri
    }

    /**
     * Removes an image URI from the list of selected images.
     */
    fun removeImageUri(uri: Uri) {
        _imageUris.value -= uri
    }

    /**
     * Creates a new car with the specified name and uploads associated images to Firebase Storage.
     * Ensures that the name is not empty and exactly 5 images are selected.
     */
    fun createCar(name: String) {
        if (name.isNotEmpty() && _imageUris.value.size == 5) {
            val generatedId = crud.generateId("Car") ?: return
            val car = Car(id = generatedId, name = name)

            viewModelScope.launch {
                uploadImagesToFirebase(car.id) { urls ->
                    car.imageUrls.addAll(urls)
                    // Corrected call to crud.create
                    crud.create(car, car.id, Car::class.java, getApplication())
                    loadCars()  // Reload the car list after creating a car
                    resetForm()  // Clear the selected images and reset form fields
                    showToast(R.string.car_created_successfully)
                }
            }
        } else {
            showToast(R.string.enter_car_name_and_select_images)
        }
    }


    /**
     * Loads the list of cars from Firebase in real-time.
     */
    private fun loadCars() {
        val reference = FirebaseDatabase.getInstance().getReference("Car")

        // Real-time listener to update car list on data change
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val carList = mutableListOf<Car>()
                for (carSnapshot in snapshot.children) {
                    val car = carSnapshot.getValue(Car::class.java)
                    car?.let { carList.add(it) }
                }
                _carList.value = carList.sortedBy { it.name }  // Sort cars by name
            }

            override fun onCancelled(error: DatabaseError) {
                showToast(R.string.failed_to_load_cars)
            }
        })
    }

    /**
     * Updates an existing car's name.
     */
    fun updateCar(car: Car, name: String) {
        if (name.isNotEmpty()) {
            carIdToUpdate = car.id
            car.name = name

            viewModelScope.launch {
                val updates = mapOf("name" to name)
                crud.updateEntityIfExists(Car::class.java, car.id, updates, getApplication(), {
                    showToast(R.string.car_updated_successfully)
                    loadCars()
                }, {
                    showToast(R.string.failed_to_update_car)
                })
            }
        } else {
            showToast(R.string.car_name_cannot_be_empty)
        }
    }

    /**
     * Deletes a car from Firebase.
     */
    fun deleteCar(car: Car) {
        viewModelScope.launch {
            crud.delete(Car::class.java, car.id, getApplication())
            loadCars()
        }
    }

    /**
     * Uploads images to Firebase Storage and returns their download URLs.
     */
    private fun uploadImagesToFirebase(carId: String, onSuccess: (List<String>) -> Unit) {
        val urls = mutableListOf<String>()
        _imageUris.value.forEachIndexed { index, uri ->
            val storageRef = FirebaseStorage.getInstance().reference.child("car_images/$carId/${carId}_$index.jpg")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { url ->
                        urls.add(url.toString())
                        if (urls.size == _imageUris.value.size) onSuccess(urls)  // Call success when all images are uploaded
                    }
                }
                .addOnFailureListener {
                    showToast(R.string.failed_to_upload_image)
                }
        }
    }

    /**
     * Resets the form fields and clears the selected images.
     */
    private fun resetForm() {
        carIdToUpdate = null
        _imageUris.value = emptyList()
    }

    /**
     * Shows a Toast message with the given resource string ID.
     */
    private fun showToast(messageResId: Int) {
        Toast.makeText(getApplication(), getApplication<Application>().getString(messageResId), Toast.LENGTH_SHORT).show()
    }
}
