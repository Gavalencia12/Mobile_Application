package com.example.carhive.Domain.usecase.database

import android.net.Uri
import com.example.carhive.data.repository.AuthRepository

class UploadToProfileImageUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userID: String, imageUri: Uri): Result<String> {
        return repository.uploadProfileImage(userID, imageUri)
    }
}