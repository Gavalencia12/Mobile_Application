package com.example.carhive.presentation.chat.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.usecase.chats.GetInterestedUsersUseCase
import com.example.carhive.Domain.usecase.database.GetCarUserInDatabaseUseCase
import com.example.carhive.data.model.UserWithLastMessage
import com.example.carhive.data.model.CarWithLastMessage
import com.example.carhive.data.model.SellerInterestedData
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class InterestedUsersViewModel @Inject constructor(
    private val getInterestedUsersUseCase: GetInterestedUsersUseCase,
    private val getCarUserInDatabaseUseCase: GetCarUserInDatabaseUseCase
) : ViewModel() {

    private val _usersWithMessages = MutableLiveData<SellerInterestedData>()
    val usersWithMessages: LiveData<SellerInterestedData> get() = _usersWithMessages

    private val _carsWithMessages = MutableLiveData<List<CarWithLastMessage>>()
    val carsWithMessages: LiveData<List<CarWithLastMessage>> get() = _carsWithMessages

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun loadInterestedUsersForSeller(sellerId: String) {
        viewModelScope.launch {
            try {
                val interestedUsers = mutableListOf<UserWithLastMessage>()
                val carIds = mutableListOf<CarWithLastMessage>()

                // Obtén los coches del vendedor
                val carsResult = getCarUserInDatabaseUseCase(sellerId)
                if (carsResult.isSuccess) {
                    val carsInMessages = carsResult.getOrDefault(emptyList())
                    carIds.addAll(carsInMessages.map { carEntity ->
                        // Convertir cada CarEntity en CarWithLastMessage
                        CarWithLastMessage(carEntity, "Último mensaje aquí", System.currentTimeMillis())
                    })
                }

                // Itera sobre cada coche del vendedor para obtener los usuarios interesados
                carIds.forEach { carWithMessage ->
                    val carId = carWithMessage.car.id
                    val usersInMessages = getInterestedUsersUseCase(sellerId, carId, "received", "users")
                    interestedUsers.addAll(usersInMessages.filterIsInstance<UserWithLastMessage>())
                }

                _usersWithMessages.value = SellerInterestedData(
                    interestedUsers = interestedUsers,
                    cars = carIds
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error loading interested users: ${e.message}"
            }
        }
    }

    fun loadCarsWithUserMessages(userId: String) {
        viewModelScope.launch {
            try {
                val cars = getInterestedUsersUseCase(userId, "", "sent", "cars")
                    .filterIsInstance<CarWithLastMessage>()
                    .sortedByDescending { it.lastMessageTimestamp } // Ordenar por timestamp

                _carsWithMessages.value = cars
            } catch (e: Exception) {
                _errorMessage.value = "Error loading cars: ${e.message}"
            }
        }
    }

    fun loadInterestedUsersForCar(ownerId: String, carId: String) {
        viewModelScope.launch {
            try {
                val usersInMessages = getInterestedUsersUseCase(ownerId, carId, "received", "users")
                    .filterIsInstance<UserWithLastMessage>()
                _usersWithMessages.value = SellerInterestedData(
                    interestedUsers = usersInMessages,
                    cars = emptyList() // Aquí puedes ajustar si quieres manejar coches también
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error loading interested users: ${e.message}"
            }
        }
    }


}
