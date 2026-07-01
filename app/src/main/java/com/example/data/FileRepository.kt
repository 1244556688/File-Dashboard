package com.example.data

import kotlinx.coroutines.flow.Flow

class FileRepository(private val database: AppDatabase) {
    private val scannedFileDao = database.scannedFileDao()
    private val feedbackRuleDao = database.feedbackRuleDao()

    val allFiles: Flow<List<ScannedFile>> = scannedFileDao.getAllFiles()
    val allRules: Flow<List<FeedbackRule>> = feedbackRuleDao.getAllRules()

    fun getFilesBySemanticClass(semanticClass: String): Flow<List<ScannedFile>> {
        return scannedFileDao.getFilesBySemanticClass(semanticClass)
    }

    fun getFilesByBaseType(baseType: String): Flow<List<ScannedFile>> {
        return scannedFileDao.getFilesByBaseType(baseType)
    }

    suspend fun getFileByPath(path: String): ScannedFile? {
        return scannedFileDao.getFileByPath(path)
    }

    suspend fun insertFile(file: ScannedFile) {
        scannedFileDao.insertFile(file)
    }

    suspend fun insertFiles(files: List<ScannedFile>) {
        scannedFileDao.insertFiles(files)
    }

    suspend fun deleteFileByPath(path: String) {
        scannedFileDao.deleteFileByPath(path)
    }

    suspend fun clearAllFiles() {
        scannedFileDao.clearAll()
    }

    suspend fun insertRule(rule: FeedbackRule) {
        feedbackRuleDao.insertRule(rule)
    }

    suspend fun deleteRule(rule: FeedbackRule) {
        feedbackRuleDao.deleteRule(rule)
    }

    suspend fun deleteRuleById(id: Int) {
        feedbackRuleDao.deleteRuleById(id)
    }

    suspend fun clearAllRules() {
        feedbackRuleDao.clearAll()
    }
}
