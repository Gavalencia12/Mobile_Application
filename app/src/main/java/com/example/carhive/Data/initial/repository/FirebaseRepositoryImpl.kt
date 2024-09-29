package com.example.carhive.Data.initial.repository

import android.net.Uri
import com.example.carhive.Data.initial.exception.RepositoryException
import com.example.carhive.Data.initial.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseRepositoryImpl(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
) : AuthRepository {

    override suspend fun registerUser(email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: ""
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error registering user: ${e.message}", e))
        }
    }

    override suspend fun uploadProfileImage(userId: String, uri: Uri): Result<String> {
        return try {
            val storageRef = storage.getReference("Users/$userId/profile.jpg")
            val taskSnapshot = storageRef.putFile(uri).await()
            // Use `downloadUrl` method for Firebase Storage, ensure it is up to date
            val downloadUrl = taskSnapshot.storage.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error uploading profile image: ${e.message}", e))
        }
    }

    override suspend fun saveUserToDatabase(userId: String, user: UserModel): Result<Unit> {
        return try {
            database.getReference("Users").child(userId).setValue(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error saving user to database: ${e.message}", e))
        }
    }

    override suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error logging in user: ${e.message}", e))
        }
    }

}
