package com.example.ui

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiClassifier
import com.example.data.FeedbackRule
import com.example.data.FileRepository
import com.example.data.ScannedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(private val repository: FileRepository) : ViewModel() {
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0)
    val scanProgress: StateFlow<Int> = _scanProgress.asStateFlow()

    private val _scanTotal = MutableStateFlow(0)
    val scanTotal: StateFlow<Int> = _scanTotal.asStateFlow()

    private val _scanResult = MutableStateFlow<String?>(null)
    val scanResult: StateFlow<String?> = _scanResult.asStateFlow()

    private val _appLanguage = MutableStateFlow(AppLanguage.ZH)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    fun loadLanguage(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val langStr = prefs.getString("language", AppLanguage.ZH.name) ?: AppLanguage.ZH.name
        _appLanguage.value = try {
            AppLanguage.valueOf(langStr)
        } catch (e: Exception) {
            AppLanguage.ZH
        }
    }

    fun setLanguage(context: Context, lang: AppLanguage) {
        _appLanguage.value = lang
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("language", lang.name).apply()
    }

    // Scanned files reactively from DB
    val scannedFiles: StateFlow<List<ScannedFile>> = repository.allFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feedback rules reactively from DB
    val feedbackRules: StateFlow<List<FeedbackRule>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        private const val TAG = "MainViewModel"
    }

    /**
     * Scan a directory selected by the user via Storage Access Framework
     */
    fun scanDirectory(context: Context, treeUri: Uri) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0
            _scanTotal.value = 0
            _scanResult.value = "Establishing contact with filesystem..."

            try {
                val resolver = context.contentResolver
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                    treeUri,
                    DocumentsContract.getTreeDocumentId(treeUri)
                )

                val fileList = mutableListOf<ScannedFileInfo>()
                resolver.query(
                    childrenUri,
                    arrayOf(
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_MIME_TYPE,
                        DocumentsContract.Document.COLUMN_SIZE,
                        DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID
                    ),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    val sizeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
                    val modIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                    val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)

                    while (cursor.moveToNext()) {
                        val name = cursor.getString(nameIndex) ?: "unknown"
                        val mime = cursor.getString(mimeIndex)
                        val size = cursor.getLong(sizeIndex)
                        val mod = cursor.getLong(modIndex)
                        val docId = cursor.getString(idIndex)

                        // Recreate a pseudo-path
                        val path = "saf://${treeUri.host}/${DocumentsContract.getTreeDocumentId(treeUri)}/$docId"
                        fileList.add(ScannedFileInfo(name, path, size, mime, mod))
                    }
                }

                if (fileList.isEmpty()) {
                    _scanResult.value = "No files found in specified sector."
                    _isScanning.value = false
                    return@launch
                }

                _scanTotal.value = fileList.size
                _scanResult.value = "Analyzing file signatures..."

                // Fetch latest rules for matching
                val currentRules = repository.allRules.first()

                val scannedResultList = mutableListOf<ScannedFile>()

                for ((index, fileInfo) in fileList.withIndex()) {
                    _scanProgress.value = index + 1
                    _scanResult.value = "Processing [${index + 1}/${fileList.size}]: ${fileInfo.name}"

                    // 1. Check user-defined rule match
                    val matchedRule = matchRules(fileInfo.name, fileInfo.path, currentRules)

                    val scannedFile = if (matchedRule != null) {
                        Log.d(TAG, "Rule matched for ${fileInfo.name}: ${matchedRule.pattern}")
                        ScannedFile(
                            filePath = fileInfo.path,
                            fileName = fileInfo.name,
                            fileSize = fileInfo.size,
                            mimeType = fileInfo.mime,
                            addedDate = fileInfo.modDate,
                            baseType = matchedRule.targetBaseType,
                            semanticClass = matchedRule.targetSemanticClass,
                            aiPurpose = matchedRule.targetAiPurpose + " (Rule Learned)",
                            confidence = matchedRule.confidence,
                            userCorrected = false // Corrected via rule, not manual edit on this item
                        )
                    } else {
                        // 2. Classify via Gemini (or Local heuristic fallback)
                        val classification = GeminiClassifier.classifyFile(
                            fileName = fileInfo.name,
                            filePath = fileInfo.path,
                            fileSize = fileInfo.size,
                            mimeType = fileInfo.mime
                        )
                        ScannedFile(
                            filePath = fileInfo.path,
                            fileName = fileInfo.name,
                            fileSize = fileInfo.size,
                            mimeType = fileInfo.mime,
                            addedDate = fileInfo.modDate,
                            baseType = classification.baseType,
                            semanticClass = classification.semanticClass,
                            aiPurpose = classification.aiPurpose,
                            confidence = classification.confidence,
                            probabilities = classification.probabilities,
                            userCorrected = false
                        )
                    }
                    scannedResultList.add(scannedFile)
                }

                // Batch save to Room
                repository.insertFiles(scannedResultList)
                _scanResult.value = "Scan Complete! Scanned ${fileList.size} files."

            } catch (e: Exception) {
                Log.e(TAG, "Error scanning sector: ${e.message}", e)
                _scanResult.value = "Scan Aborted: ${e.message}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    /**
     * Check if a file matches any of our learned feedback rules.
     */
    private fun matchRules(name: String, path: String, rules: List<FeedbackRule>): FeedbackRule? {
        val nameLower = name.lowercase()
        val ext = name.substringAfterLast('.', "").lowercase()

        for (rule in rules) {
            val patternLower = rule.pattern.lowercase()
            when (rule.matchType) {
                "EXTENSION" -> {
                    if (ext == patternLower) return rule
                }
                "NAME_CONTAINS" -> {
                    if (nameLower.contains(patternLower)) return rule
                }
                "PATH_CONTAINS" -> {
                    if (path.lowercase().contains(patternLower)) return rule
                }
            }
        }
        return null
    }

    /**
     * User submits correction feedback for a file, and learns a new rule.
     */
    fun submitFeedback(
        file: ScannedFile,
        newBaseType: String,
        newSemanticClass: String,
        newAiPurpose: String,
        learnAsRule: Boolean,
        ruleType: String = "NAME_CONTAINS" // "NAME_CONTAINS" or "EXTENSION"
    ) {
        viewModelScope.launch {
            // 1. Update file locally
            val updatedFile = file.copy(
                baseType = newBaseType,
                semanticClass = newSemanticClass,
                aiPurpose = newAiPurpose,
                confidence = 100, // Explicit user correction is 100% accurate
                userCorrected = true
            )
            repository.insertFile(updatedFile)

            // 2. If user requested to save this as a rule to apply to future scans:
            if (learnAsRule) {
                val pattern = if (ruleType == "EXTENSION") {
                    file.fileName.substringAfterLast('.', "")
                } else {
                    // Extract a keyword or use the filename itself
                    file.fileName.substringBeforeLast('.', file.fileName)
                }

                if (pattern.isNotEmpty()) {
                    val newRule = FeedbackRule(
                        pattern = pattern,
                        matchType = ruleType,
                        targetBaseType = newBaseType,
                        targetSemanticClass = newSemanticClass,
                        targetAiPurpose = newAiPurpose,
                        confidence = 100
                    )
                    repository.insertRule(newRule)
                    Log.d(TAG, "Learned new rule: $newRule")
                }
            }
        }
    }

    /**
     * Delete a single file from the scanned records
     */
    fun deleteFileRecord(file: ScannedFile) {
        viewModelScope.launch {
            repository.deleteFileByPath(file.filePath)
        }
    }

    /**
     * Delete a single learned rule
     */
    fun deleteRule(rule: FeedbackRule) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }

    /**
     * Clear all database scans to restart
     */
    fun clearAllScans() {
        viewModelScope.launch {
            repository.clearAllFiles()
            _scanResult.value = "Database cleared. Command center reset."
        }
    }

    /**
     * Populate high-fidelity simulation files for showcase or testing
     */
    fun generateDemoCorpus() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0
            _scanTotal.value = 10
            _scanResult.value = "Decrypting mainframe asset records..."

            val now = System.currentTimeMillis()
            val day = 24 * 60 * 60 * 1000L

            val demoFiles = listOf(
                ScannedFile(
                    filePath = "/storage/emulated/0/Documents/Class_Notes_Astronomy.txt",
                    fileName = "Class_Notes_Astronomy.txt",
                    fileSize = 12450L,
                    mimeType = "text/plain",
                    addedDate = now - 2 * day,
                    baseType = "DOCUMENT",
                    semanticClass = "STUDY",
                    aiPurpose = "Notes on Stellar Thermodynamics & Stars",
                    confidence = 94
                ),
                ScannedFile(
                    filePath = "/storage/emulated/0/Download/screenshot_20260615_minecraft.png",
                    fileName = "screenshot_20260615_minecraft.png",
                    fileSize = 1450200L,
                    mimeType = "image/png",
                    addedDate = now - 1 * day,
                    baseType = "IMAGE",
                    semanticClass = "GAMING",
                    aiPurpose = "Minecraft fortress build rendering",
                    confidence = 96
                ),
                ScannedFile(
                    filePath = "/storage/emulated/0/Work/Q2_invoice_google_cloud.pdf",
                    fileName = "Q2_invoice_google_cloud.pdf",
                    fileSize = 345000L,
                    mimeType = "application/pdf",
                    addedDate = now - 5 * day,
                    baseType = "DOCUMENT",
                    semanticClass = "WORK",
                    aiPurpose = "GCP infrastructure subscription receipt",
                    confidence = 95
                ),
                ScannedFile(
                    filePath = "/storage/emulated/0/Backups/minecraft_save_01.dat",
                    fileName = "minecraft_save_01.dat",
                    fileSize = 45012200L,
                    mimeType = "application/octet-stream",
                    addedDate = now - 4 * day,
                    baseType = "UNKNOWN",
                    semanticClass = "GAMING",
                    aiPurpose = "Minecraft World Save Structure",
                    confidence = 90,
                    probabilities = "Game Save Archive: 75%, Custom Binary: 15%, System Log: 10%"
                ),
                ScannedFile(
                    filePath = "/storage/emulated/0/Development/quantum_simulator.xyz",
                    fileName = "quantum_simulator.xyz",
                    fileSize = 89000L,
                    mimeType = "application/xyz",
                    addedDate = now - 10 * day,
                    baseType = "UNKNOWN",
                    semanticClass = "CODE",
                    aiPurpose = "Quantum simulation coordinates backup",
                    confidence = 65,
                    probabilities = "Developer Backup: 65%, Custom Asset: 25%, Binary Stream: 10%"
                ),
                ScannedFile(
                    filePath = "/storage/emulated/0/Download/telegram_installer.apk",
                    fileName = "telegram_installer.apk",
                    fileSize = 65120000L,
                    mimeType = "application/vnd.android.package-archive",
                    addedDate = now - 6 * day,
                    baseType = "APK",
                    semanticClass = "OTHER",
                    aiPurpose = "Telegram messaging APK package",
                    confidence = 98
                ),
                ScannedFile(
                    filePath = "/storage/emulated/0/Development/main_activity_compose.kt",
                    fileName = "main_activity_compose.kt",
                    fileSize = 4500L,
                    mimeType = "text/x-kotlin",
                    addedDate = now,
                    baseType = "DOCUMENT",
                    semanticClass = "CODE",
                    aiPurpose = "Jetpack Compose view declarations",
                    confidence = 97
                ),
                ScannedFile(
                    filePath = "/storage/emulated/0/System/garbage_dump.tmp",
                    fileName = "garbage_dump.tmp",
                    fileSize = 880400L,
                    mimeType = "application/temp",
                    addedDate = now - 12 * day,
                    baseType = "DOCUMENT",
                    semanticClass = "JUNK",
                    aiPurpose = "Temporary memory register crash dump",
                    confidence = 88
                ),
                ScannedFile(
                    filePath = "/storage/emulated/0/Music/cyberpunk_synthesizer_groove.mp3",
                    fileName = "cyberpunk_synthesizer_groove.mp3",
                    fileSize = 8400000L,
                    mimeType = "audio/mpeg",
                    addedDate = now - 3 * day,
                    baseType = "AUDIO",
                    semanticClass = "OTHER",
                    aiPurpose = "Chill synthwave beat track",
                    confidence = 92
                ),
                ScannedFile(
                    filePath = "/storage/emulated/0/Pictures/screenshot_2026_dashboard_error.png",
                    fileName = "screenshot_2026_dashboard_error.png",
                    fileSize = 852000L,
                    mimeType = "image/png",
                    addedDate = now - 2 * day,
                    baseType = "IMAGE",
                    semanticClass = "SCREENSHOT",
                    aiPurpose = "Hacker system dashboard screen grab",
                    confidence = 95
                ),
                ScannedFile(
                    filePath = "/storage/emulated/0/Documents/lecture_notes_ai_nlp.docx",
                    fileName = "lecture_notes_ai_nlp.docx",
                    fileSize = 150000L,
                    mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    addedDate = now - 8 * day,
                    baseType = "DOCUMENT",
                    semanticClass = "STUDY",
                    aiPurpose = "NLP Transformers notes",
                    confidence = 91
                ),
                ScannedFile(
                    filePath = "/storage/emulated/0/Work/q3_project_pitch_deck.pptx",
                    fileName = "q3_project_pitch_deck.pptx",
                    fileSize = 12500000L,
                    mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    addedDate = now - 14 * day,
                    baseType = "DOCUMENT",
                    semanticClass = "WORK",
                    aiPurpose = "Business pitch presentation slide",
                    confidence = 93
                )
            )

            // Wait a small bit for realism
            withContext(Dispatchers.IO) {
                repository.insertFiles(demoFiles)
            }

            _scanResult.value = "Demo Corpus Generated! Decrypted 12 cyber sectors."
            _isScanning.value = false
        }
    }
}

data class ScannedFileInfo(
    val name: String,
    val path: String,
    val size: Long,
    val mime: String?,
    val modDate: Long
)

class MainViewModelFactory(private val repository: FileRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
