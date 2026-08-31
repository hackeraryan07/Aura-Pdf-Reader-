package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdf_history")
data class PdfHistoryEntity(
    @PrimaryKey val uriString: String,
    val name: String,
    val size: String,
    val lastAccessed: Long
)
