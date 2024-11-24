package com.example.carhive.Presentation.user.viewModel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.model.Car
import com.example.carhive.data.model.CarEntity
import com.example.carhive.data.model.HistoryEntity
import com.example.carhive.data.model.UserEntity
import com.example.carhive.Domain.usecase.auth.GetCurrentUserIdUseCase
import com.example.carhive.Domain.usecase.database.GetAllCarsFromDatabaseUseCase
import com.example.carhive.Domain.usecase.database.GetCarUserInDatabaseUseCase
import com.example.carhive.Domain.usecase.database.GetUserDataUseCase
import com.example.carhive.Domain.usecase.database.UpdateCarToDatabaseUseCase
import com.example.carhive.Domain.usecase.favorites.AddCarToFavoritesUseCase
import com.example.carhive.Domain.usecase.favorites.RemoveCarFromFavoritesUseCase
import com.example.carhive.Domain.usecase.notifications.AddNotificationUseCase
import com.example.carhive.Domain.usecase.notifications.ListenForNewFavoritesUseCase
import com.example.carhive.R
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    private val getAllCarsFromDatabaseUseCase: GetAllCarsFromDatabaseUseCase,
    private val addCarToFavoritesUseCase: AddCarToFavoritesUseCase,
    private val removeCarFromFavoritesUseCase: RemoveCarFromFavoritesUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getUserDataUseCase: GetUserDataUseCase,
    private val firebaseDatabase: FirebaseDatabase,
    private val updateCarToDatabaseUseCase: UpdateCarToDatabaseUseCase,
    private val addNotificationUseCase: AddNotificationUseCase,
    private val listenForNewFavoritesUseCase: ListenForNewFavoritesUseCase,
    private val getCarUserInDatabaseUseCase: GetCarUserInDatabaseUseCase
) : AndroidViewModel(application) {

    // Lista predeterminada de marcas de autos obtenida desde los recursos de strings
    val defaultBrands = application.resources.getStringArray(R.array.brand_options).toList()

    // LiveData para mantener la lista de autos
    private val _carList = MutableLiveData<List<CarEntity>>()
    val carList: LiveData<List<CarEntity>> get() = _carList

    private val _recommendedCarList = MutableLiveData<List<CarEntity>>()
    val recommendedCarList: LiveData<List<CarEntity>> get() = _recommendedCarList

    // LiveData para mantener los datos del usuario
    private val _userData = MutableLiveData<UserEntity?>()
    val userData: LiveData<UserEntity?> get() = _userData

    private var allCars: List<CarEntity> = emptyList()
    private var favoriteCounts: Map<String, Int> = emptyMap() // Mapa para conteo de favoritos

    private val _brandList = MutableLiveData<List<String>>()
    val brandList: LiveData<List<String>> get() = _brandList

    // Filtros seleccionados
    var selectedBrands: MutableSet<String> = mutableSetOf()
    var selectedModel: String? = null
    var yearRange: Pair<Int, Int>? = null
    var priceRange: Pair<Int, Int?> = 0 to null
    var mileageRange: Pair<Int, Int?> = 0 to null
    var selectedColors: MutableSet<String> = mutableSetOf()

    // Color seleccionado estandarizado (primera letra en mayúscula)
    var selectedColor: String? = null
        set(value) {
            field = value?.replaceFirstChar { it.uppercase() }
        }

    // Filtro de ubicación actual
    private var selectedLocation: String? = null

    // LiveData para modelos y colores únicos de autos para opciones de filtro
    private val _uniqueCarModels = MutableLiveData<List<String>>()
    val uniqueCarModels: LiveData<List<String>> get() = _uniqueCarModels

    private val _uniqueCarColors = MutableLiveData<List<String>>()
    val uniqueCarColors: LiveData<List<String>> get() = _uniqueCarColors

    // Nuevos filtros
    var selectedTransmission: String? = null
    var selectedFuelType: String? = null
    var engineCapacityRange: Pair<Double?, Double?> = null to null // Rango para capacidad del motor

    var selectedCondition: String? = null

    /**
     * Obtiene la lista de autos y actualiza las listas únicas de modelos y colores para filtros.
     */
    fun fetchCars() {
        viewModelScope.launch {
            val result = getAllCarsFromDatabaseUseCase()
            if (result.isSuccess) {
                allCars = result.getOrNull()?.filter { !it.sold && it.approved } ?: emptyList()
                _carList.value = allCars
                loadUniqueCarModels()
                loadUniqueCarColors()
                fetchRecommendedCars() // Actualizar autos recomendados después de obtener todos los autos
            } else {
                showToast(R.string.error_fetching_cars)
            }
        }
    }

    fun fetchBrandsFromCars() {
        viewModelScope.launch {
            val result = getAllCarsFromDatabaseUseCase()
            if (result.isSuccess) {
                val cars = result.getOrNull()
                val uniqueBrands = cars?.map { it.brand }?.distinct() ?: emptyList()
                _brandList.value = uniqueBrands // Actualiza el LiveData de marcas
            } else {
                Log.e("UserViewModel", "Error al obtener los autos")
            }
        }
    }

    /**
     * Obtiene los autos recomendados basados en vistas, conteo de favoritos y nombre del modelo.
     */
    fun fetchRecommendedCars() {
        viewModelScope.launch {
            // Ordenar por vistas, reacciones y orden alfabético por nombre de modelo
            val sortedCars = allCars.sortedWith(
                compareByDescending<CarEntity> { it.views }
                    .thenByDescending { favoriteCounts[it.id] ?: 0 }
                    .thenBy { it.modelo }
            )

            // Tomar los primeros 5 autos
            _recommendedCarList.value = sortedCars.take(5)
        }
    }

    // Cargar modelos únicos de la lista de autos
    private fun loadUniqueCarModels() {
        val models = allCars.map { it.modelo }.distinct()
        _uniqueCarModels.value = models
    }

    // Cargar colores únicos de la lista de autos, capitalizados
    private fun loadUniqueCarColors() {
        val colors = allCars.map { it.color.replaceFirstChar { it.uppercase() } }.distinct()
        _uniqueCarColors.value = colors
    }

    /**
     * Aplica filtros a la lista de autos basada en criterios seleccionados, incluyendo ubicación, transmisión, tipo de combustible y rango de capacidad del motor.
     */
    fun applyFilters() {
        val filteredCars = allCars.filter { car ->
            val matchesBrand = selectedBrands.isEmpty() || selectedBrands.contains(car.brand)
            val matchesModel = selectedModel == null || car.modelo == selectedModel
            val matchesYear = yearRange?.let { (min, max) -> car.year.toIntOrNull()?.let { it in min..max } } ?: true
            val matchesColor = selectedColors.isEmpty() || selectedColors.contains(car.color.replaceFirstChar { it.uppercase() })
            val matchesLocation = selectedLocation == null || car.location == selectedLocation
            val matchesCondition = selectedCondition == null || car.condition == selectedCondition

            val carPrice = car.price.toIntOrNull() ?: 0
            val matchesPrice = carPrice in (priceRange.first)..(priceRange.second ?: Int.MAX_VALUE)

            val carMileage = car.mileage.toIntOrNull() ?: 0
            val matchesMileage = carMileage in (mileageRange.first)..(mileageRange.second ?: Int.MAX_VALUE)

            // Nuevos filtros
            val matchesTransmission = selectedTransmission == null || car.transmission.equals(selectedTransmission, ignoreCase = true)
            val matchesFuelType = selectedFuelType == null || car.fuelType.equals(selectedFuelType, ignoreCase = true)
            val matchesEngineCapacity = engineCapacityRange.let { (min, max) ->
                val engineCapacity = car.engineCapacity?.toDoubleOrNull() ?: 0.0
                (min == null || engineCapacity >= min) && (max == null || engineCapacity <= max)
            }

            matchesBrand && matchesModel && matchesYear && matchesColor && matchesLocation &&
                    matchesCondition && matchesPrice && matchesMileage &&
                    matchesTransmission && matchesFuelType && matchesEngineCapacity
        }
        _carList.value = filteredCars
    }

    fun filterCarsBySelectedBrands(selectedBrands: Set<String>, onFilterApplied: (List<CarEntity>) -> Unit) {
        val filteredCars = if (selectedBrands.isEmpty()) {
            allCars
        } else {
            allCars.filter { selectedBrands.contains(it.brand) }
        }
        onFilterApplied(filteredCars)
    }

    /**
     * Reinicia todos los filtros y muestra la lista completa de autos.
     */
    fun clearFilters() {
        selectedBrands.clear()
        selectedModel = null
        yearRange = null
        selectedColor = null
        priceRange = 0 to null
        mileageRange = 0 to null
        selectedLocation = null
        selectedCondition = null // Reinicia la condición seleccionada
        selectedTransmission = null
        selectedFuelType = null
        engineCapacityRange = null to null
        _carList.value = allCars
    }

    /**
     * Filtra autos por ubicación.
     */
    fun filterByLocation(location: String) {
        selectedLocation = location
        applyFilters() // Reaplicar filtros con ubicación actualizada
    }

    /**
     * Limpia el filtro de ubicación.
     */
    fun clearLocationFilter() {
        selectedLocation = null
        applyFilters() // Reaplicar filtros sin restricción de ubicación
    }

    /**
     * Verifica si un auto está marcado como favorito para el usuario actual.
     */
    fun isCarFavorite(carId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val currentUser = getCurrentUserIdUseCase()
            val userId = currentUser.getOrNull() ?: return@launch

            firebaseDatabase.getReference("Favorites/UserFavorites")
                .child(userId)
                .child(carId)
                .get()
                .addOnSuccessListener { snapshot -> callback(snapshot.exists()) }
                .addOnFailureListener {
                    showToast(R.string.error_fetching_favorite_status)
                    callback(false)
                }
        }
    }

    /**
     * Alterna el estado de favorito de un auto para el usuario actual.
     */
    fun toggleFavorite(car: CarEntity, isFavorite: Boolean) {
        viewModelScope.launch {
            val currentUser = getCurrentUserIdUseCase()
            val userId = currentUser.getOrNull() ?: return@launch

            val resultUser = getUserDataUseCase(userId)
            if (resultUser.isSuccess) {
                val user = resultUser.getOrNull()?.firstOrNull()
                val fullName = "${user?.firstName} ${user?.lastName}"

                if (isFavorite) {
                    val result = addCarToFavoritesUseCase(userId, fullName, car.id, car.id, car.ownerId)
                    if (result.isSuccess) {
                        showToast(R.string.car_added_to_favorites)
                        listenForNewFavorites(car.id, car.ownerId, car.modelo) // Inicia la lógica de notificaciones
                        addHistoryEvent(
                            userId,
                            "Add to Favorite",
                            "Car ${car.modelo} (${car.id}) added to favorites by $fullName."
                        )
                        // Actualizar el conteo de favoritos
                        updateFavoriteCount(car.id, increment = true)
                    }
                } else {
                    val result = removeCarFromFavoritesUseCase(userId, car.id)
                    if (result.isSuccess) {
                        showToast(R.string.car_removed_from_favorites)
                        addHistoryEvent(
                            userId,
                            "Remove from Favorite",
                            "Car ${car.modelo} (${car.id}) removed from favorites by $fullName."
                        )
                        // Actualizar el conteo de favoritos
                        updateFavoriteCount(car.id, increment = false)
                    }
                }
            } else {
                showToast(R.string.error_fetching_user_data)
            }
        }
    }

    /**
     * Actualiza el conteo de favoritos para un auto específico.
     */
    private fun updateFavoriteCount(carId: String, increment: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val favoriteCountRef = firebaseDatabase.getReference("Favorites/Counts/$carId")
            favoriteCountRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
                override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                    val currentCount = currentData.getValue(Int::class.java) ?: 0
                    currentData.value = if (increment) currentCount + 1 else if (currentCount > 0) currentCount - 1 else 0
                    return com.google.firebase.database.Transaction.success(currentData)
                }

                override fun onComplete(error: com.google.firebase.database.DatabaseError?, committed: Boolean, currentData: com.google.firebase.database.DataSnapshot?) {
                    if (error != null) {
                        Log.e("UserViewModel", "Failed to update favorite count: ${error.message}")
                    } else if (committed) {
                        // Actualizar el mapa de favoriteCounts
                        viewModelScope.launch(Dispatchers.Main) {
                            favoriteCounts = favoriteCounts.toMutableMap().apply {
                                this[carId] = (this[carId] ?: 0) + if (increment) 1 else -1
                                if (this[carId]!! < 0) this[carId] = 0
                            }
                            fetchRecommendedCars() // Recalcular autos recomendados
                        }
                    }
                }
            })
        }
    }

    /**
     * Agrega un evento al historial del usuario en la base de datos.
     */
    private fun addHistoryEvent(userId: String, eventType: String, message: String) {
        val timestamp = System.currentTimeMillis()
        val historyEntity = HistoryEntity(userId, timestamp, eventType, message)

        // Agregar el evento de historial directamente bajo el nodo 'History/userHistory'
        firebaseDatabase.getReference("History/userHistory")
            .push()
            .setValue(historyEntity)
            .addOnSuccessListener {
                // Opcional: Manejar la adición exitosa si es necesario
            }
            .addOnFailureListener { exception ->
                // Registrar o manejar el fallo aquí
                exception.printStackTrace()
            }
    }

    fun listenForNewFavorites(carId: String, sellerId: String, name: String) {
        viewModelScope.launch {
            listenForNewFavoritesUseCase(carId) { buyerId, buyerName, carId ->
                createBuyerNotification(buyerId, name)
                createSellerNotification(sellerId, buyerName, carId, name)
            }
        }
    }

    private fun createBuyerNotification(buyerId: String, name: String) {
        viewModelScope.launch {
            addNotificationUseCase(
                userId = buyerId,
                title = "Car added to favorites",
                message = "You have added the car $name to your favorites."
            )
        }
    }

    private fun createSellerNotification(sellerId: String, buyerName: String, carId: String, name: String) {
        viewModelScope.launch {
            addNotificationUseCase(
                userId = sellerId,
                title = "New favorite for your car",
                message = "Your car $name has been added to favorites by $buyerName."
            )
        }
    }

    // Mostrar mensaje de toast con el ID de recurso de string especificado
    private fun showToast(messageResId: Int) {
        Toast.makeText(getApplication(), getApplication<Application>().getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    private fun mapCarEntityToCar(carEntity: CarEntity): Car {
        return Car(
            id = carEntity.id,
            modelo = carEntity.modelo,
            color = carEntity.color,
            mileage = carEntity.mileage,
            brand = carEntity.brand,
            description = carEntity.description,
            price = carEntity.price,
            year = carEntity.year,
            sold = carEntity.sold,
            imageUrls = carEntity.imageUrls,
            ownerId = carEntity.ownerId,
            transmission = carEntity.transmission,
            fuelType = carEntity.fuelType,
            doors = carEntity.doors,
            engineCapacity = carEntity.engineCapacity,
            location = carEntity.location,
            condition = carEntity.condition,
            features = carEntity.features,
            vin = carEntity.vin,
            previousOwners = carEntity.previousOwners,
            views = carEntity.views,
            listingDate = carEntity.listingDate,
            lastUpdated = carEntity.lastUpdated
        )
    }

    /**
     * Maneja el conteo de vistas únicas para un auto verificando si el usuario actual lo ha visto.
     */
    fun handleCarView(car: CarEntity) {
        viewModelScope.launch {
            val currentUser = getCurrentUserIdUseCase()
            val userId = currentUser.getOrNull() ?: return@launch

            // Verificar si el usuario ya ha visto el auto en Firebase
            val viewsRef = firebaseDatabase.getReference("views/${car.id}/$userId")

            viewsRef.get().addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    // Incrementar el conteo de vistas solo si es una vista única
                    car.views += 1

                    // Agregar la vista única a Firebase y actualizar el conteo de vistas en la entrada de la base de datos del auto
                    viewsRef.setValue(true).addOnSuccessListener {
                        updateCarViewCountInDatabase(car)
                    }.addOnFailureListener {
                        showToast(R.string.error_updating_view_count)
                    }
                }
            }.addOnFailureListener {
                showToast(R.string.error_fetching_view_status)
            }
        }
    }

    private fun updateCarViewCountInDatabase(car: CarEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val ownerId = car.ownerId

            // Crear un objeto Car actualizado solo con el conteo de vistas
            val updatedCar = mapCarEntityToCar(car).copy(views = car.views)

            // Actualizar el conteo de vistas en la entrada de la base de datos del auto
            val result = updateCarToDatabaseUseCase(ownerId, car.id, updatedCar)
            result.fold(
                onSuccess = { fetchCars() },
                onFailure = {
                    withContext(Dispatchers.Main) {
                        showToast(R.string.error_updating_car)
                    }
                }
            )
        }
    }
}
