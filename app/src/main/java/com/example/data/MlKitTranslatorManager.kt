package com.example.data

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * On-Device Real ML Kit Translation Manager.
 * Manages model downloading and translation clients for all supported languages,
 * including Croatian, Japanese, Spanish, French, German, Chinese, English, etc.
 */
class MlKitTranslatorManager {

    companion object {
        private const val TAG = "MlKitTranslatorManager"
        @Volatile
        private var INSTANCE: MlKitTranslatorManager? = null

        fun getInstance(): MlKitTranslatorManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MlKitTranslatorManager().also { INSTANCE = it }
            }
        }
    }

    private val translatorCache = ConcurrentHashMap<String, Translator>()

    /**
     * Translates text on-device using Google ML Kit.
     * All languages, including Croatian ("hr"), go through the real ML Kit on-device model pipeline.
     */
    suspend fun translateText(
        text: String,
        sourceLang: String,
        targetLang: String
    ): String = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext text

        val sourceTag = resolveLanguageCode(sourceLang)
        val targetTag = resolveLanguageCode(targetLang)

        if (sourceTag == targetTag) {
            return@withContext text
        }

        val translator = getOrCreateTranslator(sourceTag, targetTag)

        // Ensure model is downloaded
        ensureModelDownloaded(translator)

        // Perform translation via ML Kit
        suspendCancellableCoroutine { continuation ->
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    if (continuation.isActive) {
                        continuation.resume(translatedText)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "ML Kit translation failed for $sourceTag -> $targetTag", exception)
                    if (continuation.isActive) {
                        continuation.resumeWithException(exception)
                    }
                }
        }
    }

    private suspend fun ensureModelDownloaded(translator: Translator) {
        val conditions = DownloadConditions.Builder().build()
        suspendCancellableCoroutine<Unit> { continuation ->
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to download/verify ML Kit model", exception)
                    if (continuation.isActive) {
                        continuation.resumeWithException(exception)
                    }
                }
        }
    }

    private fun getOrCreateTranslator(sourceTag: String, targetTag: String): Translator {
        val key = "${sourceTag}_$targetTag"
        return translatorCache.computeIfAbsent(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceTag)
                .setTargetLanguage(targetTag)
                .build()
            Translation.getClient(options)
        }
    }

    /**
     * Resolves human-readable language names or codes to standard ML Kit TranslateLanguage BCP-47 tags.
     */
    fun resolveLanguageCode(language: String): String {
        return when (language.trim().lowercase()) {
            "croatian", "hr" -> TranslateLanguage.CROATIAN
            "english", "en", "auto detect" -> TranslateLanguage.ENGLISH
            "spanish", "es" -> TranslateLanguage.SPANISH
            "french", "fr" -> TranslateLanguage.FRENCH
            "german", "de" -> TranslateLanguage.GERMAN
            "japanese", "ja" -> TranslateLanguage.JAPANESE
            "chinese", "zh" -> TranslateLanguage.CHINESE
            "korean", "ko" -> TranslateLanguage.KOREAN
            "russian", "ru" -> TranslateLanguage.RUSSIAN
            "arabic", "ar" -> TranslateLanguage.ARABIC
            "italian", "it" -> TranslateLanguage.ITALIAN
            "portuguese", "pt" -> TranslateLanguage.PORTUGUESE
            "dutch", "nl" -> TranslateLanguage.DUTCH
            "polish", "pl" -> TranslateLanguage.POLISH
            "turkish", "tr" -> TranslateLanguage.TURKISH
            "vietnamese", "vi" -> TranslateLanguage.VIETNAMESE
            "indonesian", "id" -> TranslateLanguage.INDONESIAN
            "czech", "cs" -> TranslateLanguage.CZECH
            "swedish", "sv" -> TranslateLanguage.SWEDISH
            "greek", "el" -> TranslateLanguage.GREEK
            "hindi", "hi" -> TranslateLanguage.HINDI
            "thai", "th" -> TranslateLanguage.THAI
            else -> {
                val tag = TranslateLanguage.fromLanguageTag(language.trim().lowercase())
                tag ?: TranslateLanguage.ENGLISH
            }
        }
    }

    fun closeAll() {
        translatorCache.values.forEach { it.close() }
        translatorCache.clear()
    }
}
