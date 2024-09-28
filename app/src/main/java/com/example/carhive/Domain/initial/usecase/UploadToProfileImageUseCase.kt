package com.example.carhive.Domain.initial.usecase

import android.net.Uri
import com.example.carhive.Data.initial.repository.AuthRepository

class UploadToProfileImageUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userID: String, imageUri: Uri): Result<String> {
        return repository.uploadProfileImage(userID, imageUri)
    }
}