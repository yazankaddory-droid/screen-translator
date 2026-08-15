package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history")
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSaved: Boolean = false,
    val category: String = "Screen", // Screen, Game, Manga, Document, Chat
    val confidenceScore: Float = 0.95f
)
