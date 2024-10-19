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
    private val getUserRoleUseCase: GetUserRoleUseCase,
) : ViewModel() {
    private val _userRole = MutableStateFlow<Int?>(null)
    val userRole: StateFlow<Int?> get() = _userRole

    private val _isLogin = MutableStateFlow(false)
    val isLogin: StateFlow<Boolean> get() = _isLogin

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> get() = _loginError

    // Función para manejar el clic en login y decidir a dónde navegar
    fun onLoginClick(email: String, password: String, navigateBasedOnRole: (String) -> Unit, navigateToVerifyEmail: () -> Unit) {
        viewModelScope.launch {
            val loginResult = loginUseCase(email, password)

            if (loginResult.isSuccess) {
                val userId = loginResult.getOrNull()
                _isLogin.value = true

                userId?.let {
                    val roleResult = getUserRoleUseCase(it)
                    _userRole.value = roleResult.getOrNull()

                    // Lógica de navegación basada en el rol
                    when (_userRole.value) {
                        0 -> navigateBasedOnRole("Admin")
                        1 -> navigateBasedOnRole("Seller")
                        2 -> navigateBasedOnRole("User")
                        else -> navigateBasedOnRole("Login")
                    }
                }
            } else {
                // Si falla, revisamos el tipo de error
                val exception = loginResult.exceptionOrNull()
                if (exception?.message == "Email not verified.") {
                    // Navegar a una pantalla para verificar el email
                    navigateToVerifyEmail()
                } else {
                    // Mostrar mensaje de error de credenciales incorrectas
                    _loginError.value = "Incorrect credentials."
                }
            }
        }
    }
}
