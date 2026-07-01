package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.File
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClassifier {
    private const val TAG = "GeminiClassifier"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Performs semantic analysis of the file. If Gemini is available, uses Gemini API.
     * Otherwise, falls back to local rules & heuristics.
     */
    suspend fun classifyFile(
        fileName: String,
        filePath: String,
        fileSize: Long,
        mimeType: String?
    ): FileClassificationResult {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "Gemini API Key is missing or placeholder. Falling back to local rules.")
            return classifyLocal(fileName, filePath, fileSize, mimeType)
        }

        try {
            val extension = fileName.substringAfterLast('.', "")
            val prompt = """
                Analyze this file and classify its semantics.
                
                File Name: $fileName
                File Path: $filePath
                File Size: $fileSize bytes
                Mime Type: $mimeType
                File Extension: $extension
                
                Respond strictly with a JSON object in this format (no markdown, no backticks, no text wrappers, just pure JSON):
                {
                  "baseType": "IMAGE" | "VIDEO" | "DOCUMENT" | "APK" | "AUDIO" | "ARCHIVE" | "UNKNOWN",
                  "semanticClass": "STUDY" | "WORK" | "GAMING" | "SCREENSHOT" | "CODE" | "JUNK" | "OTHER",
                  "aiPurpose": "Short 3-8 word description of inferred purpose",
                  "confidence": 0-100 (integer representing confidence percentage),
                  "probabilities": "Guess 1: X%, Guess 2: Y%, Guess 3: Z% (Include this only for UNKNOWN types, provide 3 guesses)"
                }
                
                Rules:
                - baseType must match one of the seven exact strings.
                - semanticClass must match one of the seven exact strings.
                - If the file type is UNKNOWN (e.g. unknown extension, no extension, mixed/custom format), explain what it might be under 'probabilities' (e.g., "Developer Backup: 65%, Archive Variant: 25%, Unknown Binary: 10%").
                - Be smart. Infer from keywords (e.g., 'minecraft' means GAMING/Minecraft Records, 'invoice' means WORK/Business Documents, 'note1' means STUDY/Class Note).
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(
                    temperature = 0.2f,
                    responseMimeType = "application/json"
                ),
                systemInstruction = Content(parts = listOf(Part(text = "You are an advanced Android AI File understanding agent. You output clean, valid, structured JSON classifications of files.")))
            )

            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                // Parse result
                val cleanJson = jsonText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()
                val resultAdapter = moshi.adapter(FileClassificationResult::class.java)
                val result = resultAdapter.fromJson(cleanJson)
                if (result != null) {
                    return result
                }
            }
            throw Exception("Failed to extract JSON from Gemini response")
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call failed, falling back to local rule-classifier: ${e.message}", e)
            return classifyLocal(fileName, filePath, fileSize, mimeType)
        }
    }

    /**
     * Sophisticated local heuristic rules to classify files offline or when API key is not present.
     */
    fun classifyLocal(
        fileName: String,
        filePath: String,
        fileSize: Long,
        mimeType: String?
    ): FileClassificationResult {
        val nameLower = fileName.lowercase()
        val pathLower = filePath.lowercase()
        val extension = fileName.substringAfterLast('.', "").lowercase()

        // 1. Inferred Base Type from extension/mime
        val baseType = when {
            extension in listOf("png", "jpg", "jpeg", "gif", "webp", "bmp", "heic") || mimeType?.startsWith("image/") == true -> "IMAGE"
            extension in listOf("mp4", "mkv", "avi", "mov", "3gp", "webm", "flv") || mimeType?.startsWith("video/") == true -> "VIDEO"
            extension in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "epub", "md", "csv") || mimeType?.startsWith("text/") == true -> "DOCUMENT"
            extension == "apk" -> "APK"
            extension in listOf("mp3", "wav", "ogg", "m4a", "flac", "aac", "mid") || mimeType?.startsWith("audio/") == true -> "AUDIO"
            extension in listOf("zip", "rar", "7z", "tar", "gz", "tgz", "bz2") || mimeType?.contains("zip") == true || mimeType?.contains("archive") == true -> "ARCHIVE"
            else -> "UNKNOWN"
        }

        // 2. Semantic Class & Purpose Inference
        var semanticClass = "OTHER"
        var aiPurpose = "General local user asset"
        var confidence = 70
        var probabilities: String? = null

        when {
            // Screen captures
            nameLower.contains("screenshot") || nameLower.contains("ss_") || nameLower.contains("cap_") -> {
                semanticClass = "SCREENSHOT"
                aiPurpose = if (nameLower.contains("minecraft") || nameLower.contains("game")) {
                    "Game play screen capture"
                } else {
                    "System screen capture / Image record"
                }
                confidence = 95
            }
            // Gaming
            nameLower.contains("minecraft") || nameLower.contains("mcworld") || nameLower.contains("steam") ||
                    nameLower.contains("game") || nameLower.contains("save") || nameLower.contains("play") ||
                    extension in listOf("mcpack", "mctemplate", "mcworld", "sav", "gamedata") -> {
                semanticClass = "GAMING"
                aiPurpose = when {
                    extension == "mcworld" -> "Minecraft World Backup File"
                    nameLower.contains("backup") -> "Game save backup copy"
                    else -> "Gaming record / play assets"
                }
                confidence = 92
            }
            // Code & Development
            extension in listOf("kt", "kts", "py", "java", "cpp", "c", "h", "html", "js", "css", "json", "xml", "sh", "bat", "gradle", "properties") ||
                    nameLower.contains("main") || nameLower.contains("test") || nameLower.contains("build") || nameLower.contains("src") -> {
                semanticClass = "CODE"
                aiPurpose = when (extension) {
                    "kt", "kts" -> "Kotlin Source / Build Script"
                    "py" -> "Python Automation Script"
                    "json", "xml" -> "Structured Software Config"
                    else -> "Development code source module"
                }
                confidence = 95
            }
            // Work / Business
            nameLower.contains("invoice") || nameLower.contains("bill") || nameLower.contains("salary") ||
                    nameLower.contains("schedule") || nameLower.contains("work") || nameLower.contains("resume") ||
                    nameLower.contains("cv") || nameLower.contains("brief") || nameLower.contains("budget") ||
                    nameLower.contains("report") || nameLower.contains("meeting") || nameLower.contains("proposal") -> {
                semanticClass = "WORK"
                aiPurpose = when {
                    nameLower.contains("invoice") || nameLower.contains("bill") -> "Financial record / Invoice sheet"
                    nameLower.contains("resume") || nameLower.contains("cv") -> "Career resume bio dossier"
                    else -> "Work project report / Plan schedule"
                }
                confidence = 88
            }
            // Education & Study
            nameLower.contains("homework") || nameLower.contains("lecture") || nameLower.contains("slide") ||
                    nameLower.contains("study") || nameLower.contains("note") || nameLower.contains("class") ||
                    nameLower.contains("syllabus") || nameLower.contains("assignment") || nameLower.contains("exam") ||
                    nameLower.contains("tutorial") || nameLower.contains("course") || extension == "epub" -> {
                semanticClass = "STUDY"
                aiPurpose = when {
                    nameLower.contains("note") -> "Education classroom study notes"
                    nameLower.contains("homework") || nameLower.contains("assignment") -> "Academic course assignment"
                    else -> "Reference learning tutorial document"
                }
                confidence = 90
            }
            // Junk / Temp
            nameLower.contains("temp") || nameLower.contains("cache") || nameLower.contains("junk") ||
                    nameLower.contains("trash") || nameLower.contains("deleted") ||
                    extension in listOf("tmp", "log", "bak", "cache", "chk") -> {
                semanticClass = "JUNK"
                aiPurpose = "Temporary junk file / Log output / System cache"
                confidence = 85
            }
        }

        // 3. Special handling for UNKNOWN baseType
        if (baseType == "UNKNOWN") {
            aiPurpose = "Custom binary data or backup container"
            confidence = 65
            probabilities = when {
                nameLower.contains("backup") || nameLower.contains("archive") || extension == "bak" -> {
                    "Dev Backup: 65%, Custom Archive: 25%, Raw Binary: 10%"
                }
                extension == "dat" || extension == "bin" || extension == "sys" -> {
                    "Database Store: 50%, Software Cache: 30%, App Configuration: 20%"
                }
                else -> {
                    "Proprietary Database: 45%, System Backup: 35%, Encrypted Stream: 20%"
                }
            }
        }

        return FileClassificationResult(
            baseType = baseType,
            semanticClass = semanticClass,
            aiPurpose = aiPurpose,
            confidence = confidence,
            probabilities = probabilities
        )
    }
}
