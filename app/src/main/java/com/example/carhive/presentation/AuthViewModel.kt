package com.example.carhive.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.usecase.session.GetUserRoleUseCase
import com.example.carhive.Domain.usecase.session.IsUserAuthenticatedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val isUserAuthenticatedUseCase: IsUserAuthenticatedUseCase,
    private val getUserRoleUseCase: GetUserRoleUseCase
) : ViewModel() {

    private val _userRole = MutableStateFlow<Int?>(null)
    val userRole: StateFlow<Int?> get() = _userRole

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> get() = _isAuthenticated

    init {
        checkAuthentication()
    }

    // Función para verificar si el usuario está autenticado y obtener el rol
    private fun checkAuthentication() {
        viewModelScope.launch {
            val authResult = isUserAuthenticatedUseCase()

            _isAuthenticated.value = authResult.isSuccess && authResult.getOrNull() != null

            if (_isAuthenticated.value) {
                val userId = authResult.getOrNull()
                userId?.let {
                    val roleResult = getUserRoleUseCase(it)
                    _userRole.value = roleResult.getOrNull()
                }
            } else {
                _userRole.value = null
            }
        }
    }
}
