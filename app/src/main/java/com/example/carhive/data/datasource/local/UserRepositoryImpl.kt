/**
 * Implementación del repositorio de usuarios que maneja el almacenamiento local de datos de usuario.
 *
 * Esta clase proporciona métodos para:
 * - Guardar y recuperar información del usuario utilizando SharedPreferences.
 * - Gestionar credenciales de usuario, incluyendo la contraseña.
 * - Mapear entre modelos de dominio (User) y entidades de datos (UserEntity) utilizando un mapper.
 *
 * Utiliza SharedPreferences como mecanismo de almacenamiento local para persistir
 * los datos del usuario entre sesiones de la aplicación.
 *
 * @property sharedPreferences Almacenamiento local para la persistencia de datos del usuario.
 * @property userMapper Convierte entre modelos de dominio y entidades de datos, facilitando
 *                      la manipulación de los datos del usuario.
 */
package com.example.carhive.data.datasource.local

import android.content.SharedPreferences
import com.example.carhive.data.exception.RepositoryException
import com.example.carhive.data.model.UserEntity
import com.example.carhive.Domain.model.User
import com.example.carhive.data.repository.UserRepository
import com.example.carhive.data.mapper.UserMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val userMapper: UserMapper // Convierte entre modelos de dominio y entidades de datos
) : UserRepository {

    override suspend fun saveUser(user: User): Result<Unit> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val userEntity = userMapper.mapToEntity(user) // Mapea el usuario a una entidad
                with(sharedPreferences.edit()) {
                    putString("firstName", userEntity.firstName)
                    putString("lastName", userEntity.lastName)
                    putString("email", userEntity.email)
                    putString("phoneNumber", userEntity.phoneNumber)
                    putString("voterID", userEntity.voterID)
                    putString("curp", userEntity.curp)
                    putString("imageUrl", userEntity.imageUrl)
                    putString("imageUrl2", userEntity.imageUrl2)
                    putInt("role", userEntity.role)
                    putBoolean("termsUser", userEntity.termsUser)
                    apply() // Aplica los cambios en SharedPreferences
                }
                Result.success(Unit) // Retorno exitoso
            } catch (e: Exception) {
                Result.failure(RepositoryException("Error saving user: ${e.message}", e))
            }
        }
    }

    override suspend fun getUser(): Result<User> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                // Crea un UserEntity a partir de SharedPreferences
                val userEntity = UserEntity(
                    firstName = sharedPreferences.getString("firstName", "") ?: "",
                    lastName = sharedPreferences.getString("lastName", "") ?: "",
                    email = sharedPreferences.getString("email", "") ?: "",
                    phoneNumber = sharedPreferences.getString("phoneNumber", "") ?: "",
                    voterID = sharedPreferences.getString("voterID", "") ?: "",
                    curp = sharedPreferences.getString("curp", "") ?: "",
                    imageUrl = sharedPreferences.getString("imageUrl", null),
                    imageUrl2 = sharedPreferences.getString("imageUrl2", null),
                    role = sharedPreferences.getInt("role", -1), // -1 indica usuario sin rol asignado
                    termsUser = sharedPreferences.getBoolean("termsUser",false)
                )
                val user = userMapper.mapToDomain(userEntity) // Mapea a User del dominio
                Result.success(user) // Retorno exitoso con el usuario
            } catch (e: Exception) {
                Result.failure(RepositoryException("Error retrieving user: ${e.message}", e))
            }
        }
    }

    override suspend fun clear(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                with(sharedPreferences.edit()) {
                    clear() // Limpia todas las preferencias almacenadas
                    apply() // Aplica los cambios
                }
                Result.success(Unit) // Retorno exitoso
            } catch (e: Exception) {
                Result.failure(RepositoryException("Error clearing preferences: ${e.message}", e))
            }
        }
    }

    override suspend fun savePassword(password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                with(sharedPreferences.edit()) {
                    putString("password", password) // Almacena la contraseña
                    apply() // Aplica los cambios
                }
                Result.success(Unit) // Retorno exitoso
            } catch (e: Exception) {
                Result.failure(RepositoryException("Error saving password: ${e.message}", e))
            }
        }
    }

    override suspend fun getPassword(): Result<String?> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val password = sharedPreferences.getString("password", null) // Recupera la contraseña
                Result.success(password) // Retorno exitoso con la contraseña
            } catch (e: Exception) {
                Result.failure(RepositoryException("Error retrieving password: ${e.message}", e))
            }
        }
    }
}
