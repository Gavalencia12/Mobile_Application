package com.example.carhive.data.repository

import android.net.Uri
import com.example.carhive.data.model.CarEntity
import com.example.carhive.data.model.FavoriteCar
import com.example.carhive.data.model.FavoriteUser
import com.example.carhive.data.model.UserEntity
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
    suspend fun getAllCarsFromDatabase(): Result<List<CarEntity>>
    suspend fun getUserData(userId: String): Result<List<UserEntity>>
    suspend fun updateUserRole(userId: String, newRole: Int): Result<Unit>
    suspend fun updateTermsSeller(userId: String, termsSeller: Boolean): Result<Unit>
    suspend fun isVerifiedTheEmail(): Result<Unit>
    suspend fun saveCarToDatabase(userId: String, car: Car): Result<String>
    suspend fun updateCarToDatabase(
        userId: String,
        carId: String,
        car: Car
    ): Result<Unit>

    suspend fun deleteCarInDatabase(userId: String, carId: String): Result<Unit>
    suspend fun getCarUserFromDatabase(userId: String): Result<List<CarEntity>>
    suspend fun loginUser(email: String, password: String): Result<String?>
    suspend fun getCurrentUserId(): Result<String?>
    suspend fun getUserRole(userId: String): Result<Int?>
    suspend fun removeCarFromFavorites(userId: String, carId: String): Result<Unit>
    suspend fun addCarToFavorites(userId: String, userName: String, carId: String, carModel: String, carOwner: String): Result<Unit>
    suspend fun getUserFavorites(userId: String): Result<List<FavoriteCar>>
    suspend fun getCarFavoriteCountAndUsers(carId: String): Result<Pair<Int, List<FavoriteUser>>>
    suspend fun getUserFavoriteCars(userId: String): Result<List<CarEntity>>
    suspend fun getFavoriteReactionsForUserCars(userId: String): Result<Int>
}
