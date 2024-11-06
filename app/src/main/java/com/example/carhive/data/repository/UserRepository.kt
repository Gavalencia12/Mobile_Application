package com.example.carhive.data.repository

import com.example.carhive.Domain.model.User

interface UserRepository {

    suspend fun saveUser(user: User): Result<Unit>
    suspend fun getUser(): Result<User>
    suspend fun clear(): Result<Unit>
    suspend fun savePassword(password: String): Result<Unit>
    suspend fun getPassword(): Result<String?>
}
