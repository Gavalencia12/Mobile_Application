package com.example.carhive.Presentation.initial.Login.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Domain.initial.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    var uiState by mutableStateOf(LoginUiState())

    fun onLogicClick(email:String, password:String, navigateToUser:()->Unit){
        viewModelScope.launch {
            val result = loginUseCase(email, password)
            result.onSuccess {
                navigateToUser()
            }.onFailure {
                uiState = uiState.copy(errorMessage = it.localizedMessage.toString())
            }
        }
    }
    
}

data class LoginUiState(
    val errorMessage: String = ""
)
