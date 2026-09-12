package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dub_history")
data class DubHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val platform: String, // TikTok, Instagram, Kwai
    val url: String,
    val originalLanguage: String,
    val targetLanguage: String,
    val translatedTitle: String,
    val translatedScript: String,
    val voicePersona: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
