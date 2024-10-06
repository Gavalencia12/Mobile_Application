package com.example.carhive.Presentation.initial.Register.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.model.User
import com.example.carhive.Domain.usecase.user.SavePasswordUseCase
import com.example.carhive.Domain.usecase.user.SaveUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirstRegisterViewModel @Inject constructor(
    private val savePasswordUseCase: SavePasswordUseCase,
    private val saveUserPreferencesUseCase: SaveUserPreferencesUseCase
) : ViewModel() {

    fun saveFirstPartOfUserData(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            // Crear una instancia del modelo de dominio User
            val user = User(
                firstName = firstName,
                lastName = lastName,
                email = email
            )

            // Guardar los datos del usuario en SharedPreferences
            saveUserPreferencesUseCase(user)

            // Guardar la contraseña
            savePasswordUseCase(password)

            // Aquí puedes agregar lógica adicional para manejar el resultado del registro,
            // como mostrar un mensaje de éxito o error.
        }
    }
}
