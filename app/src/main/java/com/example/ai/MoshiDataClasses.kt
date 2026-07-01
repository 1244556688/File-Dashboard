package com.example.ai

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val responseMimeType: String? = null,
    val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class FileClassificationResult(
    val baseType: String,      // IMAGE, VIDEO, DOCUMENT, APK, AUDIO, ARCHIVE, UNKNOWN
    val semanticClass: String, // STUDY, WORK, GAMING, SCREENSHOT, CODE, JUNK, OTHER
    val aiPurpose: String,     // e.g. "Lecture Note on physics"
    val confidence: Int,       // 0-100
    val probabilities: String? = null // "Backup: 65%, Archive: 25%, Binary: 10%"
)
