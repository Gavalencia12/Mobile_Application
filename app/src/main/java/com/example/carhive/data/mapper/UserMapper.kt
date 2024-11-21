/**
 * Mapper responsable de la conversión bidireccional entre modelos de usuario.
 *
 * Esta clase se encarga de transformar los datos entre dos representaciones:
 * - [UserEntity]: Modelo de datos utilizado para la persistencia en la base de datos.
 * - [User]: Modelo de dominio utilizado en la lógica de negocio de la aplicación.
 *
 * El mapper facilita:
 * - La conversión entre modelos de datos y de dominio, asegurando que los datos se mantengan coherentes durante el proceso.
 * - El mapeo de roles de usuario entre diferentes representaciones, permitiendo una gestión eficaz de la lógica de acceso y permisos.
 * - La validación de la integridad de los datos, asegurando que la información se mantenga consistente a lo largo de las conversiones.
 *
 * Tabla de mapeo de roles:
 * ```
 * | Valor BD | UserRole      |
 * |----------|---------------|
 * | 0        | ADMIN         |
 * | 1        | ADVANCED_USER |
 * | 2        | NORMAL_USER   |
 * ```
 */
package com.example.carhive.data.mapper

import com.example.carhive.data.model.UserEntity
import com.example.carhive.Domain.model.User
import com.example.carhive.Domain.model.UserRole
import javax.inject.Inject

class UserMapper @Inject constructor() {

    /**
     * Convierte una entidad de datos [UserEntity] al modelo de dominio [User].
     *
     * Este método transforma todos los campos del modelo de datos en su correspondiente representación de dominio,
     * asegurando que la información necesaria para la lógica de negocio esté disponible.
     *
     * @param entity Entidad de usuario a convertir
     * @return [User] Modelo de dominio convertido
     *
     * Realiza la conversión de todos los campos, incluyendo:
     * - Información personal (nombre, email, número de teléfono, etc.)
     * - Documentos de identificación (voterID, CURP)
     * - Estado de verificación y aceptación de términos
     * - Rol de usuario (mediante [mapRoleToDomain])
     */
    fun mapToDomain(entity: UserEntity): User {
        return User(
            firstName = entity.firstName,
            lastName = entity.lastName,
            email = entity.email,
            phoneNumber = entity.phoneNumber,
            voterID = entity.voterID,
            curp = entity.curp,
            imageUrl = entity.imageUrl,
            imageUrl2 = entity.imageUrl2,
            role = mapRoleToDomain(entity.role),
            termsUser = entity.termsUser,
            termsSeller = entity.termsSeller,
            isVerified = entity.isverified,
            verificationTimestamp = entity.verificationTimestamp
        )
    }

    /**
     * Convierte un modelo de dominio [User] a una entidad de datos [UserEntity].
     *
     * Este método asegura que todos los datos relevantes del modelo de dominio se conviertan
     * adecuadamente a la representación persistente, manteniendo la integridad y el significado
     * de la información.
     *
     * @param domainModel Modelo de dominio de usuario a convertir
     * @return [UserEntity] Entidad de datos convertida
     *
     * Realiza la conversión inversa de todos los campos, asegurando:
     * - Preservación de toda la información del usuario
     * - Conversión correcta del rol (mediante [mapRoleToEntity])
     * - Mantenimiento de los estados de verificación y aceptación de términos
     */
    fun mapToEntity(domainModel: User): UserEntity {
        return UserEntity(
            firstName = domainModel.firstName,
            lastName = domainModel.lastName,
            email = domainModel.email,
            phoneNumber = domainModel.phoneNumber,
            voterID = domainModel.voterID,
            curp = domainModel.curp,
            imageUrl = domainModel.imageUrl,
            imageUrl2 = domainModel.imageUrl2,
            role = mapRoleToEntity(domainModel.role),
            termsUser = domainModel.termsUser,
            termsSeller = domainModel.termsSeller,
            isverified = domainModel.isVerified,
            verificationTimestamp = domainModel.verificationTimestamp
        )
    }

    /**
     * Convierte el valor numérico del rol almacenado en la base de datos
     * a su correspondiente enum [UserRole].
     *
     * Este método interpreta el valor numérico recibido y retorna el rol de usuario correspondiente,
     * lo que permite una traducción efectiva de datos desde la capa de persistencia
     * a la lógica de negocio.
     *
     * @param role Valor numérico del rol (0-2)
     * @return [UserRole] Enum correspondiente al rol
     *
     * Mapeo:
     * - 0 -> ADMIN
     * - 1 -> ADVANCED_USER
     * - 2 o cualquier otro valor -> NORMAL_USER (valor por defecto)
     */
    private fun mapRoleToDomain(role: Int): UserRole {
        return when (role) {
            0 -> UserRole.ADMIN
            1 -> UserRole.ADVANCED_USER
            else -> UserRole.NORMAL_USER // Valor por defecto
        }
    }

    /**
     * Convierte el enum [UserRole] a su correspondiente valor numérico
     * para almacenamiento en la base de datos.
     *
     * Este método garantiza que los roles se traduzcan correctamente a un formato
     * que puede ser almacenado en la base de datos, facilitando la persistencia de los datos
     * relacionados con los usuarios.
     *
     * @param role Enum [UserRole] a convertir
     * @return [Int] Valor numérico correspondiente al rol
     *
     * Mapeo:
     * - ADMIN -> 0
     * - ADVANCED_USER -> 1
     * - NORMAL_USER -> 2
     */
    private fun mapRoleToEntity(role: UserRole): Int {
        return when (role) {
            UserRole.ADMIN -> 0
            UserRole.ADVANCED_USER -> 1
            UserRole.NORMAL_USER -> 2
        }
    }
}
