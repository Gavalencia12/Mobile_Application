package com.example.carhive.Domain.usecase.database

import android.net.Uri
import com.example.carhive.data.repository.AuthRepository
import javax.inject.Inject

class UploadToCarImageUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(userId:String, carId:String , uris:List<Uri>):Result<List<String>>{
        return repository.uploadCardImage(userId, carId, uris)
    }
}