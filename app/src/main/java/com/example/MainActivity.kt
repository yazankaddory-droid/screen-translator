package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.TranslationEntity
import com.example.ui.MainViewModel
import com.example.ui.ScreenScenario
import com.example.ui.components.ALL_SUPPORTED_LANGUAGES
import com.example.ui.components.LanguageSelectorBar
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.ElectricAccent
import com.example.ui.theme.GlowGreen
import com.example.ui.theme.ScreenTranslatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScreenTranslatorTheme {
                ScreenTranslatorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTranslatorApp(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyList by viewModel.translationHistory.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Screen Translator",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (uiState.isFloatingServiceActive) "● Service Active" else "○ Service Idle",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.isFloatingServiceActive) GlowGreen else Color.Gray
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Floating Service",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = uiState.isFloatingServiceActive,
                            onCheckedChange = { viewModel.toggleFloatingService() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = CyanPrimary
                            ),
                            modifier = Modifier.testTag("floating_service_switch")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.Default.Layers, contentDescription = "Screen Translator") },
                    label = { Text("Translator") }
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Default.Block, contentDescription = "Excluded Languages") },
                    label = { Text("Excluded") }
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                0 -> ScreenTranslatorView(viewModel = viewModel)
                1 -> ExcludedLanguagesView(viewModel = viewModel)
                2 -> TranslationHistoryView(viewModel = viewModel, historyList = historyList)
                3 -> TranslatorSettingsView(viewModel = viewModel)
            }

            // Onboarding Setup Instructions Dialog
            if (uiState.showOnboarding) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissOnboarding() },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = CyanPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Screen Translation Setup", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "How live screen translation works:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )

                            Row(verticalAlignment = Alignment.Top) {
                                Text("1. ", color = CyanPrimary, fontWeight = FontWeight.Bold)
                                Text("Tap 'TRANSLATE FULL SCREEN' or start Broadcast from Control Center / Floating Service button.", style = MaterialTheme.typography.bodySmall)
                            }

                            Row(verticalAlignment = Alignment.Top) {
                                Text("2. ", color = CyanPrimary, fontWeight = FontWeight.Bold)
                                Text("A system recording indicator will appear in your status bar while active. System privacy policy requires this indicator and it cannot be hidden.", style = MaterialTheme.typography.bodySmall)
                            }

                            Row(verticalAlignment = Alignment.Top) {
                                Text("3. ", color = CyanPrimary, fontWeight = FontWeight.Bold)
                                Text("To stop screen translation, tap the red recording pill in your status bar or toggle off in Control Center.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.dismissOnboarding() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("OK, Got It", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Translation Result Dialog Modal
            uiState.lastTranslationResult?.let { result ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissResultDialog() },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = CyanPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Translation Overlay Result", style = MaterialTheme.typography.titleMedium)
                        }
                    },
                    text = {
                        Column {
                            Text(
                                text = "Original (${result.sourceLang}):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = result.originalText,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Translated (${result.targetLang}):",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanPrimary
                            )
                            Text(
                                text = result.translatedText,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        CyanPrimary.copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(1.dp, CyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.dismissResultDialog() }) {
                            Text("Close", color = CyanPrimary)
                        }
                    },
                    dismissButton = {
                        IconButton(onClick = {
                            viewModel.toggleSavedState(result.id, result.isSaved)
                        }) {
                            Icon(
                                imageVector = if (result.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save",
                                tint = CyanPrimary
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ExcludedLanguagesView(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val excludedList = uiState.excludedLanguages.toList().sorted()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Excluded Languages",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Languages that will not be translated because you already understand them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Prominent Add Language Button
        Button(
            onClick = { viewModel.showAddExcludedDialog(true) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("add_language_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyanPrimary,
                contentColor = Color.Black
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Language",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (excludedList.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Excluded Languages",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "All languages on your screen will be translated. Tap 'Add Language' above to exclude languages you know.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            Text(
                text = "CURRENTLY EXCLUDED (${excludedList.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = CyanPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(excludedList, key = { it }) { lang ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("excluded_item_${lang.lowercase()}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = lang,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "Skipped during translation",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Red circular "X" removal button
                            // Circle background is lighter/less saturated red; X glyph is vivid saturated red
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE57373).copy(alpha = 0.22f))
                                    .clickable { viewModel.removeExcludedLanguage(lang) }
                                    .testTag("remove_excluded_${lang.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove $lang from excluded languages",
                                    tint = Color(0xFFFF3B30),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Language Dialog Modal
    if (uiState.showAddExcludedDialog) {
        var searchQuery by remember { mutableStateOf("") }
        val availableLanguages = ALL_SUPPORTED_LANGUAGES
            .filter { it !in uiState.excludedLanguages }
            .filter { it.contains(searchQuery.trim(), ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { viewModel.showAddExcludedDialog(false) },
            title = {
                Text(
                    text = "Add Excluded Language",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Select a language to exclude from screen translation:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search language...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_excluded_picker"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (availableLanguages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No matching languages found" else "All supported languages are already excluded",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(availableLanguages) { lang ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.addExcludedLanguage(lang)
                                            viewModel.showAddExcludedDialog(false)
                                        }
                                        .testTag("pick_lang_${lang.lowercase()}"),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = lang,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color.White
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.showAddExcludedDialog(false) }) {
                    Text("Cancel", color = CyanPrimary)
                }
            }
        )
    }
}

@Composable
fun ScreenTranslatorView(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentScenario = uiState.selectedScenario
    val screenBlocks = viewModel.sampleScreenBlocks[currentScenario] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Language Selector
        LanguageSelectorBar(
            sourceLang = uiState.sourceLanguage,
            targetLang = uiState.targetLanguage,
            onSourceSelected = { viewModel.setSourceLanguage(it) },
            onTargetSelected = { viewModel.setTargetLanguage(it) },
            onSwapClicked = { viewModel.swapLanguages() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Scenario Selector Chips
        Text(
            text = "SIMULATION CANVAS SCENARIO",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ScreenScenario.entries.toTypedArray()) { scenario ->
                val isSelected = scenario == currentScenario
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { viewModel.selectScenario(scenario) }
                        .testTag("scenario_${scenario.name.lowercase()}")
                ) {
                    Text(
                        text = scenario.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Screen Frame Simulation Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11161D)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            ) {
                // Interactive OCR Text Blocks on Screen Canvas
                screenBlocks.forEach { block ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CyanPrimary.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                CyanPrimary.copy(alpha = 0.8f)
                            ),
                            modifier = Modifier
                                .align(
                                    biasToAlignment(
                                        xBias = (block.xRatio * 2f) - 1f,
                                        yBias = (block.yRatio * 2f) - 1f
                                    )
                                )
                                .clickable { viewModel.translateTextBlock(block) }
                                .padding(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = block.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Tap to translate OCR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanPrimary
                                )
                            }
                        }
                    }
                }

                if (uiState.isTranslating) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = CyanPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Extracting Screen Text with AI OCR...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Floating Action Translate Button
        Button(
            onClick = { viewModel.translateWholeScreen() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("translate_screen_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyanPrimary,
                contentColor = Color.Black
            )
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "TRANSLATE FULL SCREEN",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

private fun biasToAlignment(xBias: Float, yBias: Float): Alignment {
    return BiasAlignment(xBias.coerceIn(-1f, 1f), yBias.coerceIn(-1f, 1f))
}

@Composable
fun TranslationHistoryView(viewModel: MainViewModel, historyList: List<TranslationEntity>) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search translations...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val categories = listOf("All", "Screen", "Game", "Manga", "Document", "Chat")
            items(categories) { cat ->
                val isSelected = cat == uiState.selectedCategoryFilter
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) ElectricAccent else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { viewModel.setCategoryFilter(cat) }
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No saved translations found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(historyList, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.sourceLang} → ${item.targetLang} (${item.category})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanPrimary
                                )
                                Row {
                                    IconButton(
                                        onClick = { viewModel.toggleSavedState(item.id, item.isSaved) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (item.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Bookmark",
                                            tint = CyanPrimary
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteTranslation(item.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.originalText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.translatedText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TranslatorSettingsView(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Overlay & Accessibility Settings",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Floating Service Button", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = uiState.isFloatingServiceActive,
                        onCheckedChange = { viewModel.toggleFloatingService() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Text-to-Speech (TTS) Reader", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = uiState.isTtsPlaying,
                        onCheckedChange = { viewModel.toggleTts() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setTab(1) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Excluded Languages", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${uiState.excludedLanguages.size} language(s) excluded",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanPrimary
                        )
                    }
                    Text(
                        text = "Manage →",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = CyanPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Overlay Style Mode",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))

        val styles = listOf("Subtitles", "Inline Replacement", "Floating Card")
        styles.forEach { style ->
            val isSelected = style == uiState.overlayTranslationStyle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, CyanPrimary) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.setOverlayStyle(style) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = style,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
