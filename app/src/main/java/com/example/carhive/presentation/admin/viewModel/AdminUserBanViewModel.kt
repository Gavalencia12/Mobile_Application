package com.example.carhive.presentation.admin.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.data.model.HistoryEntity
import com.example.carhive.data.model.UserEntity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminUserBanViewModel : ViewModel() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val _users = MutableLiveData<List<UserEntity>>()
    val users: LiveData<List<UserEntity>> = _users

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userList = mutableListOf<UserEntity>()
                val snapshot = database.child("Users").get().await()
                snapshot.children.forEach { userSnapshot ->
                    val user = userSnapshot.getValue(UserEntity::class.java)
                    user?.let {
                        it.id = userSnapshot.key ?: ""
                        userList.add(it)
                    }
                }
                _users.postValue(userList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun banUser(userId: String, userName: String, userEmail: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.child("Users").child(userId).child("banned").setValue(true).await()
                logHistory(userId, "Ban", "User $userName was banned")
                loadUsers() // Recarga los usuarios
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unbanUser(userId: String, userName: String, userEmail: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.child("Users").child(userId).child("banned").setValue(false).await()
                logHistory(userId, "Unban", "User $userName was unbanned")
                loadUsers() // Recarga los usuarios
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteUser(userId: String, userName: String, userEmail: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.child("Users").child(userId).removeValue().await()
                logHistory(userId, "Delete", "User $userName was deleted")
                loadUsers() // Recarga los usuarios
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun logHistory(userId: String, eventType: String, message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val historyEntry = HistoryEntity(
                    userId = userId,
                    timestamp = System.currentTimeMillis(),
                    eventType = eventType,
                    message = message
                )
                database.child("History/userHistory").push().setValue(historyEntry).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
