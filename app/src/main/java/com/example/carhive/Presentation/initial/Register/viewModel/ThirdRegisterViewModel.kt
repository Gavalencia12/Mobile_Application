package com.example.carhive.Presentation.initial.Register.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Data.initial.storage.UserPreferences
import com.example.carhive.Domain.initial.usecase.RegisterUseCase
import com.example.carhive.Domain.initial.usecase.SaveUserToDatabaseUseCase
import com.example.carhive.Domain.initial.usecase.UploadToProfileImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThirdRegisterViewModel @Inject constructor(
    private val uploadToProfileImageUseCase: UploadToProfileImageUseCase,
    private val userPreferences: UserPreferences,
    private val saveUserToDatabaseUseCase: SaveUserToDatabaseUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    // Función para subir la imagen de perfil
    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            // Recuperar la contraseña y el usuario de UserPreferences
            val password = userPreferences.getPassword()
            val user = userPreferences.getUser()

            // Registrar el usuario en Firebase Authentication
            val userIdResult = registerUseCase(user.email, password ?: "")
            val userId = userIdResult.getOrNull() ?: return@launch // Si falla el registro, termina aquí

            // Subir la imagen al storage
            val uploadResult = uploadToProfileImageUseCase(userId, imageUri)
            val imageUrl = uploadResult.getOrNull() ?: return@launch // Si falla la subida, termina aquí

            // Crear un nuevo objeto de usuario con la URL de la imagen
            val updatedUser = user.copy(imageUrl = imageUrl)

            // Guardar la información actualizada del usuario en la base de datos
            val saveResult = saveUserToDatabaseUseCase(userId, updatedUser)

            // Verificar si se guardó correctamente
            if (saveResult.isSuccess) {
                // Limpiar UserPreferences después del registro exitoso
                userPreferences.clear()
            }
        }
    }
}
