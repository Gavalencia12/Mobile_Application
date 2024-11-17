package com.example.carhive.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.carhive.data.model.DownloadedFileEntity

@Dao
interface DownloadedFileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(downloadedFile: DownloadedFileEntity)

    @Query("SELECT * FROM downloaded_files WHERE fileHash = :fileHash LIMIT 1")
    suspend fun getFileByHash(fileHash: String): DownloadedFileEntity?

    @Query("SELECT * FROM downloaded_files")
    suspend fun getAllFiles(): List<DownloadedFileEntity>

    @Query("DELETE FROM downloaded_files WHERE fileHash = :fileHash")
    suspend fun deleteFileByHash(fileHash: String)
}

