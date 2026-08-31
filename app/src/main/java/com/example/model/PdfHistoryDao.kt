package com.example.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(history: PdfHistoryEntity)

    @Query("SELECT * FROM pdf_history ORDER BY lastAccessed DESC")
    fun getAllHistory(): Flow<List<PdfHistoryEntity>>
}
