package com.example.carhive.Data.repository

/**
 * Interfaz que define las operaciones relacionadas con la gestión de sesiones de usuario.
 *
 * Esta interfaz actúa como un contrato para la implementación de la lógica de sesión
 * en la aplicación, permitiendo verificar la autenticación del usuario y gestionar su rol.
 *
 * Todas las funciones son suspending functions, lo que permite que se ejecuten de manera asíncrona
 * dentro de un coroutine, evitando bloqueos en el hilo principal.
 */
interface SessionRepository {

    /**
     * Verifica si el usuario está autenticado en el sistema.
     *
     * @return [Result<String?>] Un objeto Result que contiene el ID del usuario autenticado o null si no hay ningún usuario autenticado.
     *
     * Esta función comprueba la validez de la sesión del usuario y proporciona su ID si está autenticado.
     * Es útil para controlar el acceso a diferentes partes de la aplicación según el estado de autenticación del usuario.
     */
    suspend fun isUserAuthenticated(): Result<String?>

    /**
     * Guarda el rol del usuario en la sesión actual.
     *
     * @param userRole Rol del usuario representado como un valor entero.
     * @return [Result<Unit>] Un objeto Result que indica el éxito o fallo de la operación.
     *
     * Esta función permite establecer el rol del usuario, lo que puede influir en su acceso y permisos dentro de la aplicación.
     * Almacenar el rol ayuda a mantener la consistencia en la gestión de sesiones de usuario.
     */
    suspend fun saveUserRole(userRole: Int): Result<Unit>

    /**
     * Obtiene el rol del usuario dado su ID.
     *
     * @param userId ID del usuario cuyo rol se desea obtener.
     * @return [Result<Int?>] Un objeto Result que contiene el rol del usuario como un valor entero o null si no se encuentra.
     *
     * Esta función permite determinar el nivel de acceso y permisos del usuario basado en su rol asignado.
     * Es útil para manejar la lógica de autorización y acceso a diferentes funcionalidades de la aplicación.
     */
    suspend fun getUserRole(userId: String): Result<Int?>
}
