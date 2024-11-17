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
package com.example.carhive.data.datasource.remote.Firebase

import android.net.Uri
import com.example.carhive.data.exception.RepositoryException
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage // Instancia de Firebase Storage para operaciones de archivos
) {

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

    suspend fun uploadCarImages(userId: String, carId:String, uris: List<Uri>): Result<List<String>> {
        return try {
            // Lista para almacenar las URLs de descarga de cada imagen
            val downloadUrls = mutableListOf<String>()

            // Recorre cada URI y sube la imagen
            uris.forEachIndexed { index, uri ->
                // Crea una referencia única para cada imagen dentro del ID del coche
                val storageRef = storage.getReference("Car/$userId/$carId/image_$index.jpg")

                // Sube el archivo y espera a que se complete
                val taskSnapshot = storageRef.putFile(uri).await()

                // Obtiene la URL de descarga del archivo subido
                val downloadUrl = taskSnapshot.storage.downloadUrl.await()

                // Añade la URL de descarga a la lista
                downloadUrls.add(downloadUrl.toString())
            }

            // Retorna la lista de URLs como resultado exitoso
            Result.success(downloadUrls)
        } catch (e: Exception) {
            // Captura cualquier excepción y devuelve un resultado de error con detalles
            Result.failure(RepositoryException("Error uploading car images: ${e.message}", e))
        }
    }


}
