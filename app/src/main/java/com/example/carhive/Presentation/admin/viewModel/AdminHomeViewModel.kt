package com.example.carhive.Presentation.admin.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminHomeViewModel @Inject constructor(

) : ViewModel() {
    fun onLogicClick(){
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
            Log.i("angel", "Log out")
        }
    }
}