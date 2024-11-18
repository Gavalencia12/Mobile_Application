package com.example.carhive.presentation.initial.Login.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecoveryPasswordViewModel @Inject constructor(

) : ViewModel() {

    // Para gestionar el estado de éxito o error del correo de recuperación
    private val _emailState = MutableStateFlow<EmailState>(EmailState.Idle)
    val emailState: StateFlow<EmailState> = _emailState

    // FirebaseAuth para enviar el correo de recuperación
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Método para enviar el correo de recuperación
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _emailState.value = EmailState.Loading

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _emailState.value = EmailState.Success
                    } else {
                        _emailState.value = EmailState.Error(task.exception?.message ?: "Error al enviar el correo")
                    }
                }
        }
    }

}


// Estado de la recuperación de contraseña
sealed class EmailState {
    data object Idle : EmailState()
    data object Loading : EmailState()
    data object Success : EmailState()
    data class Error(val message: String) : EmailState()
}
