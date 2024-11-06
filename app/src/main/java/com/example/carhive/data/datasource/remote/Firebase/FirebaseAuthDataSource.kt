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
package com.example.carhive.data.datasource.remote.Firebase

import com.example.carhive.data.exception.RepositoryException
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
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                if (firebaseUser.isEmailVerified) {
                    // Si el correo está verificado, obtenemos el ID del usuario autenticado.
                    val userIdResult = getCurrentUserId()
                    val userId = userIdResult.getOrNull()
                    Result.success(userId) // Retorna el ID del usuario.
                } else {
                    // Si el correo no está verificado, devolvemos un error personalizado.
                    sendVerificationEmail(firebaseUser)
                    Result.failure(RepositoryException("Email not verified."))
                }
            } else {
                Result.failure(RepositoryException("User not found."))
            }
        } catch (e: Exception) {
            // Captura cualquier excepción y devuelve un resultado de error.
            Result.failure(RepositoryException("Error logging in user: ${e.message}", e))
        }
    }

    suspend fun registerUser(email: String, password: String): Result<String> {
        return try {
            // Crea un nuevo usuario utilizando email y contraseña.
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(RepositoryException("User creation failed"))
            val userId = user.uid

            // Envía el correo de verificación después de registrar al usuario.
            sendVerificationEmail(user)

            Result.success(userId) // Retorna el ID del usuario creado si todo está bien.
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

    suspend fun isVerifiedTheEmail(): Result<Unit> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // Verificar si el correo está verificado
                if (!currentUser.isEmailVerified) {
                    // Cerrar sesión si el correo no está verificado
                    FirebaseAuth.getInstance().signOut()
                    Result.failure(Exception("El correo no está verificado, sesión cerrada."))
                } else {
                    // Si está verificado, retornamos éxito
                    Result.success(Unit)
                }
            } else {
                Result.failure(Exception("Usuario no autenticado."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}
