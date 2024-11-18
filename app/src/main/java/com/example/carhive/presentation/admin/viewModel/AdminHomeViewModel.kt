package com.example.carhive.presentation.admin.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

@HiltViewModel
class AdminHomeViewModel @Inject constructor(
    private val database: FirebaseDatabase
) : ViewModel() {

    fun getUserInfo(onComplete: (String, String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef: DatabaseReference = database.getReference("Users/$userId")
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val firstName = snapshot.child("firstName").getValue(String::class.java) ?: "Usuario"
                        val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                        onComplete(firstName, lastName)
                    } else {
                        onComplete("Usuario", "")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete("Usuario", "")
                }
            })
        } else {
            onComplete("Usuario", "")
        }
    }

    fun onLogicClick() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
            Log.i("angel", "Log out")
        }
    }
}