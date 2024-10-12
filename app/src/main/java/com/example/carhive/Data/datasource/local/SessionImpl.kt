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
package com.example.carhive.Data.datasource.local

import android.content.SharedPreferences
import com.example.carhive.Data.exception.RepositoryException
import com.example.carhive.Data.repository.AuthRepository
import com.example.carhive.Data.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SessionImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences, // Almacenamiento local para la persistencia de datos
    private val repository: AuthRepository  // Repositorio para operaciones de autenticación
) : SessionRepository {

    /**
     * Verifica si existe un usuario autenticado actualmente.
     *
     * Este método consulta al repositorio de autenticación para obtener el ID del usuario
     * actualmente autenticado. Si no hay un usuario autenticado, se retorna null. En caso
     * de cualquier error durante la consulta, se captura la excepción y se retorna un fallo
     * encapsulado en un [RepositoryException].
     *
     * @return Result<String?> Éxito con el ID del usuario si está autenticado,
     *                         null si no lo está, o failure con [RepositoryException]
     *                         si ocurre un error durante la verificación.
     */
    override suspend fun isUserAuthenticated(): Result<String?> {
        return try {
            repository.getCurrentUserId() // Se obtiene el ID del usuario autenticado
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error checking authentication: ${e.message}", e))
        }
    }

    /**
     * Obtiene el rol asignado a un usuario específico.
     *
     * Este método permite consultar el rol del usuario en base a su ID. Si el rol se encuentra
     * correctamente, se retorna como un resultado exitoso. Si se produce un error durante la
     * consulta, se captura la excepción y se retorna un fallo encapsulado en un
     * [RepositoryException].
     *
     * @param userId ID del usuario del cual se quiere obtener el rol.
     * @return Result<Int?> Éxito con el rol del usuario si se encuentra,
     *                      o failure con [RepositoryException] si ocurre un error
     *                      durante la recuperación del rol.
     */
    override suspend fun getUserRole(userId: String): Result<Int?> {
        return try {
            repository.getUserRole(userId) // Se obtiene el rol del usuario según su ID
        } catch (e: Exception) {
            Result.failure(RepositoryException("Error retrieving user role: ${e.message}", e))
        }
    }

    /**
     * Almacena el rol del usuario en SharedPreferences para acceso local.
     *
     * Este método permite persistir el rol del usuario en el almacenamiento local utilizando
     * SharedPreferences. Si el rol se guarda correctamente, se retorna un resultado exitoso.
     * En caso de error durante la operación de guardado, se captura la excepción y se
     * retorna un fallo encapsulado en un [RepositoryException].
     *
     * @param userRole El rol del usuario a guardar en SharedPreferences.
     * @return Result<Unit> Éxito si se guardó correctamente,
     *                      o failure con [RepositoryException] si ocurre un error
     *                      durante el proceso de guardado.
     */
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