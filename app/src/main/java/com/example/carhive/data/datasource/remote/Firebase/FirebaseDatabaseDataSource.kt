package com.example.carhive.data.datasource.remote.Firebase

import android.util.Log
import com.example.carhive.data.exception.RepositoryException
import com.example.carhive.data.model.CarEntity
import com.example.carhive.data.model.FavoriteCar
import com.example.carhive.data.model.FavoriteUser
import com.example.carhive.data.model.UserEntity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseDatabaseDataSource @Inject constructor(
    private val database: FirebaseDatabase // Instancia de Firebase Realtime Database
) {

    /**
     * Saves a user to the database.
     *
     * @param userId The unique ID of the user.
     * @param user The UserEntity object containing the user's details.
     * @return A Result object indicating success or failure.
     */
    suspend fun saveUserToDatabase(userId: String, user: UserEntity): Result<Unit> {
        return try {
            database.getReference("Users")         // Reference to the main "Users" node
                .child(userId)                     // Create or access the specific user node
                .setValue(user)                    // Set the user data
                .await()                           // Wait for the operation to complete
            Result.success(Unit)                   // Return success if the operation completed successfully
        } catch (e: Exception) {
            // Catch any exceptions and return a failure result with an error message
            Result.failure(RepositoryException("Error saving user to database: ${e.message}", e))
        }
    }

    /**
     * Retrieves all cars from the database.
     *
     * @return A Result object containing a list of CarEntity objects or an error if the operation fails.
     */
    suspend fun getAllCarsFromDatabase(): Result<List<CarEntity>> {
        return try {
            // Obtain the reference to the "Car" node in the database
            val carSnapshot = database.getReference("Car")
                .get()
                .await()

            // Check if the snapshot exists and contains data
            if (carSnapshot.exists()) {
                // Convert the snapshot to a list of CarEntity objects
                val carList = carSnapshot.children.flatMap { userSnapshot ->
                    // Map each car within each user's node
                    userSnapshot.children.mapNotNull { carSnapshot ->
                        carSnapshot.getValue(CarEntity::class.java)
                    }
                }
                Result.success(carList)            // Return the list of CarEntity objects
            } else {
                Result.success(emptyList())        // Return an empty list if no cars are found
            }
        } catch (e: Exception) {
            // Catch any exceptions and return a failure result with an error message
            Result.failure(RepositoryException("Error fetching cars from database", e))
        }
    }


    suspend fun saveCarToDatabase(userId: String, car: CarEntity): Result<String> {
        return try {
            // Crea una referencia para el nuevo coche
            val carRef = database.getReference("Car")
                .child(userId)
                .push()

            // Guarda el coche en la base de datos con el ID generado
            carRef.setValue(car).await()

            // Obtiene el ID del coche recién creado
            val carId = carRef.key ?: throw Exception("Error generating car ID")

            // Llama a la función para crear el grupo de chat asociado a la publicación (carId)
            createChatGroupForCar(carId, userId, car.modelo)

            // Devuelve el ID del coche
            Result.success(carId)
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error saving car to database: ${e.message}", e))
        }
    }

    private suspend fun createChatGroupForCar(carId: String, ownerId: String, carModel: String) {
        val chatGroupRef = database.getReference("ChatGroups").child(ownerId).child(carId)

        // Datos iniciales para el grupo de chat de la publicación
        val chatGroupData = mapOf(
            "carId" to carId,
            "ownerId" to ownerId,
            "carModel" to carModel,
            "createdAt" to System.currentTimeMillis(),
            "messages" to emptyMap<String, Any>() // Inicializa sin mensajes
        )

        try {
            // Guarda el grupo de chat en la base de datos
            chatGroupRef.setValue(chatGroupData).await()
            Log.d("saveCarToDatabase", "Chat group created successfully for carId: $carId")
        } catch (e: Exception) {
            Log.e("saveCarToDatabase", "Failed to create chat group: ${e.message}")
            throw Exception("Error creating chat group for car: ${e.message}", e)
        }
    }


    suspend fun updateCarInDatabase(userId: String, carId: String, car: CarEntity): Result<Unit> {
        return try {
            database.getReference("Car")
                .child(userId)
                .child(carId) // Usar el ID del coche existente
                .setValue(car)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error updating car in database: ${e.message}", e))
        }
    }


    suspend fun deleteCarInDatabase(userId: String, carId: String): Result<Unit>{
        return try {
            database.getReference("Car")
                .child(userId)
                .child(carId)
                .removeValue()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error deleting car from database: ${e.message}", e))
        }
    }

    suspend fun getCarUserFromDatabase(userId: String): Result<List<CarEntity>> {
        return try {
            // Obtén la referencia del nodo del coche
            val carSnapshot = database.getReference("Car")
                .child(userId)
                .get()
                .await()

            // Verifica si el snapshot existe y tiene datos
            if (carSnapshot.exists()) {
                // Convierte el snapshot a una lista de CarEntity
                val carList = carSnapshot.children.mapNotNull { childSnapshot ->
                    childSnapshot.getValue(CarEntity::class.java) // Mapea cada hijo a un CarEntity
                }
                Result.success(carList) // Retorna la lista de CarEntity
            } else {
                Result.success(emptyList()) // Retorna una lista vacía si no hay coches
            }
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error fetching car from database", e))
        }
    }


    /**
     * Recupera el rol de un usuario específico desde la base de datos.
     *
     * Este método se utiliza para obtener el rol asignado a un usuario en la base de datos.
     * El rol se almacena como un valor entero en la estructura de datos de Firebase.
     *
     * @param userId Identificador único del usuario cuyo rol se desea recuperar.
     * @return Result<Int?> Éxito con el rol del usuario si existe,
     *                      null si el usuario no tiene rol asignado,
     *                      o failure con Exception si ocurre un error durante la recuperación.
     *
     * La operación se lleva a cabo en los siguientes pasos:
     * 1. Obtiene la referencia específica al campo 'role' del usuario dentro de la
     *    estructura de datos de Firebase.
     * 2. Recupera el valor asociado a ese campo como un entero.
     * 3. Retorna el resultado envuelto en un objeto Result, que indica éxito o fracaso.
     */
    suspend fun getUserRole(userId: String): Result<Int?> {
        return try {
            val databaseRef = FirebaseDatabase.getInstance()
                .getReference("Users/$userId/role") // Referencia directa al campo role
            val dataSnapshot = databaseRef.get().await() // Obtiene el snapshot de datos
            val userRole = dataSnapshot.getValue(Int::class.java) // Obtiene el rol como Int
            Result.success(userRole) // Retorna el rol del usuario
        } catch (e: Exception) {
            // Captura cualquier excepción y devuelve un resultado de error
            Result.failure(e)
        }
    }

    suspend fun getUserData(userId: String): Result<List<UserEntity>> {
        return try {
            val userSnapshot = database.getReference("Users")
                .child(userId)
                .get()
                .await()

            if (userSnapshot.exists()) {
                // Obtener el objeto UserEntity directamente
                val user = userSnapshot.getValue(UserEntity::class.java)
                if (user != null) {
                    Result.success(listOf(user)) // Retornar el objeto en una lista
                } else {
                    Result.success(emptyList()) // Si no hay datos
                }
            } else {
                Result.success(emptyList()) // Si el snapshot no existe
            }
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error fetching user from database", e))
        }
    }

    suspend fun updateUserRole(userId: String, newRole: Int): Result<Unit> {
        return try {
            // Obtener la referencia a la base de datos para el usuario específico
            val userRef = database.getReference("Users")
                .child(userId)

            // Verificar si el usuario existe en la base de datos
            val userSnapshot = userRef.get().await()

            if (userSnapshot.exists()) {
                // Actualizar el campo de "role" del usuario en la base de datos
                userRef.child("role").setValue(newRole).await()

                // Si la actualización fue exitosa, retornamos un resultado de éxito
                Result.success(Unit)
            } else {
                // Si el usuario no existe, retornamos un fallo con un mensaje adecuado
                Result.failure(RepositoryException("User with ID $userId not found"))
            }
        } catch (e: Exception) {
            // En caso de error durante la operación, retornamos un fallo con el mensaje de excepción
            Result.failure(RepositoryException("Error updating user role in database", e))
        }
    }

    suspend fun updateTermsSeller(userId: String, termsSeller: Boolean): Result<Unit> {
        return try {
            // Obtener la referencia a la base de datos para el usuario específico
            val userRef = database.getReference("Users")
                .child(userId)

            // Verificar si el usuario existe en la base de datos
            val userSnapshot = userRef.get().await()

            if (userSnapshot.exists()) {
                // Actualizar el campo de "role" del usuario en la base de datos
                userRef.child("termsSeller").setValue(termsSeller).await()

                // Si la actualización fue exitosa, retornamos un resultado de éxito
                Result.success(Unit)
            } else {
                // Si el usuario no existe, retornamos un fallo con un mensaje adecuado
                Result.failure(RepositoryException("User with ID $userId not found"))
            }
        } catch (e: Exception) {
            // En caso de error durante la operación, retornamos un fallo con el mensaje de excepción
            Result.failure(RepositoryException("Error updating user role in database", e))
        }
    }

    suspend fun addCarToFavorites(
        userId: String,
        userName: String,
        carId: String,
        carModel: String,
        carOwner: String
    ): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()

            // Referencia al nodo "UserFavorites"
            val userFavoritesRef = database.getReference("Favorites/UserFavorites")
                .child(userId)
                .child(carId)

            // Verificar si el coche ya está en favoritos
            val isAlreadyFavorite = userFavoritesRef.get().await().exists()
            if (isAlreadyFavorite) {
                return Result.failure(RepositoryException("Car is already marked as favorite"))
            }

            // Agregar a "UserFavorites" con el userId como clave
            userFavoritesRef.setValue(
                mapOf(
                    "addedAt" to timestamp,
                    "carModel" to carModel,
                    "carOwner" to carOwner
                )
            ).await()

            // Agregar a "CarFavorites" con el userId como clave y guardar el userName como un valor
            database.getReference("Favorites/CarFavorites")
                .child(carId)
                .child("users")
                .child(userId) // Asegúrate de que userId es la clave aquí
                .setValue(
                    mapOf(
                        "userName" to userName, // Guardar el nombre completo como valor, no como clave
                        "addedAt" to timestamp
                    )
                ).await()

            // Incrementar el contador de favoritos
            val favoriteCountRef = database.getReference("Favorites/CarFavorites")
                .child(carId)
                .child("favoriteCount")

            val favoriteCount = favoriteCountRef.get().await().getValue(Int::class.java) ?: 0
            favoriteCountRef.setValue(favoriteCount + 1).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error adding car to favorites: ${e.message}", e))
        }
    }

    suspend fun getUserFavorites(userId: String): Result<List<FavoriteCar>> {
        return try {
            val favoritesSnapshot = database.getReference("Favorites/UserFavorites")
                .child(userId)
                .get()
                .await()

            if (favoritesSnapshot.exists()) {
                val favoritesList = favoritesSnapshot.children.mapNotNull { it.getValue(FavoriteCar::class.java) }
                Result.success(favoritesList)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error fetching user favorites: ${e.message}", e))
        }
    }

    suspend fun getCarFavoriteCountAndUsers(carId: String): Result<Pair<Int, List<FavoriteUser>>> {
        return try {
            val carFavoritesSnapshot = database.getReference("Favorites/CarFavorites")
                .child(carId)
                .get()
                .await()

            if (carFavoritesSnapshot.exists()) {
                val favoriteCount = carFavoritesSnapshot.child("favoriteCount").getValue(Int::class.java) ?: 0
                val users = carFavoritesSnapshot.child("users").children.mapNotNull { it.getValue(FavoriteUser::class.java) }
                Result.success(favoriteCount to users)
            } else {
                Result.success(0 to emptyList())
            }
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error fetching car favorites: ${e.message}", e))
        }
    }

    suspend fun removeCarFromFavorites(userId: String, carId: String): Result<Unit> {
        return try {
            // Referencia al nodo "UserFavorites"
            val userFavoritesRef = database.getReference("Favorites/UserFavorites")
                .child(userId)
                .child(carId)

            // Verificar si el coche está marcado como favorito
            val isFavorite = userFavoritesRef.get().await().exists()
            if (!isFavorite) {
                return Result.failure(RepositoryException("Car is not marked as favorite"))
            }

            // Eliminar el coche de "UserFavorites"
            userFavoritesRef.removeValue().await()

            // Eliminar el usuario de "CarFavorites"
            val carFavoritesRef = database.getReference("Favorites/CarFavorites")
                .child(carId)
                .child("users")
                .child(userId)
            carFavoritesRef.removeValue().await()

            // Decrementar el contador de favoritos
            val favoriteCountRef = database.getReference("Favorites/CarFavorites")
                .child(carId)
                .child("favoriteCount")
            val favoriteCount = favoriteCountRef.get().await().getValue(Int::class.java) ?: 0
            if (favoriteCount > 0) {
                favoriteCountRef.setValue(favoriteCount - 1).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error removing car from favorites: ${e.message}", e))
        }
    }

    suspend fun getUserFavoriteCars(userId: String): Result<List<CarEntity>> {
        return try {
            // Obtener los coches favoritos del usuario desde el nodo "Favorites/UserFavorites"
            val userFavoritesSnapshot = database.getReference("Favorites/UserFavorites")
                .child(userId)
                .get()
                .await()

            if (userFavoritesSnapshot.exists()) {
                // Obtener los IDs de los coches favoritos
                val carIds = userFavoritesSnapshot.children.mapNotNull { it.key }

                // Obtener los detalles completos de los coches favoritos
                val favoriteCars = mutableListOf<CarEntity>()
                for (carId in carIds) {
                    // Consulta a la tabla "Cars" usando el carId para obtener el coche completo
                    val carSnapshot = database.getReference("Cars")
                        .child(carId)
                        .get()
                        .await()

                    carSnapshot.getValue(CarEntity::class.java)?.let { favoriteCars.add(it) }
                }
                Result.success(favoriteCars)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error fetching user favorite cars: ${e.message}", e))
        }
    }

    suspend fun getFavoriteReactionsForUserCars(userId: String): Result<Int> {
        return try {
            // Obtén la referencia a los carros del usuario
            val carSnapshot = database.getReference("Car")
                .child(userId)
                .get()
                .await()

            // Verifica si el snapshot tiene datos
            if (carSnapshot.exists()) {
                // Itera sobre los carros del usuario
                val carsWithFavoritesCount = carSnapshot.children.count { car ->
                    val carId = car.key // Obtener el ID del carro
                    val carFavoritesSnapshot = database.getReference("Favorites/CarFavorites")
                        .child(carId!!)
                        .get()
                        .await()

                    // Verificar si el carro tiene al menos un favorito (favoriteCount > 0)
                    val favoriteCount = carFavoritesSnapshot.child("favoriteCount").getValue(Int::class.java) ?: 0
                    favoriteCount > 0
                }

                Result.success(carsWithFavoritesCount) // Retornar el número de carros con al menos un favorito
            } else {
                Result.success(0) // Si no hay carros, retornar 0
            }
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error fetching favorite reactions count for user cars: ${e.message}", e))
        }
    }

}

