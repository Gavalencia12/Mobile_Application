package com.example.carhive.Presentation.user.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(

) : ViewModel() {
    fun onLogicClick(){
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
            Log.i("angel", "Log out")
        }
    }
}