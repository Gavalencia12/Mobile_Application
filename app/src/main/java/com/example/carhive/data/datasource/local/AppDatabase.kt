package com.example.carhive.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.carhive.data.model.DownloadedFileEntity
import com.example.carhive.data.repository.DownloadedFileDao

/**
 * Room database class for the application.
 * Defines the database configuration and serves as the main access point to the persisted data.
 *
 * @Database annotation:
 * - `entities`: List of all entities (tables) in the database.
 * - `version`: Database version, used for migrations.
 */
@Database(entities = [DownloadedFileEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to the DAO (Data Access Object) for downloaded files.
     *
     * @return An instance of DownloadedFileDao for database operations on the downloaded files table.
     */
    abstract fun downloadedFileDao(): DownloadedFileDao
}
