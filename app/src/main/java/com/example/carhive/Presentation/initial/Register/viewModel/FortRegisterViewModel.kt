package com.example.carhive.Presentation.initial.Register.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class FortRegisterViewModel @Inject constructor() : ViewModel() {

    private val _isEmailVerified = MutableLiveData<Boolean>(false)
    val isEmailVerified: LiveData<Boolean> = _isEmailVerified

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Método para verificar el estado del email
    fun checkEmailVerification() {
        viewModelScope.launch {
            val user = auth.currentUser
            user?.reload()?.await() // Refresca el estado del usuario en Firebase
            _isEmailVerified.value = user?.isEmailVerified ?: false
        }
    }
}
