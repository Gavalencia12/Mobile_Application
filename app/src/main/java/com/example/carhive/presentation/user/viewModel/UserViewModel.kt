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
) : AndroidViewModel(application) {

    // Default list of car brands retrieved from string resources
    val defaultBrands = application.resources.getStringArray(R.array.brand_options).toList()

    // LiveData to hold the list of cars
    private val _carList = MutableLiveData<List<CarEntity>>()
    val carList: LiveData<List<CarEntity>> get() = _carList

    private val _recommendedCarList = MutableLiveData<List<CarEntity>>()
    val recommendedCarList: LiveData<List<CarEntity>> get() = _recommendedCarList

    // LiveData to hold user data
    private val _userData = MutableLiveData<UserEntity?>()
    val userData: LiveData<UserEntity?> get() = _userData

    private var allCars: List<CarEntity> = emptyList()
    private var favoriteCounts: MutableMap<String, Int> = mutableMapOf() // Mapa para conteo de favoritos

    private val _brandList = MutableLiveData<List<String>>()
    val brandList: LiveData<List<String>> get() = _brandList

    // Filtros seleccionados
    var selectedBrands: MutableSet<String> = mutableSetOf()
    var selectedModel: String? = null
    var yearRange: Pair<Int, Int>? = null
    var priceRange: Pair<Int, Int?> = 0 to null
    var mileageRange: Pair<Int, Int?> = 0 to null
    var selectedColors: MutableSet<String> = mutableSetOf()

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
                loadUniqueCarModels()
                loadUniqueCarColors()
                fetchRecommendedCars()
                applyFilters() // Aplicar filtros después de cargar los autos
                Log.d("UserViewModel", "Autos cargados y filtros aplicados")
            } else {
                showToast(R.string.error_fetching_cars)
                Log.e("UserViewModel", "Error al obtener los autos: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    /**
     * Obtiene las marcas únicas de los autos cargados.
     */
    fun fetchBrandsFromCars() {
        viewModelScope.launch {
            val result = getAllCarsFromDatabaseUseCase()
            if (result.isSuccess) {
                val cars = result.getOrNull()
                val uniqueBrands = cars?.map { it.brand }?.distinct() ?: emptyList()
                _brandList.value = uniqueBrands // Actualiza el LiveData de marcas
                Log.d("UserViewModel", "Marcas únicas cargadas: $uniqueBrands")
            } else {
                Log.e("UserViewModel", "Error al obtener los autos: ${result.exceptionOrNull()?.message}")
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
            Log.d("UserViewModel", "Autos recomendados actualizados: ${_recommendedCarList.value?.size} autos")
        }
    }

    /**
     * Carga y actualiza los modelos únicos de la lista de autos.
     */
    private fun loadUniqueCarModels() {
        val models = allCars.map { it.modelo }.distinct()
        _uniqueCarModels.value = models
        Log.d("UserViewModel", "Modelos únicos cargados: $models")
    }

    /**
     * Carga y actualiza los colores únicos de la lista de autos.
     */
    private fun loadUniqueCarColors() {
        val colors = allCars.map { it.color.replaceFirstChar { it.uppercase() } }.distinct()
        _uniqueCarColors.value = colors
        Log.d("UserViewModel", "Colores únicos cargados: $colors")
    }

    /**
     * Aplica filtros a la lista de autos basada en criterios seleccionados, incluyendo ubicación, transmisión, tipo de combustible y rango de capacidad del motor.
     */
    fun applyFilters() {
        var filteredList = allCars

        // Filtro por marcas
        if (selectedBrands.isNotEmpty()) {
            filteredList = filteredList.filter { car ->
                selectedBrands.contains(car.brand)
            }
        }

        // Filtro por modelo
        selectedModel?.let {
            filteredList = filteredList.filter { car ->
                car.modelo.equals(it, ignoreCase = true)
            }
        }

        // Filtro por rango de años
        yearRange?.let { (minYear, maxYear) ->
            filteredList = filteredList.filter { car ->
                val year = car.year.toIntOrNull() ?: 0
                (minYear <= year) && (year <= maxYear)
            }
        }

        // Filtro por colores
        if (selectedColors.isNotEmpty()) {
            filteredList = filteredList.filter { car ->
                selectedColors.contains(car.color.replaceFirstChar { it.uppercase() })
            }
        }

        // Filtro por ubicación
        selectedLocation?.let {
            filteredList = filteredList.filter { car ->
                car.location.equals(it, ignoreCase = true)
            }
        }

        // Filtro por condición
        selectedCondition?.let {
            filteredList = filteredList.filter { car ->
                car.condition.equals(it, ignoreCase = true)
            }
        }

        // Filtro por precio
        priceRange.let { (minPrice, maxPrice) ->
            filteredList = filteredList.filter { car ->
                val price = car.price.toIntOrNull() ?: 0
                price >= minPrice && (maxPrice == null || price <= maxPrice)
            }
        }

        // Filtro por kilometraje
        mileageRange.let { (minMileage, maxMileage) ->
            filteredList = filteredList.filter { car ->
                val mileage = car.mileage.toIntOrNull() ?: 0
                mileage >= minMileage && (maxMileage == null || mileage <= maxMileage)
            }
        }

        // Filtro por transmisión
        selectedTransmission?.let {
            filteredList = filteredList.filter { car ->
                car.transmission.equals(it, ignoreCase = true)
            }
        }

        // Filtro por tipo de combustible
        selectedFuelType?.let {
            filteredList = filteredList.filter { car ->
                car.fuelType.equals(it, ignoreCase = true)
            }
        }

        // Filtro por capacidad del motor
        engineCapacityRange.let { (minCapacity, maxCapacity) ->
            filteredList = filteredList.filter { car ->
                val capacity = car.engineCapacity?.toDoubleOrNull() ?: 0.0
                (minCapacity == null || capacity >= minCapacity) && (maxCapacity == null || capacity <= maxCapacity)
            }
        }

        // Actualizar el LiveData con la lista filtrada
        _carList.value = filteredList
    }

    /**
     * Filtra autos por marcas seleccionadas y ejecuta un callback con la lista filtrada.
     */
    fun filterCarsBySelectedBrands(selectedBrands: Set<String>, onFilterApplied: (List<CarEntity>) -> Unit) {
        val filteredCars = if (selectedBrands.isEmpty()) {
            allCars
        } else {
            allCars.filter { it.brand in selectedBrands }
        }
        onFilterApplied(filteredCars)
        Log.d("UserViewModel", "Autos filtrados por marcas seleccionadas: ${selectedBrands.joinToString()}")
    }

    /**
     * Reinicia todos los filtros y muestra la lista completa de autos.
     */
    fun clearFilters() {
        selectedBrands.clear()
        selectedModel = null
        yearRange = null
        selectedColors.clear() // Asegurar que se limpien los colores seleccionados
        selectedLocation = null
        selectedCondition = null // Reinicia la condición seleccionada
        selectedTransmission = null
        selectedFuelType = null
        engineCapacityRange = null to null
        priceRange = 0 to null
        mileageRange = 0 to null
        applyFilters() // Aplicar filtros después de limpiar
        Log.d("UserViewModel", "Filtros limpiados")
    }

    /**
     * Filtra autos por ubicación.
     */
    fun filterByLocation(location: String) {
        selectedLocation = location
        applyFilters() // Reaplicar filtros con ubicación actualizada
        Log.d("UserViewModel", "Filtrado por nueva ubicación: $location")
    }

    /**
     * Limpia el filtro de ubicación.
     */
    fun clearLocationFilter() {
        selectedLocation = null
        applyFilters() // Reaplicar filtros sin restricción de ubicación
        Log.d("UserViewModel", "Filtro de ubicación limpiado")
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
                .addOnSuccessListener { snapshot ->
                    val exists = snapshot.exists()
                    Log.d("UserViewModel", "Auto $carId favorito: $exists")
                    callback(exists)
                }
                .addOnFailureListener {
                    showToast(R.string.error_fetching_favorite_status)
                    Log.e("UserViewModel", "Error al verificar favorito: ${it.message}")
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
                        sendFavoriteNotifications(car, userId, fullName)
                        addHistoryEvent(
                            userId,
                            "Add to Favorite",
                            "Car ${car.modelo} (${car.id}) added to favorites by $fullName."
                        )
                        // Actualizar el conteo de favoritos
                        updateFavoriteCount(car.id, increment = true)
                        Log.d("UserViewModel", "Auto ${car.id} agregado a favoritos por $fullName")
                    } else {
                        Log.e("UserViewModel", "Error al agregar favorito: ${result.exceptionOrNull()?.message}")
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
                        Log.d("UserViewModel", "Auto ${car.id} removido de favoritos por $fullName")
                    } else {
                        showToast(R.string.error_removing_favorite)
                        Log.e("UserViewModel", "Error al remover favorito: ${result.exceptionOrNull()?.message}")
                    }
                }
            } else {
                showToast(R.string.error_fetching_user_data)
                Log.e("UserViewModel", "Error al obtener datos del usuario: ${resultUser.exceptionOrNull()?.message}")
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
                            val currentCount = favoriteCounts[carId] ?: 0
                            favoriteCounts[carId] = if (increment) currentCount + 1 else if (currentCount > 0) currentCount - 1 else 0
                            fetchRecommendedCars() // Recalcular autos recomendados
                            Log.d("UserViewModel", "Favorite count actualizado para $carId: ${favoriteCounts[carId]}")
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
                Log.d("UserViewModel", "Evento de historial agregado exitosamente")
            }
            .addOnFailureListener { exception ->
                // Registrar o manejar el fallo aquí
                Log.e("UserViewModel", "Error al agregar evento de historial: ${exception.message}")
            }
    }

    private fun sendFavoriteNotifications(car: CarEntity, userId: String, buyerName: String) {
        viewModelScope.launch {
            // Notificación para el comprador
            addNotificationUseCase(
                userId = userId,
                title = "Car added to favorites",
                message = "You have added the car ${car.modelo} to your favorites."
            )

            // Notificación para el vendedor
            addNotificationUseCase(
                userId = car.ownerId,
                title = "New favorite for your car",
                message = "Your car ${car.modelo} has been added to favorites by $buyerName."
            )
        }
    }

    /**
     * Muestra un mensaje de toast con el ID de recurso de string especificado.
     */
    private fun showToast(messageResId: Int) {
        Toast.makeText(getApplication(), getApplication<Application>().getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    /**
     * Mapea un `CarEntity` a un objeto `Car`.
     */
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
                        Log.d("UserViewModel", "Vista única registrada para el auto ${car.id} por el usuario $userId")
                    }.addOnFailureListener {
                        showToast(R.string.error_updating_view_count)
                        Log.e("UserViewModel", "Error al registrar vista única: ${it.message}")
                    }
                } else {
                    Log.d("UserViewModel", "El usuario $userId ya ha visto el auto ${car.id}")
                }
            }.addOnFailureListener {
                showToast(R.string.error_fetching_view_status)
                Log.e("UserViewModel", "Error al verificar vista: ${it.message}")
            }
        }
    }

    /**
     * Actualiza el conteo de vistas en la base de datos.
     */
    private fun updateCarViewCountInDatabase(car: CarEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val ownerId = car.ownerId

            // Crear un objeto Car actualizado solo con el conteo de vistas
            val updatedCar = mapCarEntityToCar(car).copy(views = car.views)

            // Actualizar el conteo de vistas en la entrada de la base de datos del auto
            val result = updateCarToDatabaseUseCase(ownerId, car.id, updatedCar)
            result.fold(
                onSuccess = {
                    Log.d("UserViewModel", "Conteo de vistas actualizado para el auto ${car.id}")
                    withContext(Dispatchers.Main) {
                        fetchCars() // Re-cargar los autos para reflejar el cambio
                    }
                },
                onFailure = {
                    withContext(Dispatchers.Main) {
                        showToast(R.string.error_updating_car)
                        Log.e("UserViewModel", "Error al actualizar el auto: ${it.message}")
                    }
                }
            )
        }
    }

    /**
     * Método faltante: fetchUniqueCarModels
     * Este método simplemente llama a loadUniqueCarModels y puede ser usado si se necesita acceder públicamente.
     */
    fun fetchUniqueCarModels() {
        loadUniqueCarModels()
    }
}