package com.example.carhive.data.mapper

import com.example.carhive.data.model.MessageEntity
import com.example.carhive.Domain.model.Message
import javax.inject.Inject

class MessageMapper @Inject constructor() {

    fun mapToDomain(entity: MessageEntity): Message {
        return Message(
            senderId = entity.senderId,
            content = entity.content,
            timestamp = entity.timestamp,
            fileUrl = entity.fileUrl,
            fileType = entity.fileType,
            fileName = entity.fileName,
            hash = entity.hash  // Mapea el campo hash desde MessageEntity a Message
        )
    }

    fun mapToEntity(domain: Message): MessageEntity {
        return MessageEntity(
            senderId = domain.senderId,
            content = domain.content,
            timestamp = domain.timestamp,
            fileUrl = domain.fileUrl,
            fileType = domain.fileType,
            fileName = domain.fileName,
            hash = domain.hash  // Mapea el campo hash desde Message a MessageEntity
        )
    }
}
