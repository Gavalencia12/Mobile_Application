package com.example.carhive.Presentation.initial.Register.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Data.initial.storage.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecondRegisterViewModel @Inject constructor(
    private  val userPreferences: UserPreferences
) : ViewModel() {

    fun saveSecondPartOfUserData(
        curp: String,
        phoneNumber: String,
        voterID: String,
    ) {
        viewModelScope.launch {
            val user = userPreferences.getUser().copy(
                phoneNumber = phoneNumber,
                voterID = voterID,
                curp = curp
            )
            userPreferences.saveUser(user) // Guardar en SharedPreferences
            // Registro del usuario
        }
    }
}
