package com.example.carhive.Data.initial.repository

import android.net.Uri
import com.example.carhive.Data.initial.model.UserModel

interface AuthRepository {
    suspend fun registerUser(email: String, password: String): Result<String>
    suspend fun uploadProfileImage(userId: String, uri: Uri): Result<String>
    suspend fun saveUserToDatabase(userId: String, user: UserModel): Result<Unit>
    suspend fun loginUser(email: String, password: String): Result<Unit>
}