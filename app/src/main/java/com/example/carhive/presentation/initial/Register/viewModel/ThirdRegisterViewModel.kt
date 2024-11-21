package com.example.carhive.presentation.initial.Register.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.usecase.user.ClearUserPreferencesUseCase
import com.example.carhive.Domain.usecase.user.GetPasswordUseCase
import com.example.carhive.Domain.usecase.user.GetUserPreferencesUseCase
import com.example.carhive.Domain.usecase.auth.RegisterUseCase
import com.example.carhive.Domain.usecase.database.SaveUserToDatabaseUseCase
import com.example.carhive.Domain.usecase.database.UploadToProfileImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThirdRegisterViewModel @Inject constructor(
    private val uploadToProfileImageUseCase: UploadToProfileImageUseCase,
    private val getPasswordUseCase: GetPasswordUseCase, // Asegúrate de que este caso de uso esté definido
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase, // Agrega el caso de uso para obtener el usuario
    private val saveUserToDatabaseUseCase: SaveUserToDatabaseUseCase,
    private val registerUseCase: RegisterUseCase,
    private val clearUserPreferencesUseCase: ClearUserPreferencesUseCase,
) : ViewModel() {

    // Función para subir la imagen de perfil
    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                // Recuperar la contraseña y el usuario usando los casos de uso
                val passwordResult = getPasswordUseCase() // Obtén la contraseña

                val userResult = getUserPreferencesUseCase() // Obtén el usuario

                val password = passwordResult.getOrNull() ?: return@launch
                val user = userResult.getOrNull() ?: return@launch

                // Registrar el usuario en Firebase Authentication
                val userIdResult = registerUseCase(user.email, password)
                val userId = userIdResult.getOrNull() ?: return@launch // Si falla el registro, termina aquí

                // Subir la imagen al storage
                val uploadResult = uploadToProfileImageUseCase(userId, imageUri)
                val imageUrl2 = uploadResult.getOrNull() ?: return@launch // Si falla la subida, termina aquí

                val verificationTimestamp = System.currentTimeMillis().toString()

                // Crear un nuevo objeto de usuario con la URL de la imagen
                val updatedUser = user.copy(imageUrl2 = imageUrl2, verificationTimestamp = verificationTimestamp)

                // Guardar la información actualizada del usuario en la base de datos
                val saveResult = saveUserToDatabaseUseCase(userId, updatedUser)

                // Verificar si se guardó correctamente
                if (saveResult.isSuccess) {
                    // Limpiar UserPreferences después del registro exitoso
                    clearUserPreferencesUseCase() // Esto se podría mejorar al agregar un caso de uso para limpiar
                } else {
                    // Manejar el error de guardado (opcional)
                }
            } catch (e: Exception) {
                // Manejar excepciones (puedes usar un log o mostrar un mensaje al usuario)
            }
        }
    }

}
