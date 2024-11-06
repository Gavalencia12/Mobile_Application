/**
 * Implementación de [SessionImpl] que maneja la gestión de sesiones de usuario.
 *
 * Esta clase se encarga de:
 * - Verificar el estado de autenticación del usuario mediante el repositorio de autenticación.
 * - Obtener y almacenar roles de usuario para una correcta gestión de permisos.
 * - Persistir la información de sesión localmente utilizando SharedPreferences.
 *
 * La clase implementa la interfaz [SessionRepository], que define los métodos para
 * gestionar las sesiones de usuario.
 *
 * @property sharedPreferences Almacenamiento local para la persistencia de datos de sesión,
 *                             como el rol del usuario.
 * @property repository Repositorio para operaciones de autenticación, permitiendo obtener
 *                      el ID del usuario autenticado y su rol.
 */
package com.example.carhive.data.datasource.local

import android.content.SharedPreferences
import com.example.carhive.data.exception.RepositoryException
import com.example.carhive.data.repository.AuthRepository
import com.example.carhive.data.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SessionImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences, // Almacenamiento local para la persistencia de datos
    private val repository: AuthRepository  // Repositorio para operaciones de autenticación
) : SessionRepository {

    override suspend fun isUserAuthenticated(): Result<String?> {
        return try {
            repository.getCurrentUserId() // Se obtiene el ID del usuario autenticado
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error checking authentication: ${e.message}", e))
        }
    }

    override suspend fun getUserRole(userId: String): Result<Int?> {
        return try {
            repository.getUserRole(userId) // Se obtiene el rol del usuario según su ID
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error retrieving user role: ${e.message}", e))
        }
    }

    override suspend fun saveUserRole(userRole: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                with(sharedPreferences.edit()) {
                    putInt("user_role", userRole) // Se almacena el rol del usuario
                    apply() // Se aplican los cambios
                }
                Result.success(Unit) // Retorno exitoso
            } catch (e: Exception) {
                Result.failure(RepositoryException("Error saving user role: ${e.message}", e))
            }
        }
    }
}
