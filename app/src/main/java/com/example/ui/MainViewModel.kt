package com.example.ui

import android.app.Application
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SettingsRepository
import com.example.data.TranslationEntity
import com.example.data.TranslationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ScreenTextBlock(
    val id: String,
    val text: String,
    val xRatio: Float, // 0..1 relative position on screen
    val yRatio: Float,
    val widthRatio: Float,
    val heightRatio: Float,
    val category: String = "Game"
)

enum class ScreenScenario(val title: String, val category: String, val sampleLang: String) {
    GAME_RPG("Fantasy RPG Battle", "Game", "Japanese"),
    MANGA("Manga Speech Bubble", "Manga", "Japanese"),
    RESTAURANT_MENU("Tokyo Restaurant Menu", "Document", "Japanese"),
    CHAT_APP("Global Friend Chat", "Chat", "Spanish")
}

data class ScreenTranslatorUiState(
    val selectedTab: Int = 0, // 0: Screen Translator, 1: Excluded Languages, 2: History, 3: Settings
    val isFloatingServiceActive: Boolean = true,
    val showOnboarding: Boolean = true,
    val sourceLanguage: String = "Auto Detect",
    val targetLanguage: String = "English",
    val excludedLanguages: Set<String> = emptySet(),
    val selectedScenario: ScreenScenario = ScreenScenario.GAME_RPG,
    val activeSelectionRect: Rect? = null,
    val isTranslating: Boolean = false,
    val lastTranslationResult: TranslationEntity? = null,
    val overlayTranslationStyle: String = "Subtitles", // Subtitles, Inline Replacement, Floating Card
    val isTtsPlaying: Boolean = false,
    val snackbarMessage: String? = null,
    val selectedCategoryFilter: String = "All",
    val searchQuery: String = "",
    val showAddExcludedDialog: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TranslationRepository
    private val settingsRepository: SettingsRepository

    private val _uiState: MutableStateFlow<ScreenTranslatorUiState>
    val uiState: StateFlow<ScreenTranslatorUiState>

    init {
        val dao = AppDatabase.getDatabase(application).translationDao()
        repository = TranslationRepository(dao)
        settingsRepository = SettingsRepository(application)

        // Load saved preferences from persistent storage
        val savedExcluded = settingsRepository.getExcludedLanguages()
        val savedSource = settingsRepository.getSourceLanguage()
        val savedTarget = settingsRepository.getTargetLanguage()
        val savedFloating = settingsRepository.isFloatingServiceActive()
        val savedOnboarding = settingsRepository.isShowOnboarding()
        val savedOverlay = settingsRepository.getOverlayStyle()
        val savedTts = settingsRepository.isTtsPlaying()

        _uiState = MutableStateFlow(
            ScreenTranslatorUiState(
                sourceLanguage = savedSource,
                targetLanguage = savedTarget,
                excludedLanguages = savedExcluded,
                isFloatingServiceActive = savedFloating,
                showOnboarding = savedOnboarding,
                overlayTranslationStyle = savedOverlay,
                isTtsPlaying = savedTts
            )
        )
        uiState = _uiState.asStateFlow()
    }

    private val _categoryFilter = MutableStateFlow("All")
    private val _searchQuery = MutableStateFlow("")

    val translationHistory: StateFlow<List<TranslationEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isNotBlank()) {
                repository.searchTranslations(query)
            } else {
                _categoryFilter.flatMapLatest { cat ->
                    repository.getTranslationsByCategory(cat)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val sampleScreenBlocks: Map<ScreenScenario, List<ScreenTextBlock>> = mapOf(
        ScreenScenario.GAME_RPG to listOf(
            ScreenTextBlock("g1", "レベルアップ！", 0.35f, 0.18f, 0.30f, 0.08f, "Game"),
            ScreenTextBlock("g2", "火炎剣の攻撃！", 0.20f, 0.45f, 0.60f, 0.10f, "Game"),
            ScreenTextBlock("g3", "次は何をすればいい、勇者？", 0.15f, 0.70f, 0.70f, 0.12f, "Game")
        ),
        ScreenScenario.MANGA to listOf(
            ScreenTextBlock("m1", "私に任せて！", 0.15f, 0.15f, 0.35f, 0.14f, "Manga"),
            ScreenTextBlock("m2", "絶対に勝つんだから！", 0.55f, 0.40f, 0.38f, 0.16f, "Manga"),
            ScreenTextBlock("m3", "行け、光の戦士よ！", 0.20f, 0.72f, 0.60f, 0.12f, "Manga")
        ),
        ScreenScenario.RESTAURANT_MENU to listOf(
            ScreenTextBlock("r1", "特選黒毛和牛ステーキ", 0.15f, 0.22f, 0.70f, 0.09f, "Document"),
            ScreenTextBlock("r2", "新鮮刺身盛り合わせ ¥1,800", 0.15f, 0.38f, 0.70f, 0.09f, "Document"),
            ScreenTextBlock("r3", "本日のシェフおすすめデザート", 0.15f, 0.54f, 0.70f, 0.09f, "Document")
        ),
        ScreenScenario.CHAT_APP to listOf(
            ScreenTextBlock("c1", "¡Hola amigo! ¿Qué tal tu día?", 0.10f, 0.20f, 0.65f, 0.10f, "Chat"),
            ScreenTextBlock("c2", "Estudio español para mi viaje a Madrid.", 0.25f, 0.40f, 0.65f, 0.11f, "Chat"),
            ScreenTextBlock("c3", "¡Genial! Nos vemos muy pronto.", 0.10f, 0.60f, 0.65f, 0.10f, "Chat")
        )
    )

    fun setTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    // Excluded Languages Management
    fun addExcludedLanguage(language: String) {
        if (language.isBlank() || language == "Auto Detect") return
        val current = _uiState.value.excludedLanguages.toMutableSet()
        if (current.add(language)) {
            settingsRepository.setExcludedLanguages(current)
            _uiState.value = _uiState.value.copy(
                excludedLanguages = current,
                snackbarMessage = "$language added to Excluded Languages"
            )
        }
    }

    fun removeExcludedLanguage(language: String) {
        val current = _uiState.value.excludedLanguages.toMutableSet()
        if (current.remove(language)) {
            settingsRepository.setExcludedLanguages(current)
            _uiState.value = _uiState.value.copy(
                excludedLanguages = current,
                snackbarMessage = "$language removed from Excluded Languages"
            )
        }
    }

    fun showAddExcludedDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddExcludedDialog = show)
    }

    fun toggleFloatingService() {
        val newStatus = !_uiState.value.isFloatingServiceActive
        settingsRepository.setFloatingServiceActive(newStatus)
        _uiState.value = _uiState.value.copy(
            isFloatingServiceActive = newStatus,
            snackbarMessage = if (newStatus) "Floating Translator Enabled" else "Floating Service Disabled"
        )
    }

    fun setSourceLanguage(lang: String) {
        settingsRepository.setSourceLanguage(lang)
        _uiState.value = _uiState.value.copy(sourceLanguage = lang)
    }

    fun setTargetLanguage(lang: String) {
        settingsRepository.setTargetLanguage(lang)
        _uiState.value = _uiState.value.copy(targetLanguage = lang)
    }

    fun swapLanguages() {
        val currentSource = _uiState.value.sourceLanguage
        val currentTarget = _uiState.value.targetLanguage
        if (currentSource != "Auto Detect") {
            settingsRepository.setSourceLanguage(currentTarget)
            settingsRepository.setTargetLanguage(currentSource)
            _uiState.value = _uiState.value.copy(
                sourceLanguage = currentTarget,
                targetLanguage = currentSource
            )
        }
    }

    fun selectScenario(scenario: ScreenScenario) {
        _uiState.value = _uiState.value.copy(
            selectedScenario = scenario,
            activeSelectionRect = null
        )
    }

    private fun isExcluded(language: String): Boolean {
        if (language.equals("Auto Detect", ignoreCase = true)) return false
        return _uiState.value.excludedLanguages.any { it.equals(language.trim(), ignoreCase = true) }
    }

    private fun detectLanguage(text: String, fallbackLang: String): String {
        if (_uiState.value.sourceLanguage != "Auto Detect") {
            return _uiState.value.sourceLanguage
        }
        val hasJapanese = text.any { it in '\u3040'..'\u30ff' || it in '\u4e00'..'\u9faf' }
        if (hasJapanese) return "Japanese"
        val hasKorean = text.any { it in '\uac00'..'\ud7af' }
        if (hasKorean) return "Korean"
        val hasCyrillic = text.any { it in '\u0400'..'\u04ff' }
        if (hasCyrillic) return "Russian"
        val hasArabic = text.any { it in '\u0600'..'\u06ff' }
        if (hasArabic) return "Arabic"

        val lower = text.lowercase()
        if (lower.contains("¡") || lower.contains("¿") || lower.contains("hola") || lower.contains("amigo") || lower.contains("español")) {
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
        return fallbackLang
    }

    fun translateWholeScreen() {
        viewModelScope.launch {
            val scenario = _uiState.value.selectedScenario
            val blocks = sampleScreenBlocks[scenario] ?: emptyList()
            val fullText = blocks.joinToString("\n") { it.text }

            val detectedLang = detectLanguage(fullText, scenario.sampleLang)

            // Check if detected/source language is in the excluded languages list
            if (isExcluded(detectedLang)) {
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "Skipped: $detectedLang is in your Excluded Languages list"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isTranslating = true)

            val result = repository.translateAndSave(
                originalText = fullText,
                sourceLang = detectedLang,
                targetLang = _uiState.value.targetLanguage,
                category = scenario.category
            )

            _uiState.value = _uiState.value.copy(
                isTranslating = false,
                lastTranslationResult = result,
                snackbarMessage = "Screen translated ($detectedLang → ${_uiState.value.targetLanguage})!"
            )
        }
    }

    fun translateTextBlock(block: ScreenTextBlock) {
        viewModelScope.launch {
            val scenario = _uiState.value.selectedScenario
            val detectedLang = detectLanguage(block.text, scenario.sampleLang)

            if (isExcluded(detectedLang)) {
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "Skipped: $detectedLang is in your Excluded Languages list"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isTranslating = true)
            val result = repository.translateAndSave(
                originalText = block.text,
                sourceLang = detectedLang,
                targetLang = _uiState.value.targetLanguage,
                category = block.category
            )
            _uiState.value = _uiState.value.copy(
                isTranslating = false,
                lastTranslationResult = result
            )
        }
    }

    fun translateCustomText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val detectedLang = detectLanguage(text, "English")

            if (isExcluded(detectedLang)) {
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "Skipped: $detectedLang is in your Excluded Languages list"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isTranslating = true)
            val result = repository.translateAndSave(
                originalText = text,
                sourceLang = detectedLang,
                targetLang = _uiState.value.targetLanguage,
                category = "Screen"
            )
            _uiState.value = _uiState.value.copy(
                isTranslating = false,
                lastTranslationResult = result,
                snackbarMessage = "Text translated!"
            )
        }
    }

    fun toggleSavedState(id: Long, currentState: Boolean) {
        viewModelScope.launch {
            repository.toggleSaveState(id, currentState)
        }
    }

    fun deleteTranslation(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Translation deleted")
        }
    }

    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
        _uiState.value = _uiState.value.copy(selectedCategoryFilter = category)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setOverlayStyle(style: String) {
        settingsRepository.setOverlayStyle(style)
        _uiState.value = _uiState.value.copy(overlayTranslationStyle = style)
    }

    fun toggleTts() {
        val current = _uiState.value.isTtsPlaying
        settingsRepository.setTtsPlaying(!current)
        _uiState.value = _uiState.value.copy(isTtsPlaying = !current)
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun dismissResultDialog() {
        _uiState.value = _uiState.value.copy(lastTranslationResult = null)
    }

    fun dismissOnboarding() {
        settingsRepository.setShowOnboarding(false)
        _uiState.value = _uiState.value.copy(showOnboarding = false)
    }
}
