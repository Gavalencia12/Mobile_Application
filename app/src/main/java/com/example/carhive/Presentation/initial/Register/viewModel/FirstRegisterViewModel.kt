package com.example.carhive.Presentation.initial.Register.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Data.initial.model.UserModel
import com.example.carhive.Domain.initial.usecase.RegisterUseCase
import com.example.carhive.Domain.initial.usecase.SaveUserToDatabaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirstRegisterViewModel @Inject constructor(
    private val saveUserToDatabaseUseCase: SaveUserToDatabaseUseCase,
    private val RegisterUseCase : RegisterUseCase,
    private val SharedRegisterViewModel : SharedRegisterViewModel
) : ViewModel() {

    var uiState: FirstRegisterUiState = FirstRegisterUiState()

    fun saveFirstPartOfUserData(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val user = UserModel(
                firstName = firstName,
                lastName = lastName,
                email = email,
            )
            try {
                val registrationResult = RegisterUseCase(email, password)
                val userID = registrationResult.getOrNull()?:return@launch
                SharedRegisterViewModel.setUserId(userID)
                val result = saveUserToDatabaseUseCase(userID, user)
                result.onSuccess {
                    uiState = uiState.copy(isSuccess = true)
                }.onFailure {
                    uiState = uiState.copy(errorMessage = it.localizedMessage ?: "Error")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.localizedMessage ?: "Error")
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }
}

data class FirstRegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)