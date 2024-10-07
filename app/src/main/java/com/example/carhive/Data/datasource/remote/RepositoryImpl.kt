/**
 * Implementación principal del repositorio que coordina todas las operaciones con Firebase.
 *
 * Esta clase actúa como un punto centralizado para interactuar con las diferentes fuentes de datos
 * de Firebase, facilitando así las operaciones de autenticación, almacenamiento de datos de usuario
 * y gestión de archivos multimedia. Al hacerlo, proporciona una API unificada para que las capas
 * superiores de la aplicación puedan acceder a estas funcionalidades de manera coherente y sencilla.
 *
 * Esta clase implementa la interfaz [AuthRepository], lo que garantiza que las operaciones relacionadas
 * con la autenticación y los datos del usuario se gestionen de manera estandarizada. Se basa en el uso
 * de tres fuentes de datos específicas:
 * - Firebase Auth para operaciones de autenticación.
 * - Firebase Realtime Database para el almacenamiento y recuperación de datos de usuario.
 * - Firebase Storage para la gestión de archivos multimedia, como imágenes de perfil.
 */
package com.example.carhive.Data.datasource.remote

import android.net.Uri
import com.example.carhive.Data.datasource.remote.Firebase.FirebaseAuthDataSource
import com.example.carhive.Data.datasource.remote.Firebase.FirebaseDatabaseDataSource
import com.example.carhive.Data.datasource.remote.Firebase.FirebaseStorageDataSource
import com.example.carhive.Data.mapper.UserMapper
import com.example.carhive.Data.repository.AuthRepository
import com.example.carhive.Domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class RepositoryImpl(
    private val auth: FirebaseAuth,                          // Instancia principal de Firebase Auth, utilizada para autenticar usuarios.
    private val database: FirebaseDatabase,                  // Instancia principal de Firebase Database, utilizada para operar con la base de datos en tiempo real.
    private val storage: FirebaseStorage,                    // Instancia principal de Firebase Storage, utilizada para almacenar archivos multimedia.
    private val dataSource: FirebaseDatabaseDataSource,      // Fuente de datos para operaciones de base de datos, maneja el almacenamiento y recuperación de datos de usuario.
    private val dataSourceAuth: FirebaseAuthDataSource,      // Fuente de datos para operaciones de autenticación, maneja el inicio de sesión y registro de usuarios.
    private val dataSourceStorage: FirebaseStorageDataSource, // Fuente de datos para operaciones de almacenamiento, maneja la subida y recuperación de archivos.
    private val userMapper: UserMapper                       // Mapper para convertir entre modelos de dominio (User) y datos (UserEntity).
) : AuthRepository {

    /**
     * Sección: Operaciones de Firebase Storage
     */

    /**
     * Sube una imagen de perfil al almacenamiento.
     *
     * Este método permite subir una imagen que representa el perfil de un usuario a Firebase Storage.
     * La imagen se asocia con el ID del usuario y se almacena en la ruta específica para que sea
     * fácilmente accesible. La URL de descarga de la imagen se puede utilizar para mostrar la imagen
     * en la aplicación.
     *
     * @param userId ID del usuario cuya imagen se está subiendo.
     * @param uri URI de la imagen local a subir. Debe apuntar a la ubicación de la imagen en el dispositivo.
     * @return Result<String> Éxito con la URL de descarga de la imagen o error si ocurre un problema
     *                       durante la operación.
     */
    override suspend fun uploadProfileImage(userId: String, uri: Uri): Result<String> {
        return dataSourceStorage.uploadProfileImage(userId, uri)
    }

    /**
     * Sección: Operaciones de Firebase Database
     */

    /**
     * Guarda la información del usuario en la base de datos.
     *
     * Este método permite guardar un objeto User en Firebase Realtime Database. La información se
     * convierte a una entidad específica mediante un mapper antes de ser almacenada. Este enfoque
     * garantiza que la estructura de datos sea adecuada para la base de datos.
     *
     * @param userId ID del usuario a guardar, que se utiliza como clave en la base de datos.
     * @param user Objeto User que contiene los datos que se desean almacenar. Este objeto debe ser
     *             representativo de la información del usuario, como nombre, correo electrónico y rol.
     * @return Result<Unit> Éxito si los datos se guardaron correctamente, o error de la operación
     *                      si ocurre un problema.
     */
    override suspend fun saveUserToDatabase(userId: String, user: User): Result<Unit> {
        val userEntity = userMapper.mapToEntity(user) // Mapea el objeto User a UserEntity
        return dataSource.saveUserToDatabase(userId, userEntity)
    }

    /**
     * Obtiene el rol de un usuario específico.
     *
     * Este método recupera el rol asociado a un usuario en Firebase Realtime Database. Se utiliza
     * el ID del usuario para acceder a la información correspondiente. Si el usuario no tiene rol
     * asignado, el resultado puede ser null.
     *
     * @param userId ID del usuario del que se desea obtener el rol.
     * @return Result<Int?> Éxito con el rol del usuario si existe, null si el usuario no tiene rol
     *                      asignado, o error si ocurre un problema durante la operación.
     */
    override suspend fun getUserRole(userId: String): Result<Int?> {
        return dataSource.getUserRole(userId)
    }

    /**
     * Sección: Operaciones de Firebase Auth
     */

    /**
     * Inicia sesión de usuario con email y contraseña.
     *
     * Este método permite que un usuario se autentique en la aplicación utilizando su correo
     * electrónico y contraseña. Si la autenticación es exitosa, se devuelve el ID del usuario
     * autenticado, que puede ser utilizado para realizar operaciones adicionales en la aplicación.
     *
     * @param email Email del usuario que intenta iniciar sesión.
     * @param password Contraseña del usuario que corresponde al email.
     * @return Result<String?> Éxito con el ID del usuario autenticado si la autenticación fue exitosa,
     *                       o error si ocurre un problema durante la operación.
     */
    override suspend fun loginUser(email: String, password: String): Result<String?> {
        return dataSourceAuth.loginUser(email, password)
    }

    /**
     * Registra un nuevo usuario con email y contraseña.
     *
     * Este método permite registrar un nuevo usuario en la aplicación utilizando su correo
     * electrónico y contraseña. Al finalizar el registro, se devuelve el ID del usuario creado,
     * lo que puede ser útil para realizar operaciones de almacenamiento de datos inmediatamente después.
     *
     * @param email Email para el nuevo usuario que se está registrando.
     * @param password Contraseña para el nuevo usuario.
     * @return Result<String> Éxito con el ID del usuario creado, o error si ocurre un problema
     *                       durante la operación de registro.
     */
    override suspend fun registerUser(email: String, password: String): Result<String> {
        return dataSourceAuth.registerUser(email, password)
    }

    /**
     * Obtiene el ID del usuario actualmente autenticado.
     *
     * Este método se utiliza para recuperar el ID del usuario que ha iniciado sesión en la
     * aplicación. Si no hay un usuario autenticado, el método devuelve null.
     *
     * @return Result<String?> Éxito con el ID del usuario actual si hay una sesión activa,
     *                       o null si no hay un usuario autenticado.
     */
    override suspend fun getCurrentUserId(): Result<String?> {
        return dataSourceAuth.getCurrentUserId()
    }
}
