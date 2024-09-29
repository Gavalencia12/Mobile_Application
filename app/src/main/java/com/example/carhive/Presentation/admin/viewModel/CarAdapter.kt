package com.example.carhive.Presentation.admin.viewModel

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Data.admin.models.Car
import com.example.carhive.R
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CarViewModel(application: Application) : AndroidViewModel(application) {

    private val _carList = MutableStateFlow<List<Car>>(emptyList())
    val carList: StateFlow<List<Car>> = _carList

    private var carIdToUpdate: String? = null

    private val _imageUris = MutableStateFlow<List<Uri>>(emptyList())
    val imageUris: StateFlow<List<Uri>> = _imageUris

    // New StateFlow to track image updates
    private val _imageUpdateTrigger = MutableStateFlow(0)
    val imageUpdateTrigger: StateFlow<Int> = _imageUpdateTrigger

    init {
        loadCars()
    }

    fun addImageUris(uris: List<Uri>) {
        val currentList = _imageUris.value.toMutableList()
        val remainingSpace = 5 - currentList.size
        currentList.addAll(uris.take(remainingSpace))
        _imageUris.value = currentList
    }

    fun removeImageUri(uri: Uri) {
        _imageUris.value = _imageUris.value.toMutableList().apply { remove(uri) }
    }

    fun createCar(
        modelo: String,
        carColor: String,
        carSpeed: String,
        carAddOn: String,
        carDescription: String,
        carPrice: String
    ) {
        if (modelo.isNotEmpty() && _imageUris.value.size == 5) {
            val generatedId = FirebaseDatabase.getInstance().reference.child("Car").push().key ?: return
            val car = Car(id = generatedId, modelo = modelo, color = carColor, speed = carSpeed, addOn = carAddOn,
                description = carDescription, price = carPrice)

            viewModelScope.launch {
                uploadImagesToFirebase(car.id) { urls ->
                    car.imageUrls.addAll(urls)
                    FirebaseDatabase.getInstance().getReference("Car").child(car.id).setValue(car)
                        .addOnSuccessListener {
                            loadCars()
                            resetForm()
                            showToast(R.string.car_created_successfully)
                        }
                        .addOnFailureListener {
                            showToast(R.string.failed_to_create_car)
                        }
                }
            }
        } else {
            showToast(R.string.enter_car_name_and_select_images)
        }
    }

    private fun loadCars() {
        val reference = FirebaseDatabase.getInstance().getReference("Car")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val carList = mutableListOf<Car>()
                for (carSnapshot in snapshot.children) {
                    val car = carSnapshot.getValue(Car::class.java)
                    car?.let { carList.add(it) }
                }
                _carList.value = carList.sortedBy { it.modelo }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast(R.string.failed_to_load_cars)
            }
        })
    }

    fun updateCar(
        car: Car,
        newModelo: String,
        newColor: String,
        newSpeed: String,
        newAddOn: String,
        newDescription: String,
        newPrice: String,
        newImageUris: List<Uri>
    ) {
        val updates = mutableMapOf<String, Any>()

        if (newModelo.isNotEmpty()) updates["modelo"] = newModelo
        if (newColor.isNotEmpty()) updates["color"] = newColor
        if (newSpeed.isNotEmpty()) updates["speed"] = newSpeed
        if (newAddOn.isNotEmpty()) updates["addOn"] = newAddOn
        if (newDescription.isNotEmpty()) updates["description"] = newDescription
        if (newPrice.isNotEmpty()) updates["price"] = newPrice

        if (updates.isNotEmpty()) {
            FirebaseDatabase.getInstance().getReference("Car")
                .child(car.id)
                .updateChildren(updates)
                .addOnSuccessListener {
                    showToast(R.string.car_updated_successfully)
                    loadCars()
                }
                .addOnFailureListener {
                    showToast(R.string.failed_to_update_car)
                }
        }

        if (newImageUris.isNotEmpty()) {
            viewModelScope.launch {
                val storageRef = FirebaseStorage.getInstance().reference.child("car_images/${car.id}")
                val oldImageUrls = car.imageUrls.toMutableList()

                val imagesToDelete = oldImageUrls.filter { oldUrl ->
                    !newImageUris.any { uri -> oldUrl == uri.toString() }
                }

                val deleteTasks = imagesToDelete.map { urlToDelete ->
                    val fileName = urlToDelete.substringAfterLast("/")
                    storageRef.child(fileName).delete()
                }

                Tasks.whenAllComplete(deleteTasks).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val urisToUpload = newImageUris.filter { uri ->
                            !oldImageUrls.contains(uri.toString())
                        }

                        uploadImagesToFirebase(car.id, urisToUpload) { newImageUrls ->
                            car.imageUrls.clear()
                            car.imageUrls.addAll(newImageUrls)
                            car.imageUrls.addAll(oldImageUrls.filter { !imagesToDelete.contains(it) })

                            FirebaseDatabase.getInstance().getReference("Car")
                                .child(car.id)
                                .updateChildren(mapOf("imageUrls" to car.imageUrls))
                                .addOnSuccessListener {
                                    showToast(R.string.car_updated_successfully)
                                    loadCars()
                                    // Trigger image update
                                    _imageUpdateTrigger.value += 1
                                }
                                .addOnFailureListener {
                                    showToast(R.string.failed_to_update_car)
                                }
                        }
                    } else {
                        showToast(R.string.failed_to_delete_car_images)
                    }
                }
            }
        }
    }

    private fun uploadImagesToFirebase(
        carId: String,
        newImageUris: List<Uri>,
        onSuccess: (List<String>) -> Unit
    ) {
        val urls = mutableListOf<String>()
        newImageUris.forEachIndexed { index, uri ->
            val storageRef = FirebaseStorage.getInstance().reference.child("car_images/$carId/${carId}_$index.jpg")

            storageRef.downloadUrl.addOnSuccessListener { url ->
                if (!urls.contains(url.toString())) {
                    storageRef.putFile(uri)
                        .addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                urls.add(downloadUrl.toString())
                                if (urls.size == newImageUris.size) onSuccess(urls)
                            }
                        }
                        .addOnFailureListener {
                            showToast(R.string.failed_to_upload_image)
                        }
                }
            }.addOnFailureListener {
                storageRef.putFile(uri)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            urls.add(downloadUrl.toString())
                            if (urls.size == newImageUris.size) onSuccess(urls)
                        }
                    }
                    .addOnFailureListener {
                        showToast(R.string.failed_to_upload_image)
                    }
            }
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
                        if (urls.size == _imageUris.value.size) onSuccess(urls)
                    }
                }
                .addOnFailureListener {
                    showToast(R.string.failed_to_upload_image)
                }
        }
    }

    fun deleteCar(car: Car) {
        viewModelScope.launch {
            val storage = FirebaseStorage.getInstance()
            val databaseRef = FirebaseDatabase.getInstance().getReference("Car/${car.id}")

            val deleteTasks = car.imageUrls.map { imageUrl ->
                val imageRef = storage.getReferenceFromUrl(imageUrl)
                imageRef.delete()
            }

            Tasks.whenAllComplete(deleteTasks).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    databaseRef.removeValue().addOnSuccessListener {
                        showToast(R.string.car_deleted_successfully)
                        _carList.value = _carList.value.filter { it.id != car.id }
                    }.addOnFailureListener { exception ->
                        Log.e("DeleteCar", "Error deleting car data", exception)
                        showToast(R.string.failed_to_delete_car)
                    }
                } else {
                    Log.e("DeleteImage", "Error deleting images")
                    showToast(R.string.failed_to_delete_images)
                }
            }.addOnFailureListener { exception ->
                Log.e("DeleteImage", "Unexpected error deleting images", exception)
                showToast(R.string.failed_to_delete_images)
            }
        }
    }

    private fun resetForm() {
        _imageUris.value = emptyList()
        carIdToUpdate = null
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(getApplication(), messageResId, Toast.LENGTH_SHORT).show()
    }
}