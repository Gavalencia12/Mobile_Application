package com.example.carhive.presentation.chat.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.usecase.chats.GetInterestedUsersUseCase
import com.example.carhive.Domain.usecase.chats.GetSupportUsersUseCase
import com.example.carhive.Domain.usecase.database.GetCarUserInDatabaseUseCase
import com.example.carhive.data.model.UserWithLastMessage
import com.example.carhive.data.model.CarWithLastMessage
import com.example.carhive.data.model.SellerInterestedData
import com.example.carhive.data.model.SupportUserData
import com.example.carhive.data.model.UserEntity
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class InterestedUsersViewModel @Inject constructor(
    private val getInterestedUsersUseCase: GetInterestedUsersUseCase,
    private val getCarUserInDatabaseUseCase: GetCarUserInDatabaseUseCase,
    private val getSupportUsersUseCase: GetSupportUsersUseCase,
    private val database: FirebaseDatabase
) : ViewModel() {

    private val _usersWithMessages = MutableLiveData<SellerInterestedData>()
    val usersWithMessages: LiveData<SellerInterestedData> get() = _usersWithMessages

    private val _carsWithMessages = MutableLiveData<List<CarWithLastMessage>>()
    val carsWithMessages: LiveData<List<CarWithLastMessage>> get() = _carsWithMessages

    private val _supportUserData = MutableLiveData<SupportUserData>()
    val supportUserData: LiveData<SupportUserData> get() = _supportUserData

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    /**
     * Loads users who have shown interest in the seller's cars.
     * Collects data on users with the last message exchanged and relevant car information.
     */
    fun loadInterestedUsersForSeller(sellerId: String) {
        viewModelScope.launch {
            try {
                val interestedUsersSet = mutableSetOf<UserWithLastMessage>()
                val carIds = mutableListOf<CarWithLastMessage>()

                // Retrieve the seller's cars
                val carsResult = getCarUserInDatabaseUseCase(sellerId)
                if (carsResult.isSuccess) {
                    val carsInMessages = carsResult.getOrDefault(emptyList())

                    // Retrieve owner info once, outside the loop
                    val ownerResult = database.reference.child("Users").child(sellerId).get().await()
                    val owner = ownerResult.getValue(UserEntity::class.java) ?: UserEntity()

                    // Iterate over the seller's cars and get interested users
                    carsInMessages.forEach { carEntity ->
                        val carId = carEntity.id
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
                                val displayMessage = if (isFile) userWithLastMessage.fileName ?: "Attached file" else userWithLastMessage.lastMessage
                                userWithLastMessage.copy(isFile = isFile, lastMessage = displayMessage)
                            }

                        // Create CarWithLastMessage with the correct owner
                        carIds.add(
                            CarWithLastMessage(
                                car = carEntity,
                                owner = owner, // Assign the retrieved owner information
                                lastMessage = carEntity.location,
                                lastMessageTimestamp = System.currentTimeMillis(),
                                isFile = false,
                                fileName = null
                            )
                        )

                        interestedUsersSet.addAll(usersInMessages)
                    }
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

    /**
     * Loads cars that the specified user has interacted with, displaying the last message.
     */
    fun loadCarsWithUserMessages(userId: String) {
        viewModelScope.launch {
            try {
                val carsWithMessages = mutableListOf<CarWithLastMessage>()

                // Primera llamada al caso de uso
                val carsResult = getInterestedUsersUseCase(userId, "", "sent", "cars")
                    .filterIsInstance<CarWithLastMessage>()

                carsResult.forEach { carWithLastMessage ->
                    val carId = carWithLastMessage.car.id

                    val messagesResult = getInterestedUsersUseCase(userId, carId, "received", "messages")
                        .filterIsInstance<UserWithLastMessage>()
                        .maxByOrNull { it.lastMessageTimestamp }

                    if (messagesResult != null) {
                        val updatedCarWithMessage = carWithLastMessage.copy(
                            lastMessage = messagesResult.lastMessage,
                            lastMessageTimestamp = messagesResult.lastMessageTimestamp,
                            isFile = messagesResult.isFile,
                            fileName = messagesResult.fileName,
                            fileType = messagesResult.fileType,
                            unreadCount = messagesResult.unreadCount
                        )
                        carsWithMessages.add(updatedCarWithMessage)
                    } else {
                        carsWithMessages.add(carWithLastMessage)
                    }
                }

                _carsWithMessages.value = carsWithMessages
                    .filter { it.lastMessage.isNotEmpty() }
                    .sortedByDescending { it.lastMessageTimestamp }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading cars: ${e.message}"
            }
        }
    }

    /**
     * Loads users who have shown interest in a specific car owned by a given seller.
     */
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
                        val displayMessage = if (isFile) userWithLastMessage.fileName ?: "Attached file" else userWithLastMessage.lastMessage
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

    /**
     * Load of users who have sent a message to technical support.
     */
    fun loadSupportUsers(ownerId: String) {
        viewModelScope.launch {
            try {
                val supportData = getSupportUsersUseCase(ownerId)
                _supportUserData.postValue(supportData)
            } catch (e: Exception) {
                _errorMessage.postValue("Error loading support users: ${e.message}")
            }
        }
    }

}
