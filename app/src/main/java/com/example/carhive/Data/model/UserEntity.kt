/**
 * Entidad que representa un usuario en la capa de datos.
 *
 * Esta clase se utiliza principalmente para:
 * - Almacenamiento en Firebase Realtime Database, permitiendo la persistencia de los datos del usuario en la nube.
 * - Serialización y deserialización de datos de usuario, facilitando la conversión de objetos a formatos que pueden ser almacenados y recuperados.
 * - Transferencia de datos entre diferentes capas de la aplicación, asegurando que la información del usuario fluya correctamente desde la capa de presentación hasta la capa de persistencia y viceversa.
 *
 * Todos los campos tienen valores por defecto para facilitar la creación de instancias
 * y la deserialización desde Firebase. Esto asegura que incluso si algunos datos no están disponibles,
 * la aplicación puede seguir funcionando sin errores.
 */
package com.example.carhive.Data.model

data class UserEntity(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var voterID: String = "",
    var curp: String = "",
    var imageUrl: String? = null,
    var role: Int = 2,
    var termsUser: Boolean = false,
    var termsSeller: Boolean = false,
    var isverified: Boolean = false
)
