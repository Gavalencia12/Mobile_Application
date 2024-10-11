package com.example.carhive.Data.repository

import android.net.Uri
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Domain.model.Car
import com.example.carhive.Domain.model.User

/**
 * Interfaz que define las operaciones relacionadas con la autenticación y gestión de usuarios.
 *
 * Esta interfaz actúa como un contrato para la implementación de la autenticación en la aplicación,
 * incluyendo el registro, inicio de sesión y manejo de imágenes de perfil.
 *
 * Todas las funciones son suspending functions, lo que permite que se ejecuten de manera asíncrona
 * dentro de un coroutine, evitando bloqueos en el hilo principal.
 */
interface AuthRepository {

    suspend fun registerUser(email: String, password: String): Result<String>
    suspend fun uploadProfileImage(userId: String, uri: Uri): Result<String>
    suspend fun uploadCardImage(
        userId: String,
        carId: String,
        uris: List<Uri>
    ): Result<List<String>>

    suspend fun saveUserToDatabase(userId: String, user: User): Result<Unit>
    suspend fun saveCarToDatabase(userId: String, car: Car): Result<String>
    suspend fun updateCarToDatabase(
        userId: String,
        carId: String,
        car: Car
    ): Result<Unit>

    suspend fun deleteCarInDatabase(userId: String, carId: String): Result<Unit>
    suspend fun getCarUserFromDatabase(userId: String): Result<CarEntity?>
    suspend fun loginUser(email: String, password: String): Result<String?>
    suspend fun getCurrentUserId(): Result<String?>
    suspend fun getUserRole(userId: String): Result<Int?>
}
