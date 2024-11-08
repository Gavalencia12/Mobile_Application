package com.example.carhive.data.mapper

import com.example.carhive.data.model.MessageEntity
import com.example.carhive.Domain.model.Message
import javax.inject.Inject

// MessageMapper.kt
class MessageMapper @Inject constructor() {

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

