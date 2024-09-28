package com.example.carhive.Data.initial.session

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class UserSessionManager @Inject constructor(private val context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)

    fun setUserId(userId: String) {
        preferences.edit().putString("userId", userId).apply()
    }

    fun getUserId(): String? {
        return preferences.getString("userId", null)
    }

    fun clearSession() {
        preferences.edit().clear().apply()
    }
}
