/**
 * Fuente de datos que maneja la autenticación con Firebase.
 *
 * Esta clase proporciona una capa de abstracción para las operaciones de autenticación
 * de Firebase, incluyendo:
 * - Inicio de sesión de usuarios
 * - Registro de nuevos usuarios
 * - Obtención de información del usuario autenticado
 *
 * Todas las operaciones están envueltas en Result para manejar errores de manera segura
 * y consistente.
 */
package com.example.carhive.Data.datasource.remote.Firebase

import com.example.carhive.Data.exception.RepositoryException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth // Instancia de FirebaseAuth para operaciones de autenticación
) {

    suspend fun loginUser(email: String, password: String): Result<String?> {
        return try {
            // Realiza la autenticación del usuario utilizando email y contraseña.
            auth.signInWithEmailAndPassword(email, password).await()
            // Obtiene el ID del usuario autenticado.
            val userIdResult = getCurrentUserId()
            val userId = userIdResult.getOrNull()
            Result.success(userId) // Retorna el ID del usuario.
        } catch (e: Exception) {
            // Captura cualquier excepción y devuelve un resultado de error.
            Result.failure(RepositoryException("Error logging in user: ${e.message}", e))
        }
    }

    suspend fun registerUser(email: String, password: String): Result<String> {
        return try {
            // Crea un nuevo usuario utilizando email y contraseña.
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: "" // Obtiene el ID del usuario creado.

            // Envía el correo de verificación después de registrar al usuario
            result.user?.let { sendVerificationEmail(it) }

            Result.success(userId) // Retorna el ID del usuario creado.
        } catch (e: Exception) {
            // Captura cualquier excepción y devuelve un resultado de error.
            Result.failure(RepositoryException("Error registering user: ${e.message}", e))
        }
    }

    // Método para enviar el correo de verificación
    private suspend fun sendVerificationEmail(user: FirebaseUser): Result<Unit> {
        return try {
            user.sendEmailVerification().await() // Envía el correo de verificación
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): Result<String?> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser // Obtiene el usuario actual.
            val userId = currentUser?.uid // Obtiene el ID del usuario.
            Result.success(userId) // Retorna el ID del usuario.
        } catch (e: Exception) {
            // Captura cualquier excepción y devuelve un resultado de error.
            Result.failure(e)
        }
    }
}
