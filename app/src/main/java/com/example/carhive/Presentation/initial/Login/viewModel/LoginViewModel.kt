package com.example.carhive.Presentation.initial.Login.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.usecase.auth.LoginUseCase
import com.example.carhive.Domain.usecase.session.GetUserRoleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val getUserRoleUseCase: GetUserRoleUseCase
) : ViewModel() {
    private val _userRole = MutableStateFlow<Int?>(null)
    val userRole: StateFlow<Int?> get() = _userRole

    private val _isLogin = MutableStateFlow(false)
    val isLogin: StateFlow<Boolean> get() = _isLogin

    // Función para manejar el clic en login y decidir a dónde navegar
    fun onLoginClick(email: String, password: String, navigateBasedOnRole: (String) -> Unit) {
        viewModelScope.launch {
            val loginResult = loginUseCase(email, password)

            _isLogin.value = loginResult.isSuccess && loginResult.getOrNull() != null
            Log.d("LoginViewModel", "Login Result: $loginResult")

            if (_isLogin.value) {
                val userId = loginResult.getOrNull()
                Log.d("LoginViewModel", "User ID: $userId")
                userId?.let {
                    val roleResult = getUserRoleUseCase(it)
                    _userRole.value = roleResult.getOrNull()
                    Log.d("LoginViewModel", "User Role: ${_userRole.value}")

                    // Lógica de navegación basada en el rol
                    when (_userRole.value) {
                        0 -> navigateBasedOnRole("Admin")
                        1 -> navigateBasedOnRole("Seller")
                        2 -> navigateBasedOnRole("User")
                        else -> navigateBasedOnRole("Login")
                    }
                }
            }
        }
    }

}
