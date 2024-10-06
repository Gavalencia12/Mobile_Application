package com.example.carhive.Data.repository

import android.net.Uri
import com.example.carhive.Domain.model.User

/**
 * Interfaz que define las operaciones relacionadas con la autenticación y gestión de usuarios.
 *
 * Esta interfaz actúa como un contrato para la implementación de la autenticación en la aplicación,
 * incluyendo el registro, inicio de sesión y manejo de imágenes de perfil.
 *
 * Todas las funciones son suspending functions, lo que permite que se ejecuten de manera asíncrona
 * dentro de un coroutine, evitando bloqueos en el hilo principal.
 */
interface AuthRepository {

    /**
     * Registra un nuevo usuario en el sistema con un correo electrónico y una contraseña.
     *
     * @param email Correo electrónico del usuario que se registrará.
     * @param password Contraseña del usuario que se registrará.
     * @return [Result<String>] Un objeto Result que contiene el ID del usuario registrado o un error.
     *
     * Esta función maneja la creación de una cuenta nueva y puede involucrar
     * validaciones adicionales como la verificación de si el correo electrónico ya está en uso.
     */
    suspend fun registerUser(email: String, password: String): Result<String>

    /**
     * Carga la imagen de perfil del usuario en el almacenamiento correspondiente.
     *
     * @param userId ID del usuario cuyo perfil se está actualizando.
     * @param uri URI de la imagen que se va a cargar.
     * @return [Result<String>] Un objeto Result que contiene la URL de la imagen cargada o un error.
     *
     * Esta función permite al usuario personalizar su perfil mediante la carga de una imagen,
     * y la URL resultante puede ser almacenada en la base de datos para su posterior recuperación.
     */
    suspend fun uploadProfileImage(userId: String, uri: Uri): Result<String>

    /**
     * Guarda los detalles del usuario en la base de datos después del registro.
     *
     * @param userId ID del usuario que se está guardando.
     * @param user Objeto [User] que contiene la información del usuario.
     * @return [Result<Unit>] Un objeto Result que indica el éxito o fallo de la operación.
     *
     * Esta función asegura que la información relevante del usuario se almacene de forma persistente
     * en la base de datos, completando así el proceso de registro.
     */
    suspend fun saveUserToDatabase(userId: String, user: User): Result<Unit>

    /**
     * Inicia sesión de un usuario en el sistema utilizando su correo electrónico y contraseña.
     *
     * @param email Correo electrónico del usuario que intenta iniciar sesión.
     * @param password Contraseña del usuario que intenta iniciar sesión.
     * @return [Result<String?>] Un objeto Result que contiene el ID del usuario autenticado o null si falla.
     *
     * Esta función maneja la autenticación del usuario y retorna su ID si el inicio de sesión es exitoso.
     * En caso de fallo, se retornará un error.
     */
    suspend fun loginUser(email: String, password: String): Result<String?>

    /**
     * Obtiene el ID del usuario actualmente autenticado.
     *
     * @return [Result<String?>] Un objeto Result que contiene el ID del usuario autenticado o null si no hay ningún usuario autenticado.
     *
     * Esta función es útil para acceder a información del usuario que ha iniciado sesión
     * y puede ser utilizada en diversas partes de la aplicación para obtener el contexto del usuario.
     */
    suspend fun getCurrentUserId(): Result<String?>

    /**
     * Obtiene el rol del usuario dado su ID.
     *
     * @param userId ID del usuario cuyo rol se desea obtener.
     * @return [Result<Int?>] Un objeto Result que contiene el rol del usuario como un valor entero o null si no se encuentra.
     *
     * Esta función permite determinar el nivel de acceso y permisos del usuario dentro de la aplicación
     * basado en su rol asignado.
     */
    suspend fun getUserRole(userId: String): Result<Int?>
}
