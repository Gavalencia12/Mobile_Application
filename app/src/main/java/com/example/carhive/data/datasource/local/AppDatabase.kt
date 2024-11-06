package com.example.carhive.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.carhive.data.model.DownloadedFileEntity
import com.example.carhive.data.repository.DownloadedFileDao

@Database(entities = [DownloadedFileEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadedFileDao(): DownloadedFileDao
}
