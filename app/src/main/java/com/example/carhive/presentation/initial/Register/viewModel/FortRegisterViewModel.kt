package com.example.carhive.presentation.initial.Register.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.usecase.auth.IsVerifiedTheEmailUseCase
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class FortRegisterViewModel @Inject constructor(
    private val isVerifiedTheEmailUseCase: IsVerifiedTheEmailUseCase
) : ViewModel() {

    private val _isEmailVerified = MutableLiveData<Boolean>(false)
    val isEmailVerified: LiveData<Boolean> = _isEmailVerified

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun checkEmailVerification() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                try {
                    // Recarga el estado del usuario desde Firebase
                    user.reload().await()
                    // Verifica si el email está verificado
                    val isVerified = user.isEmailVerified
                    _isEmailVerified.value = isVerified
                } catch (e: Exception) {
                    // Manejo de error en caso de fallo
                    _isEmailVerified.value = false
                }
            }
        }
    }
}
