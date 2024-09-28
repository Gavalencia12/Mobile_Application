package com.example.carhive.Presentation.initial.Register.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.example.carhive.Data.initial.session.UserSessionManager
import com.example.carhive.Domain.initial.usecase.GetUserByIdUseCase
import com.example.carhive.Domain.initial.usecase.SaveUserToDatabaseUseCase
import com.example.carhive.Domain.initial.usecase.UploadToProfileImageUseCase
import com.example.carhive.Presentation.initial.Register.saveUserToDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThirdRegisterViewModel @Inject constructor(
    private val uploadToProfileImageUseCase: UploadToProfileImageUseCase,
    private val userSessionManager: UserSessionManager, // Inyectar UserSessionManager,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val saveUserToDatabaseUseCase: SaveUserToDatabaseUseCase // Inyectar el caso de uso para guardar en DB
) : ViewModel() {

    var uiState: ThirdRegisterUiState = ThirdRegisterUiState()

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            // Obtener el userId de la sesión
            val userId = userSessionManager.getUserId()
            if (userId == null) {
                uiState = uiState.copy(errorMessage = "User ID not found")
                return@launch
            }

            // Obtener los datos existentes del usuario
            val existingUserResult = getUserByIdUseCase(userId)
            if (existingUserResult.isFailure) {
                uiState = uiState.copy(errorMessage = existingUserResult.exceptionOrNull()?.localizedMessage ?: "Error retrieving user")
                return@launch
            }

            val existingUser = existingUserResult.getOrNull() ?: return@launch

            try {
                // Subir la imagen a Firebase Storage
                val result = uploadToProfileImageUseCase(userId, imageUri)
                result.onSuccess { uploadedImageUrl ->
                    // Actualizar el modelo del usuario con la URL de la imagen subida
                    val updatedUser = existingUser.copy(imageUrl = uploadedImageUrl)

                    // Guardar el modelo del usuario actualizado en la base de datos
                    val saveResult = saveUserToDatabaseUseCase(userId, updatedUser)
                    saveResult.onSuccess {
                        uiState = uiState.copy(isSuccess = true, imageUrl = uploadedImageUrl)
                    }.onFailure {
                        uiState = uiState.copy(errorMessage = it.localizedMessage ?: "Error saving user data to database")
                    }
                }.onFailure {
                    uiState = uiState.copy(errorMessage = it.localizedMessage ?: "Error uploading profile image")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.localizedMessage ?: "Error")
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }
}

data class ThirdRegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val imageUrl: String? = null,
    val errorMessage: String? = null
)
