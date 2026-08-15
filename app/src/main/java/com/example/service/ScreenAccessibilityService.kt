package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.AppDatabase
import com.example.data.MlKitTranslatorManager
import com.example.data.SettingsRepository
import com.example.data.TranslationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Screen Accessibility Service for reading on-screen content and translating in real-time.
 * Automatically skips translation for any language present in the user's Excluded Languages list.
 */
class ScreenAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var settingsRepository: SettingsRepository
    private val translationService = MlKitTranslatorManager.getInstance()

    companion object {
        private const val TAG = "ScreenAccessibility"
        var isRunning: Boolean = false
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        settingsRepository = SettingsRepository(applicationContext)
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        serviceInfo = info
        Log.d(TAG, "ScreenAccessibilityService connected and configured.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!::settingsRepository.isInitialized) {
            settingsRepository = SettingsRepository(applicationContext)
        }

        if (!settingsRepository.isFloatingServiceActive()) {
            return
        }

        val rootNode = rootInActiveWindow ?: return
        val extractedTexts = mutableListOf<String>()
        extractTextFromNode(rootNode, extractedTexts)

        if (extractedTexts.isEmpty()) return

        val combinedText = extractedTexts.take(5).joinToString(" ")
        processScreenText(combinedText)
    }

    /**
     * Inspects extracted text, checks against full Excluded Languages list, and translates if not excluded.
     */
    fun processScreenText(text: String, detectedLangOverride: String? = null) {
        if (text.isBlank()) return

        val targetLang = settingsRepository.getTargetLanguage()
        val detectedLang = detectedLangOverride ?: detectLanguageSimple(text)
        val excludedLanguages = settingsRepository.getExcludedLanguages()

        // Check against the full excluded languages list
        val isExcluded = excludedLanguages.any { excluded ->
            excluded.equals(detectedLang, ignoreCase = true) ||
            (excluded.equals("English", ignoreCase = true) && detectedLang.equals("en", ignoreCase = true)) ||
            (excluded.equals("Croatian", ignoreCase = true) && detectedLang.equals("hr", ignoreCase = true)) ||
            (excluded.equals("Spanish", ignoreCase = true) && detectedLang.equals("es", ignoreCase = true)) ||
            (excluded.equals("French", ignoreCase = true) && detectedLang.equals("fr", ignoreCase = true)) ||
            (excluded.equals("German", ignoreCase = true) && detectedLang.equals("de", ignoreCase = true)) ||
            (excluded.equals("Japanese", ignoreCase = true) && detectedLang.equals("ja", ignoreCase = true)) ||
            (excluded.equals("Chinese", ignoreCase = true) && detectedLang.equals("zh", ignoreCase = true))
        }

        if (isExcluded) {
            Log.d(TAG, "Skipping translation: Language '$detectedLang' is in the Excluded Languages list ($excludedLanguages)")
            return
        }

        // Proceed with on-device translation
        serviceScope.launch {
            try {
                val translated = translationService.translateText(
                    text = text,
                    sourceLang = detectedLang,
                    targetLang = targetLang
                )
                Log.d(TAG, "Translated text ($detectedLang -> $targetLang): $translated")

                val dao = AppDatabase.getDatabase(applicationContext).translationDao()
                dao.insertTranslation(
                    TranslationEntity(
                        originalText = text,
                        translatedText = translated,
                        sourceLang = detectedLang,
                        targetLang = targetLang,
                        category = "Screen"
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in translation processing", e)
            }
        }
    }

    private fun detectLanguageSimple(text: String): String {
        val hasJapanese = text.any { it in '\u3040'..'\u30ff' || it in '\u4e00'..'\u9faf' }
        if (hasJapanese) return "Japanese"

        val hasKorean = text.any { it in '\uac00'..'\ud7af' }
        if (hasKorean) return "Korean"

        val hasCyrillic = text.any { it in '\u0400'..'\u04ff' }
        if (hasCyrillic) return "Russian"

        val hasArabic = text.any { it in '\u0600'..'\u06ff' }
        if (hasArabic) return "Arabic"

        val lower = text.lowercase()
        if (lower.contains("¡") || lower.contains("¿") || lower.contains("hola") || lower.contains("amigo")) {
            return "Spanish"
        }
        if (lower.contains("bonjour") || lower.contains("merci") || lower.contains("supérieur")) {
            return "French"
        }
        if (lower.contains("danke") || lower.contains("aufstieg") || lower.contains("schwert")) {
            return "German"
        }
        if (lower.contains("bok") || lower.contains("hvala") || lower.contains("razina") || lower.contains("zadatak") || lower.contains("junače")) {
            return "Croatian"
        }

        return "English"
    }

    private fun extractTextFromNode(node: AccessibilityNodeInfo?, result: MutableList<String>) {
        if (node == null) return
        val text = node.text?.toString()
        if (!text.isNullOrBlank()) {
            result.add(text.trim())
        }
        for (i in 0 until node.childCount) {
            extractTextFromNode(node.getChild(i), result)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "ScreenAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }
}
