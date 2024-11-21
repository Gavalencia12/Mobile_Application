package com.example.carhive.presentation.admin.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.carhive.data.model.UserEntity
import com.example.carhive.Domain.model.UserRole


@HiltViewModel
class AdminUserListViewModel @Inject constructor(
    private val database: FirebaseDatabase
) : ViewModel() {

    private val _users = MutableLiveData<List<UserEntity>>()
    val users: LiveData<List<UserEntity>> = _users

    init {
        getUsers()
    }

    fun getUsers() {
        val usersRef = database.getReference("Users")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userList = mutableListOf<UserEntity>()
                for (snapshot in dataSnapshot.children) {
                    val userEntity = snapshot.getValue(UserEntity::class.java)

                    if (userEntity != null) {
                        val userRole = when (userEntity.role) {
                            0 -> UserRole.ADMIN
                            1 -> UserRole.ADVANCED_USER
                            2 -> UserRole.NORMAL_USER
                            else -> UserRole.NORMAL_USER
                        }

                        userEntity.id = snapshot.key ?: ""
                        userList.add(userEntity)
                    }
                }
                _users.value = userList
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", databaseError.message)
            }
        })
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