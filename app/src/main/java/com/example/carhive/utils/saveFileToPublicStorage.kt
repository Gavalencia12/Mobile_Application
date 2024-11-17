package com.example.carhive.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.IOException

fun saveFileToMediaStorage(context: Context, fileBytes: ByteArray, fileType: String, fileHash: String, fileName: String): Uri? {
    val directory = when {
        fileType.startsWith("image/") -> "CarHive/Media/Images"
        fileType.startsWith("video/") -> "CarHive/Media/Videos"
        else -> "CarHive/Media/Documents"
    }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentResolver: ContentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, fileType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Android/media/${context.packageName}/$directory")
        }

        val uri: Uri? = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        uri?.let {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(fileBytes)
                }
                Log.d("FileDownload", "Archivo guardado exitosamente en: $uri")
            } catch (e: IOException) {
                Log.e("FileDownloadError", "Error al guardar el archivo: ${e.message}", e)
                return null
            }
        }
        uri
    } else {
        val mediaDir = File(context.getExternalFilesDir(null)?.parentFile, "media/${context.packageName}/$directory")
        if (!mediaDir.exists()) mediaDir.mkdirs()

        val file = File(mediaDir, fileName)
        return try {
            file.outputStream().use { outputStream ->
                outputStream.write(fileBytes)
            }
            Log.d("FileDownload", "Archivo guardado exitosamente en: ${Uri.fromFile(file)}")
            Uri.fromFile(file)
        } catch (e: IOException) {
            Log.e("FileDownloadError", "Error al guardar el archivo: ${e.message}", e)
            null
        }
    }
}
