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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterestedUsersViewModel @Inject constructor(
    private val getInterestedUsersUseCase: GetInterestedUsersUseCase,
    private val getCarUserInDatabaseUseCase: GetCarUserInDatabaseUseCase,
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
                val interestedUsersSet = mutableSetOf<UserWithLastMessage>()
                val carIds = mutableListOf<CarWithLastMessage>()

                // Obtiene los coches del vendedor
                val carsResult = getCarUserInDatabaseUseCase(sellerId)
                if (carsResult.isSuccess) {
                    val carsInMessages = carsResult.getOrDefault(emptyList())
                    carIds.addAll(carsInMessages.map { carEntity ->
                        CarWithLastMessage(
                            car = carEntity,
                            lastMessage = "Último mensaje aquí",
                            lastMessageTimestamp = System.currentTimeMillis(),
                            isFile = false,
                            fileName = null
                        )
                    })
                }

                // Itera sobre cada coche del vendedor para obtener los usuarios interesados
                carIds.forEach { carWithMessage ->
                    val carId = carWithMessage.car.id
                    val usersInMessages = getInterestedUsersUseCase(sellerId, carId, "received", "users")
                        .filterIsInstance<UserWithLastMessage>()
                        .map { userWithLastMessage ->
                            val fileType = userWithLastMessage.fileType
                            val isFile = when {
                                fileType?.contains("application") == true -> true
                                fileType?.contains("image") == true -> true
                                fileType?.contains("video") == true -> true
                                else -> false
                            }
                            val displayMessage = if (isFile) userWithLastMessage.fileName ?: "Archivo adjunto" else userWithLastMessage.lastMessage
                            userWithLastMessage.copy(isFile = isFile, lastMessage = displayMessage.toString())
                        }

                    interestedUsersSet.addAll(usersInMessages)
                }

                _usersWithMessages.value = SellerInterestedData(
                    interestedUsers = interestedUsersSet.toList(),
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
                _usersWithMessages.value = SellerInterestedData(
                    interestedUsers = emptyList(),
                    cars = emptyList()
                )

                val usersInMessages = getInterestedUsersUseCase(ownerId, carId, "received", "users")
                    .filterIsInstance<UserWithLastMessage>()
                    .map { userWithLastMessage ->
                        val fileType = userWithLastMessage.fileType
                        val isFile = when {
                            fileType?.contains("application") == true -> true
                            fileType?.contains("image") == true -> true
                            fileType?.contains("video") == true -> true
                            else -> false
                        }
                        val displayMessage = if (isFile) userWithLastMessage.fileName ?: "Archivo adjunto" else userWithLastMessage.lastMessage
                        userWithLastMessage.copy(isFile = isFile, lastMessage = displayMessage.toString())
                    }

                _usersWithMessages.value = SellerInterestedData(
                    interestedUsers = usersInMessages,
                    cars = emptyList()
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error loading interested users: ${e.message}"
            }
        }
    }
}
