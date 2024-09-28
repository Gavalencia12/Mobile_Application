package com.example.carhive.Presentation.initial.Register.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Data.initial.session.UserSessionManager
import com.example.carhive.Domain.initial.usecase.GetUserByIdUseCase
import com.example.carhive.Domain.initial.usecase.RegisterUseCase
import com.example.carhive.Domain.initial.usecase.SaveUserToDatabaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecondRegisterViewModel @Inject constructor(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val saveUserToDatabaseUseCase: SaveUserToDatabaseUseCase,
    private val userSessionManager: UserSessionManager // Inyectar UserSessionManager
) : ViewModel() {

    var uiState: SecondRegisterUiState = SecondRegisterUiState()

    fun saveSecondPartOfUserData(
        curp: String,
        phoneNumber: String,
        voterID: String,
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            val userId = userSessionManager.getUserId() // Obtener el userId de la sesión
            if (userId == null) {
                uiState = uiState.copy(errorMessage = "User ID not found")
                return@launch
            }

            // Obtener el UserModel existente usando el caso de uso
            val existingUserResult = getUserByIdUseCase(userId)
            if (existingUserResult.isFailure) {
                uiState = uiState.copy(errorMessage = existingUserResult.exceptionOrNull()?.localizedMessage ?: "Error retrieving user")
                return@launch
            }

            val existingUser = existingUserResult.getOrNull() ?: return@launch

            // Actualizar los datos
            val updatedUser = existingUser.copy(
                curp = curp,
                phoneNumber = phoneNumber,
                voterID = voterID
            )

            try {
                val result = saveUserToDatabaseUseCase(userId, updatedUser)
                result.onSuccess {
                    uiState = uiState.copy(isSuccess = true)
                }.onFailure {
                    uiState = uiState.copy(errorMessage = it.localizedMessage ?: "Error")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.localizedMessage ?: "Error")
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }
}

data class SecondRegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)