package com.example.carhive.presentation.user.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.carhive.data.model.AdditionalDataEntity
import com.example.carhive.data.model.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdditionalDataViewModel : ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _combinedData = MutableLiveData<Pair<AdditionalDataEntity, UserEntity>?>()
    val combinedData: LiveData<Pair<AdditionalDataEntity, UserEntity>?> = _combinedData


    fun loadUserData() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            loadAdditionalData(userId)
        } else {
            _combinedData.value = null
        }
    }

    private fun loadAdditionalData(userId: String) {
        val additionalDataRef = databaseReference.child("additionalData").child(userId)
        additionalDataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val additionalData = snapshot.getValue(AdditionalDataEntity::class.java)
                if (additionalData != null) {
                    loadUserDetails(userId, additionalData)
                } else {
                    _combinedData.value = null
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _combinedData.value = null
            }
        })
    }

    private fun loadUserDetails(userId: String, additionalData: AdditionalDataEntity) {
        val userRef = databaseReference.child("Users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserEntity::class.java)
                if (user != null) {
                    _combinedData.value = Pair(additionalData, user)
                } else {
                    _combinedData.value = null
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _combinedData.value = null
            }
        })
    }


}
