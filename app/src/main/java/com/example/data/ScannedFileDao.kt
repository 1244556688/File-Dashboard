package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedFileDao {
    @Query("SELECT * FROM scanned_files ORDER BY addedDate DESC")
    fun getAllFiles(): Flow<List<ScannedFile>>

    @Query("SELECT * FROM scanned_files WHERE semanticClass = :semanticClass ORDER BY addedDate DESC")
    fun getFilesBySemanticClass(semanticClass: String): Flow<List<ScannedFile>>

    @Query("SELECT * FROM scanned_files WHERE baseType = :baseType ORDER BY addedDate DESC")
    fun getFilesByBaseType(baseType: String): Flow<List<ScannedFile>>

    @Query("SELECT * FROM scanned_files WHERE filePath = :path LIMIT 1")
    suspend fun getFileByPath(path: String): ScannedFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ScannedFile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<ScannedFile>)

    @Delete
    suspend fun deleteFile(file: ScannedFile)

    @Query("DELETE FROM scanned_files WHERE filePath = :path")
    suspend fun deleteFileByPath(path: String)

    @Query("DELETE FROM scanned_files")
    suspend fun clearAll()
}
