package com.example.carhive.Presentation.admin.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.Data.model.UserEntity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminUserBanViewModel : ViewModel() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
    private val _users = MutableLiveData<List<UserEntity>>()
    val users: LiveData<List<UserEntity>> = _users
    // Método para banear a un usuario
    fun banUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.child(userId).child("isBanned").setValue(true).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Método para desbanear a un usuario
    fun unbanUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.child(userId).child("isBanned").setValue(false).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Método para eliminar a un usuario
    fun deleteUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.child(userId).removeValue().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
