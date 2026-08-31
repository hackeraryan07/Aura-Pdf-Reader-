package com.example.model

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfLoader {
    suspend fun loadPdfFiles(context: Context): List<PdfFile> = withContext(Dispatchers.IO) {
        val pdfList = mutableListOf<PdfFile>()
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val projectionList = mutableListOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATA
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            projectionList.add(MediaStore.Files.FileColumns.RELATIVE_PATH)
        }
        val projection = projectionList.toTypedArray()

        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
        val selectionArgs = arrayOf("application/pdf")
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown"
                val size = cursor.getLong(sizeColumn)
                val dateModified = cursor.getLong(dateColumn)
                val dataPath = cursor.getString(dataColumn)

                val contentUri = ContentUris.withAppendedId(collection, id)
                
                var folderName = "Unknown"
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    val relativePathColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH)
                    if (relativePathColumn != -1) {
                        val relativePath = cursor.getString(relativePathColumn)
                        if (!relativePath.isNullOrEmpty()) {
                            folderName = relativePath.trim('/').split('/').lastOrNull() ?: "Unknown"
                        }
                    }
                }
                
                if (folderName == "Unknown" || folderName.isEmpty()) {
                    folderName = try {
                        if (dataPath != null) {
                            java.io.File(dataPath).parentFile?.name ?: "Unknown"
                        } else {
                            "Unknown"
                        }
                    } catch (e: Exception) {
                        "Unknown"
                    }
                }

                val sizeStr = formatSize(size)
                val dateStr = formatDate(dateModified * 1000L)

                pdfList.add(PdfFile(id, name, sizeStr, dateStr, contentUri, folderName))
            }
        }
        pdfList
    }

    private fun formatSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()
        return java.text.DecimalFormat("#,##0.#").format(sizeInBytes / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
