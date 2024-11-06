package com.example.carhive.data.repository

interface SessionRepository {

    suspend fun isUserAuthenticated(): Result<String?>
    suspend fun saveUserRole(userRole: Int): Result<Unit>
    suspend fun getUserRole(userId: String): Result<Int?>
}
