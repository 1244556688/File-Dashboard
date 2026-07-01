package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feedback_rules")
data class FeedbackRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pattern: String,         // e.g., "xyz", "note", "Minecraft"
    val matchType: String,       // "EXTENSION", "NAME_CONTAINS", "PATH_CONTAINS"
    val targetBaseType: String,
    val targetSemanticClass: String,
    val targetAiPurpose: String,
    val confidence: Int = 100,
    val createdTime: Long = System.currentTimeMillis()
)
