package com.example.carhive.Data.repository

import com.example.carhive.Domain.model.User

/**
 * Interfaz que define las operaciones relacionadas con la gestión de datos de usuario.
 *
 * Esta interfaz actúa como un contrato para la implementación de la lógica de acceso y manipulación
 * de la información del usuario en la aplicación, incluyendo almacenamiento, recuperación y manejo de contraseñas.
 *
 * Todas las funciones son suspending functions, lo que permite su ejecución de manera asíncrona
 * dentro de un coroutine, evitando bloqueos en el hilo principal.
 */
interface UserRepository {

    /**
     * Guarda la información del usuario en el repositorio.
     *
     * @param user Objeto [User] que contiene los datos del usuario a guardar.
     * @return [Result<Unit>] Un objeto Result que indica el éxito o fallo de la operación.
     *
     * Esta función permite persistir los datos del usuario en la capa de almacenamiento,
     * asegurando que la información esté disponible para futuras operaciones.
     */
    suspend fun saveUser(user: User): Result<Unit>

    /**
     * Obtiene la información del usuario almacenada en el repositorio.
     *
     * @return [Result<User>] Un objeto Result que contiene el objeto [User] con los datos del usuario
     *         o un error si la operación falla.
     *
     * Esta función permite recuperar los datos del usuario de manera eficiente,
     * lo que es fundamental para la personalización y gestión de la experiencia del usuario en la aplicación.
     */
    suspend fun getUser(): Result<User>

    /**
     * Limpia los datos del usuario almacenados en el repositorio.
     *
     * @return [Result<Unit>] Un objeto Result que indica el éxito o fallo de la operación.
     *
     * Esta función se utiliza para eliminar la información del usuario, como parte del proceso
     * de cierre de sesión o en situaciones donde se requiera reiniciar el estado de la aplicación.
     */
    suspend fun clear(): Result<Unit>

    /**
     * Guarda la contraseña del usuario en el repositorio.
     *
     * @param password Cadena que representa la contraseña a guardar.
     * @return [Result<Unit>] Un objeto Result que indica el éxito o fallo de la operación.
     *
     * Esta función permite almacenar la contraseña del usuario, lo que es necesario
     * para facilitar la autenticación y el inicio de sesión.
     */
    suspend fun savePassword(password: String): Result<Unit>

    /**
     * Obtiene la contraseña del usuario almacenada en el repositorio.
     *
     * @return [Result<String?>] Un objeto Result que contiene la contraseña como una cadena
     *         o null si no se encuentra ninguna contraseña almacenada.
     *
     * Esta función permite recuperar la contraseña del usuario para realizar validaciones
     * durante el proceso de inicio de sesión o para otras operaciones de autenticación.
     */
    suspend fun getPassword(): Result<String?>
}
