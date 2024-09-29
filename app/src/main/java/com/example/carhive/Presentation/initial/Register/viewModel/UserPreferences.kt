package com.example.carhive.Data.initial.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.carhive.Data.initial.model.UserModel

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    fun saveUser(user: UserModel) {
        with(sharedPreferences.edit()) {
            putString("firstName", user.firstName)
            putString("lastName", user.lastName)
            putString("email", user.email)
            putString("phoneNumber", user.phoneNumber)
            putString("voterID", user.voterID)
            putString("curp", user.curp)
            putString("imageUrl", user.imageUrl)
            apply()
        }
    }

    fun getUser(): UserModel {
        return UserModel(
            firstName = sharedPreferences.getString("firstName", "") ?: "",
            lastName = sharedPreferences.getString("lastName", "") ?: "",
            email = sharedPreferences.getString("email", "") ?: "",
            phoneNumber = sharedPreferences.getString("phoneNumber", "") ?: "",
            voterID = sharedPreferences.getString("voterID", "") ?: "",
            curp = sharedPreferences.getString("curp", "") ?: "",
            imageUrl = sharedPreferences.getString("imageUrl", null)
        )
    }

    fun clear() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }

    fun savePassword(password: String) {
        with(sharedPreferences.edit()) {
            putString("password", password)
            apply()
        }
    }

    fun getPassword(): String? {
        return sharedPreferences.getString("password", null)
    }
}
