package com.example.carhive.Presentation.initial.Register.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Data.initial.model.UserModel
import com.example.carhive.Data.initial.storage.UserPreferences
import com.example.carhive.Domain.initial.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirstRegisterViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
) : ViewModel() {

    fun saveFirstPartOfUserData(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            val user = UserModel(
                firstName = firstName,
                lastName = lastName,
                email = email
            )
            userPreferences.saveUser(user) // Guardar en SharedPreferences
            userPreferences.savePassword(password)
            // Registro del usuario
        }
    }
}