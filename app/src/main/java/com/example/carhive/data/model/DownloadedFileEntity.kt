package com.example.carhive.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a file that has been downloaded and stored in the local database.
 *
 * @property fileHash The unique hash of the file, used as the primary key to identify files.
 * @property fileName The name of the file.
 * @property filePath The path where the file is stored locally.
 * @property fileType The type of the file (e.g., image, video, application).
 */
@Entity(tableName = "downloaded_files")
data class DownloadedFileEntity(
    @PrimaryKey val fileHash: String,    // Unique hash used as primary key
    val fileName: String,                // Name of the file
    val filePath: String,                // Local storage path of the file
    val fileType: String                 // Type of the file (e.g., image, video)
)
