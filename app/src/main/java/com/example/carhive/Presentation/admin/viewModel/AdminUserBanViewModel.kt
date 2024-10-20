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

    fun banUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.child(userId).child("isBanned").setValue(true).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unbanUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.child(userId).child("isBanned").setValue(false).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.child(userId).removeValue().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun filterUsers(query: String): List<UserEntity> {
        val lowercaseQuery = query.lowercase()
        return _users.value?.filter { user ->
            user.firstName.lowercase().contains(lowercaseQuery) ||
                    user.lastName.lowercase().contains(lowercaseQuery) ||
                    user.email.lowercase().contains(lowercaseQuery)
        } ?: emptyList()
    }
}
