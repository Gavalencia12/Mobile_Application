package com.example.carhive.presentation.initial.Register.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.usecase.auth.IsVerifiedTheEmailUseCase
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
                    user.reload().await()
                    val isVerified = user.isEmailVerified
                    _isEmailVerified.value = isVerified

                    if (isVerified) {
                        logUserRegistrationHistory(user.uid, user.email ?: "Unknown email")
                    }
                } catch (e: Exception) {
                    _isEmailVerified.value = false
                }
            }
        }
    }

    fun resendVerificationEmail(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    user.sendEmailVerification().await()
                    onSuccess()
                } catch (e: Exception) {
                    onFailure(e.localizedMessage ?: "An error occurred")
                }
            }
        } else {
            onFailure("User is not logged in")
        }
    }


    private fun logUserRegistrationHistory(userId: String, email: String) {
        viewModelScope.launch {
            try {
                val history = mapOf(
                    "userId" to userId,
                    "timestamp" to System.currentTimeMillis(),
                    "eventType" to "Registration",
                    "message" to "User with email ($email) registered successfully."
                )
                val ref = FirebaseDatabase.getInstance().getReference("History/userHistory").push()
                ref.setValue(history).await()
            } catch (e: Exception) {
            }
        }
    }
}
