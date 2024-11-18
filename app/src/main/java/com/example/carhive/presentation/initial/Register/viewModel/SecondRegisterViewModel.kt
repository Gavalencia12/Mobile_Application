package com.example.carhive.presentation.initial.Register.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.usecase.user.GetUserPreferencesUseCase
import com.example.carhive.Domain.usecase.user.SaveUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecondRegisterViewModel @Inject constructor(
    private  val saveUserPreferencesUseCase: SaveUserPreferencesUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase
) : ViewModel() {

    fun saveSecondPartOfUserData(
        curp: String,
        phoneNumber: String,
        voterID: String,
        terms: Boolean
    ) {
        viewModelScope.launch {
            val existingUser = getUserPreferencesUseCase()
            val update = existingUser.getOrNull() ?: return@launch
            val user = update.copy(
                phoneNumber = phoneNumber,
                voterID = voterID,
                curp = curp,
                termsUser = terms
            )
            saveUserPreferencesUseCase(user) // Guardar en SharedPreferences
            // Registro del usuario
        }
    }
}
