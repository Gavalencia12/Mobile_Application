/**
 * Fuente de datos que maneja las operaciones con Firebase Storage.
 *
 * Esta clase proporciona funcionalidades para interactuar con Firebase Storage, permitiendo
 * realizar las siguientes operaciones:
 * - Subir imágenes de perfil de usuarios.
 * - Manejar el almacenamiento de archivos multimedia.
 * - Gestionar URLs de descarga de archivos almacenados.
 *
 * La estructura de almacenamiento en Firebase Storage sigue el siguiente patrón:
 * ```
 * /Users
 *    ├── userId1
 *    │   └── profile.jpg
 *    └── userId2
 *        └── profile.jpg
 * ```
 */
package com.example.carhive.Data.datasource.remote.Firebase

import android.net.Uri
import com.example.carhive.Data.exception.RepositoryException
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage // Instancia de Firebase Storage para operaciones de archivos
) {

    /**
     * Sube una imagen de perfil al Firebase Storage y retorna su URL de descarga.
     *
     * Este método se utiliza para subir una imagen que representa el perfil de un usuario
     * a Firebase Storage. Una vez que la imagen se ha subido exitosamente, se obtiene y
     * retorna la URL de descarga pública, que puede ser utilizada en la aplicación para
     * mostrar la imagen del perfil del usuario.
     *
     * @param userId Identificador único del usuario cuya imagen se está subiendo.
     * @param uri URI local de la imagen a subir, que apunta a la ubicación de la imagen en el dispositivo.
     * @return Result<String> Éxito con la URL de descarga de la imagen subida,
     *                       o failure con [RepositoryException] si ocurre un error durante la operación.
     *
     * El proceso de subida se lleva a cabo en los siguientes pasos:
     * 1. Se crea una referencia específica en Firebase Storage para la imagen de perfil del usuario,
     *    utilizando el userId para garantizar que la imagen se almacene en la carpeta correcta.
     * 2. Se sube el archivo al almacenamiento mediante el método `putFile`, y se espera a que la
     *    operación se complete de manera asíncrona.
     * 3. Se obtiene la URL de descarga del archivo subido, que es accesible públicamente.
     *
     * Notas importantes:
     * - La imagen se guarda con el nombre 'profile.jpg' independientemente de su formato original,
     *   lo que significa que si el usuario sube múltiples imágenes, se sobrescribirá la anterior.
     * - La estructura de carpetas en Firebase Storage sigue el patrón: 'Users/{userId}/profile.jpg',
     *   lo que facilita la organización y recuperación de imágenes de perfil por usuario.
     * - La URL de descarga generada es pública, lo que permite que sea utilizada directamente
     *   en la aplicación para cargar la imagen del perfil del usuario sin restricciones adicionales.
     */
    suspend fun uploadProfileImage(userId: String, uri: Uri): Result<String> {
        return try {
            // Obtiene la referencia específica para la imagen de perfil del usuario
            val storageRef = storage.getReference("Users/$userId/profile.jpg")

            // Sube el archivo y espera a que se complete
            val taskSnapshot = storageRef.putFile(uri).await()

            // Obtiene la URL de descarga del archivo subido
            // Nota: Utilizamos downloadUrl que es el método recomendado actualmente
            val downloadUrl = taskSnapshot.storage.downloadUrl.await()

            Result.success(downloadUrl.toString()) // Retorna la URL de descarga como resultado exitoso
        } catch (e: Exception) {
            // Captura cualquier excepción y devuelve un resultado de error con detalles
            Result.failure(RepositoryException("Error uploading profile image: ${e.message}", e))
        }
    }
}
