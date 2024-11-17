package com.example.carhive.data.mapper

import com.example.carhive.data.model.MessageEntity
import com.example.carhive.Domain.model.Message
import javax.inject.Inject

/**
 * Mapper class to convert between MessageEntity and Message objects.
 * This helps in separating data layer entities from domain layer models.
 */
class MessageMapper @Inject constructor() {

    /**
     * Maps a MessageEntity object (data layer) to a Message object (domain layer).
     *
     * @param entity The MessageEntity instance to map.
     * @return A Message instance with properties copied from the MessageEntity.
     */
    fun mapToDomain(entity: MessageEntity): Message {
        return Message(
            messageId = entity.messageId,
            senderId = entity.senderId,
            receiverId = entity.receiverId,
            carId = entity.carId,
            content = entity.content,
            timestamp = entity.timestamp,
            fileUrl = entity.fileUrl,
            fileType = entity.fileType,
            fileName = entity.fileName,
            fileSize = entity.fileSize,
            hash = entity.hash,
            status = entity.status,
            deletedFor = entity.deletedFor
        )
    }

    /**
     * Maps a Message object (domain layer) to a MessageEntity object (data layer).
     *
     * @param domain The Message instance to map.
     * @return A MessageEntity instance with properties copied from the Message.
     */
    fun mapToEntity(domain: Message): MessageEntity {
        return MessageEntity(
            messageId = domain.messageId,
            senderId = domain.senderId,
            receiverId = domain.receiverId,
            carId = domain.carId,
            content = domain.content,
            timestamp = domain.timestamp,
            fileUrl = domain.fileUrl,
            fileType = domain.fileType,
            fileName = domain.fileName,
            fileSize = domain.fileSize,
            hash = domain.hash,
            status = domain.status,
            deletedFor = domain.deletedFor
        )
    }
}
