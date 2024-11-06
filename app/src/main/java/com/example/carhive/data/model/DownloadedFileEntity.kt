package com.example.carhive.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_files")
data class DownloadedFileEntity(
    @PrimaryKey val fileHash: String,
    val fileName: String,
    val filePath: String,
    val fileType: String  // Nuevo campo para almacenar el tipo de archivo
)

