package com.example.carhive.presentation.seller.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.data.model.UserEntity
import com.example.carhive.Domain.usecase.auth.GetCurrentUserIdUseCase
import com.example.carhive.Domain.usecase.database.GetUserDataUseCase
import com.example.carhive.Domain.usecase.database.UpdateTermsSellerUseCase
import com.example.carhive.Domain.usecase.database.UpdateUserRoleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSellerViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val updateUserRoleUseCase: UpdateUserRoleUseCase,
    private val updateTermsSellerUseCase: UpdateTermsSellerUseCase
) : ViewModel() {
    private val _userData = MutableLiveData<Result<List<UserEntity>>>()
    val userData: LiveData<Result<List<UserEntity>>> get() = _userData

    // Método para obtener los datos del usuario
    fun fetchUserData() {
        viewModelScope.launch {
            val userIdResult = getCurrentUserIdUseCase() // Obtén el ID del usuario
            val userId = userIdResult.getOrNull()
            Log.i("angel", "es $userId")
            userIdResult.onSuccess {
                if (userId != null) {
                    val result =
                        getUserDataUseCase(userId) // Usa el ID para obtener los datos del usuario
                    _userData.value = result
                    Log.i("angel", "datos $result")
                } else {
                    _userData.value =
                        Result.failure(Exception("No se pudo obtener el ID del usuario"))
                }
            }.onFailure {
                _userData.value = Result.failure(it)
            }
        }
    }

    fun saveTermsSeller(){
        viewModelScope.launch {
            val userIdResult = getCurrentUserIdUseCase()
            val userId = userIdResult.getOrNull()
            userIdResult.onSuccess {
                if(userId != null){
                    val termsSeller = true
                    val result = updateTermsSellerUseCase(userId, termsSeller)
                    result.onSuccess {
                        Log.i("angel", "Se cambio el rol de manera correcta")
                    }
                }
            }.onFailure {
                Log.i("angel", "No encontro el id")
            }
        }
    }

    fun saveRolSeller(){
        viewModelScope.launch {
            val userIdResult = getCurrentUserIdUseCase()
            val userId = userIdResult.getOrNull()
            userIdResult.onSuccess {
                if(userId != null){
                    val newRole = 1
                    val result = updateUserRoleUseCase(userId, newRole)
                    result.onSuccess {
                        Log.i("angel", "Se cambio el rol de manera correcta")
                    }
                }
            }.onFailure {
                Log.i("angel", "No encontro el id")
            }
        }
    }
}