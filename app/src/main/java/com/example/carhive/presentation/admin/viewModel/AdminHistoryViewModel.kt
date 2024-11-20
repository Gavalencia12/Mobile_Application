package com.example.carhive.presentation.admin.viewModel

import androidx.lifecycle.ViewModel
import com.example.carhive.data.model.HistoryEntity
import com.example.carhive.data.model.UserEntity
import com.google.firebase.database.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminHistoryViewModel @Inject constructor(
    private val database: FirebaseDatabase
) : ViewModel() {
    val userMap = mutableMapOf<String, UserEntity?>()

    fun getHistoryData(onComplete: (List<HistoryEntity>) -> Unit) {
        val historyRef: DatabaseReference = database.getReference("History")
        historyRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val historyList = mutableListOf<HistoryEntity>()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        child.children.mapNotNullTo(historyList) {
                            it.getValue(HistoryEntity::class.java)
                        }
                    }
                }

                // Ordenar la lista por timestamp en orden descendente (más reciente primero)
                historyList.sortByDescending { it.timestamp }

                onComplete(historyList)
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(emptyList())
            }
        })
    }

    fun getUserData(userId: String, onComplete: (UserEntity?) -> Unit) {
        val userRef = database.getReference("Users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserEntity::class.java)
                onComplete(user)
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(null)
            }
        })
    }

    fun filterHistory(historyList: List<HistoryEntity>, query: String): List<HistoryEntity> {
        return if (query.isEmpty()) {
            historyList
        } else {
            historyList.filter { history ->
                val user = userMap[history.userId]
                val matchesMessage = history.message?.contains(query, ignoreCase = true) == true
                val matchesEventType = history.eventType?.contains(query, ignoreCase = true) == true
                val matchesUserName = user?.firstName?.contains(query, ignoreCase = true) == true ||
                        user?.lastName?.contains(query, ignoreCase = true) == true

                matchesMessage || matchesEventType || matchesUserName
            }
        }
    }

}
