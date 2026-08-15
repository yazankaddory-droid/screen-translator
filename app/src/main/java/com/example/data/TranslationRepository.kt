package com.example.data

import kotlinx.coroutines.flow.Flow

class TranslationRepository(
    private val translationDao: TranslationDao,
    private val translatorManager: MlKitTranslatorManager = MlKitTranslatorManager.getInstance()
) {
    val allTranslations: Flow<List<TranslationEntity>> = translationDao.getAllTranslations()
    val savedTranslations: Flow<List<TranslationEntity>> = translationDao.getSavedTranslations()

    fun getTranslationsByCategory(category: String): Flow<List<TranslationEntity>> {
        return if (category == "All") {
            translationDao.getAllTranslations()
        } else {
            translationDao.getTranslationsByCategory(category)
        }
    }

    fun searchTranslations(query: String): Flow<List<TranslationEntity>> {
        return translationDao.searchTranslations(query)
    }

    suspend fun translateAndSave(
        originalText: String,
        sourceLang: String,
        targetLang: String,
        category: String = "Screen"
    ): TranslationEntity {
        val translated = try {
            translatorManager.translateText(
                text = originalText,
                sourceLang = sourceLang,
                targetLang = targetLang
            )
        } catch (e: Exception) {
            // Fallback gracefully to original text with indication if ML model download is pending
            "[ML Kit] $originalText"
        }

        val entity = TranslationEntity(
            originalText = originalText,
            translatedText = translated,
            sourceLang = sourceLang,
            targetLang = targetLang,
            category = category,
            confidenceScore = 0.98f
        )

        val id = translationDao.insertTranslation(entity)
        return entity.copy(id = id)
    }

    suspend fun toggleSaveState(id: Long, currentState: Boolean) {
        translationDao.updateSavedState(id, !currentState)
    }

    suspend fun delete(id: Long) {
        translationDao.deleteById(id)
    }

    suspend fun clearAll() {
        translationDao.clearAll()
    }
}
