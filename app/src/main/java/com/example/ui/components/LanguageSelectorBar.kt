package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val POPULAR_LANGUAGES = listOf(
    "Auto Detect", "English", "Croatian", "Japanese", "Spanish", "French", "German",
    "Chinese", "Korean", "Russian", "Arabic", "Italian", "Portuguese"
)

val ALL_SUPPORTED_LANGUAGES = listOf(
    "English", "Croatian", "Spanish", "French", "German", "Japanese",
    "Chinese", "Korean", "Russian", "Arabic", "Italian", "Portuguese",
    "Dutch", "Polish", "Turkish", "Vietnamese", "Indonesian", "Czech",
    "Swedish", "Greek", "Hindi", "Thai"
)

@Composable
fun LanguageSelectorBar(
    sourceLang: String,
    targetLang: String,
    onSourceSelected: (String) -> Unit,
    onTargetSelected: (String) -> Unit,
    onSwapClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sourceExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableStateOf(0f) }

    val animatedRotation by animateFloatAsState(targetValue = rotationAngle, label = "swap_rotate")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Source Language Selector
            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { sourceExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("source_lang_selector"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = sourceLang,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Source Language",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = sourceExpanded,
                    onDismissRequest = { sourceExpanded = false }
                ) {
                    POPULAR_LANGUAGES.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang, fontSize = 14.sp) },
                            onClick = {
                                onSourceSelected(lang)
                                sourceExpanded = false
                            }
                        )
                    }
                }
            }

            // Swap Button
            IconButton(
                onClick = {
                    rotationAngle += 180f
                    onSwapClicked()
                },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .testTag("swap_lang_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                    contentDescription = "Swap Languages",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.rotate(animatedRotation)
                )
            }

            // Target Language Selector
            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { targetExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("target_lang_selector"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = targetLang,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Target Language",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = targetExpanded,
                    onDismissRequest = { targetExpanded = false }
                ) {
                    POPULAR_LANGUAGES.filter { it != "Auto Detect" }.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang, fontSize = 14.sp) },
                            onClick = {
                                onTargetSelected(lang)
                                targetExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
