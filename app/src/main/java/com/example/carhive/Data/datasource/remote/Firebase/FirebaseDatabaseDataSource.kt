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
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseDatabaseDataSource @Inject constructor(
    private val database: FirebaseDatabase // Instancia de Firebase Realtime Database
) {

    /**
     * Guarda o actualiza la información de un usuario en la base de datos.
     *
     * Este método se utiliza para almacenar o modificar la información de un usuario en la
     * base de datos. Si el usuario ya existe, sus datos se sobrescriben.
     *
     * @param userId Identificador único del usuario en Firebase.
     * @param user Entidad que contiene los datos del usuario a guardar. Debe incluir campos
     *             como nombre, apellido, correo electrónico, rol, entre otros.
     * @return Result<Unit> Éxito si los datos se guardaron correctamente,
     *                      o failure con [RepositoryException] si ocurre un error durante
     *                      el proceso de almacenamiento.
     *
     * La operación se lleva a cabo en los siguientes pasos:
     * 1. Obtiene la referencia al nodo "Users" en la base de datos.
     * 2. Crea o accede a un hijo con el userId proporcionado, que corresponde al usuario.
     * 3. Establece los datos del usuario en ese nodo, lo que incluye todos los campos de
     *    la entidad UserEntity.
     * 4. La operación se espera de manera asíncrona para garantizar que se complete antes de
     *    retornar un resultado.
     */
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
}
