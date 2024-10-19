package com.example.carhive.Domain.usecase.database

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UpdateCarSoldStatusUseCase @Inject constructor() {

    // Reference to the Firebase database at the "Car/" path
    private val database = FirebaseDatabase.getInstance().getReference("Car/")

    // Suspended function that updates the "sold" status for a specific car under a user
    suspend operator fun invoke(userId: String, carId: String, sold: Boolean): Result<Unit> {
        return try {
            // Update only the "sold" field in the correct Firebase path
            database.child(userId).child(carId).child("sold").setValue(sold).await()

            // Return success result
            Result.success(Unit)
        } catch (e: Exception) {
            // Return failure result if an error occurs
            Result.failure(e)
        }
    }
}
