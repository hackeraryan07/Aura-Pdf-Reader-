package com.example.model

import android.net.Uri

data class PdfFile(
    val id: Long,
    val name: String,
    val size: String,
    val date: String,
    val uri: Uri,
    val folderName: String = "Unknown"
)
