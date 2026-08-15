package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("screen_translator_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_EXCLUDED_LANGS = "key_excluded_languages"
        private const val KEY_SOURCE_LANG = "key_source_language"
        private const val KEY_TARGET_LANG = "key_target_language"
        private const val KEY_FLOATING_SERVICE = "key_floating_service"
        private const val KEY_SHOW_ONBOARDING = "key_show_onboarding"
        private const val KEY_OVERLAY_STYLE = "key_overlay_style"
        private const val KEY_TTS_PLAYING = "key_tts_playing"
    }

    fun getExcludedLanguages(): Set<String> {
        val raw = prefs.getStringSet(KEY_EXCLUDED_LANGS, null)
        return raw?.toSet() ?: setOf("English")
    }

    fun setExcludedLanguages(languages: Set<String>) {
        prefs.edit().putStringSet(KEY_EXCLUDED_LANGS, languages).apply()
    }

    fun addExcludedLanguage(language: String) {
        val current = getExcludedLanguages().toMutableSet()
        current.add(language)
        setExcludedLanguages(current)
    }

    fun removeExcludedLanguage(language: String) {
        val current = getExcludedLanguages().toMutableSet()
        current.remove(language)
        setExcludedLanguages(current)
    }

    fun isLanguageExcluded(language: String): Boolean {
        if (language.equals("Auto Detect", ignoreCase = true)) return false
        val excluded = getExcludedLanguages()
        return excluded.any { it.equals(language.trim(), ignoreCase = true) }
    }

    fun getSourceLanguage(): String = prefs.getString(KEY_SOURCE_LANG, "Auto Detect") ?: "Auto Detect"
    fun setSourceLanguage(lang: String) = prefs.edit().putString(KEY_SOURCE_LANG, lang).apply()

    fun getTargetLanguage(): String = prefs.getString(KEY_TARGET_LANG, "English") ?: "English"
    fun setTargetLanguage(lang: String) = prefs.edit().putString(KEY_TARGET_LANG, lang).apply()

    fun isFloatingServiceActive(): Boolean = prefs.getBoolean(KEY_FLOATING_SERVICE, true)
    fun setFloatingServiceActive(active: Boolean) = prefs.edit().putBoolean(KEY_FLOATING_SERVICE, active).apply()

    fun isShowOnboarding(): Boolean = prefs.getBoolean(KEY_SHOW_ONBOARDING, true)
    fun setShowOnboarding(show: Boolean) = prefs.edit().putBoolean(KEY_SHOW_ONBOARDING, show).apply()

    fun getOverlayStyle(): String = prefs.getString(KEY_OVERLAY_STYLE, "Subtitles") ?: "Subtitles"
    fun setOverlayStyle(style: String) = prefs.edit().putString(KEY_OVERLAY_STYLE, style).apply()

    fun isTtsPlaying(): Boolean = prefs.getBoolean(KEY_TTS_PLAYING, false)
    fun setTtsPlaying(playing: Boolean) = prefs.edit().putBoolean(KEY_TTS_PLAYING, playing).apply()
}
