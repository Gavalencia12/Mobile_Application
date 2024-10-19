/**
 * Fuente de datos que maneja las operaciones con Firebase Realtime Database.
 *
 * Esta clase proporciona métodos para interactuar con Firebase Realtime Database, permitiendo
 * realizar las siguientes operaciones:
 * - Almacenar información de usuarios en la base de datos.
 * - Recuperar datos específicos de usuarios.
 * - Manejar la estructura de datos de usuarios en Firebase.
 *
 * La estructura de datos en Firebase sigue el siguiente patrón:
 * ```
 * /Users
 *    ├── userId1
 *    │   ├── firstName
 *    │   ├── lastName
 *    │   ├── email
 *    │   ├── role
 *    │   └── ...
 *    └── userId2
 *        └── ...
 * ```
 */
package com.example.carhive.Data.datasource.remote.Firebase

import com.example.carhive.Data.exception.RepositoryException
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Data.model.UserEntity
import com.example.carhive.Domain.model.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseDatabaseDataSource @Inject constructor(
    private val database: FirebaseDatabase // Instancia de Firebase Realtime Database
) {

    suspend fun saveUserToDatabase(userId: String, user: UserEntity): Result<Unit> {
        return try {
            database.getReference("Users") // Referencia al nodo principal de usuarios
                .child(userId)             // Crea/accede al nodo específico del usuario
                .setValue(user)            // Establece los datos del usuario
                .await()                   // Espera a que la operación se complete
            Result.success(Unit) // Retorna éxito si la operación se realizó correctamente
        } catch (e: Exception) {
            // Captura cualquier excepción y devuelve un resultado de error
            Result.failure(RepositoryException("Error saving user to database: ${e.message}", e))
        }
    }

    suspend fun getAllCarsFromDatabase(): Result<List<CarEntity>> {
        return try {
            // Obtén la referencia del nodo de todos los coches
            val carSnapshot = database.getReference("Car")
                .get()
                .await()

            // Verifica si el snapshot existe y tiene datos
            if (carSnapshot.exists()) {
                // Convierte el snapshot a una lista de CarEntity
                val carList = carSnapshot.children.flatMap { userSnapshot ->
                    // Mapea cada coche dentro del nodo de cada usuario
                    userSnapshot.children.mapNotNull { carSnapshot ->
                        carSnapshot.getValue(CarEntity::class.java)
                    }
                }
                Result.success(carList) // Retorna la lista de CarEntity
            } else {
                Result.success(emptyList()) // Retorna una lista vacía si no hay coches
            }
        } catch (e: Exception) {
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

            // Devuelve el ID del coche recién creado
            Result.success(carRef.key ?: throw Exception("Error generating car ID"))
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error saving car to database: ${e.message}", e))
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


}
