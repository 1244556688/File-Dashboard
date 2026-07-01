package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_files")
data class ScannedFile(
    @PrimaryKey val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String?,
    val addedDate: Long,
    val baseType: String,      // IMAGE, VIDEO, DOCUMENT, APK, AUDIO, ARCHIVE, UNKNOWN
    val semanticClass: String, // STUDY, WORK, GAMING, SCREENSHOT, CODE, JUNK, OTHER
    val aiPurpose: String,     // e.g. "Class notes / Lecture text", "Minecraft World Backup"
    val confidence: Int,       // 0 to 100
    val lastScanned: Long = System.currentTimeMillis(),
    val userCorrected: Boolean = false,
    val probabilities: String? = null // For unknown files: "Backup:65%,Archive:25%,Binary:10%"
)
