package com.example.carhive.presentation.admin.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carhive.data.model.MessageEntity
import com.example.carhive.data.model.UserEntity
import com.example.carhive.data.model.UserReport
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminReportsViewModel @Inject constructor(
    private val database: FirebaseDatabase
) : ViewModel() {

    private val _userReports = MutableLiveData<List<UserReport>>()
    val userReports: LiveData<List<UserReport>> = _userReports

    val userMap = mutableMapOf<String, UserEntity?>()

    fun fetchReports() {
        val reportsRef = database.getReference("Reports/UserReports")
        reportsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reportsList = snapshot.children.mapNotNull { reportSnapshot ->
                    try {
                        val reporterId = reportSnapshot.child("reporterId").getValue(String::class.java) ?: ""
                        val reportedUserId = reportSnapshot.child("reportedUserId").getValue(String::class.java) ?: ""
                        val carId = reportSnapshot.child("carId").getValue(String::class.java) ?: ""
                        val timestamp = reportSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                        val sampleMessages = try {
                            reportSnapshot.child("sampleMessages").children.mapNotNull {
                                it.getValue(MessageEntity::class.java) // Convierte cada nodo en MessageEntity
                            }
                        } catch (e: Exception) {
                            emptyList<MessageEntity>()
                        }
                        val comment = reportSnapshot.child("comment").getValue(String::class.java) ?: ""

                        UserReport(
                            userId = reporterId,
                            carId = carId,
                            comment = comment,
                            sampleMessages = sampleMessages, // Pasa la lista de MessageEntity
                            reportedUserId = reportedUserId,
                            timestamp = timestamp
                        )
                    } catch (e: Exception) {
                        Log.e("AdminReportsViewModel", "Error parsing report: ${e.message}")
                        null
                    }
                }

                val userIds = reportsList.flatMap { listOf(it.userId, it.reportedUserId) }.distinct()

                fetchUserDetails(userIds) {
                    _userReports.postValue(reportsList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AdminReportsViewModel", "Error fetching reports: ${error.message}")
            }
        })
    }

    private fun fetchUserDetails(userIds: List<String>, onComplete: () -> Unit) {
        val userRef = database.getReference("Users")
        userIds.forEach { userId ->
            if (!userMap.containsKey(userId)) {
                userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(UserEntity::class.java)
                        userMap[userId] = user
                        onComplete()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        userMap[userId] = null
                        onComplete()
                    }
                })
            }
        }
    }
}
