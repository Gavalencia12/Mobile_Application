package com.example.carhive.presentation.initial.Register.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private val _isPasswordVisible = MutableLiveData(false)
    val isPasswordVisible: LiveData<Boolean> get() = _isPasswordVisible

    private val _isConfirmPasswordVisible = MutableLiveData(false)
    val isConfirmPasswordVisible: LiveData<Boolean> get() = _isConfirmPasswordVisible

    // Alternar visibilidad de la contraseña
    fun togglePasswordVisibility() {
        _isPasswordVisible.value = _isPasswordVisible.value?.not()
    }

    // Alternar visibilidad de la confirmación de contraseña
    fun toggleConfirmPasswordVisibility() {
        _isConfirmPasswordVisible.value = _isConfirmPasswordVisible.value?.not()
    }

    fun saveFirstPartOfUserData(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
    ) {
        viewModelScope.launch {
            // Crear una instancia del modelo de dominio User
            val user = User(
                firstName = firstName,
                lastName = lastName,
                email = email,
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
