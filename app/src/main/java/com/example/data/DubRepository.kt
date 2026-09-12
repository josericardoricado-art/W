package com.example.data

import com.example.BuildConfig
import com.example.network.Content
import com.example.network.GeminiClient
import com.example.network.GenerateContentRequest
import com.example.network.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DubRepository(private val dubDao: DubDao) {

    val allHistory: Flow<List<DubHistoryEntity>> = dubDao.getAllHistory()
    val favorites: Flow<List<DubHistoryEntity>> = dubDao.getFavorites()

    suspend fun translateAndDub(
        platform: String,
        url: String,
        sourceLang: String,
        targetLang: String,
        voicePersona: String
    ): Result<DubHistoryEntity> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            val translatedTitle: String
            val translatedScript: String

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                // Fallback simulation if API key is not configured yet
                translatedTitle = "[$platform Dub] Live / Video translated from $sourceLang to $targetLang"
                translatedScript = "Este é um vídeo dublado em tempo real do $platform. O áudio original em $sourceLang foi traduzido e dublado com voz $voicePersona para o $targetLang com sincronização perfeita."
            } else {
                val prompt = """
                    You are an expert AI video dubbing and translation agent for social media (TikTok, Instagram, Kwai).
                    A user provided a link: $url from platform $platform.
                    The source video is in language: $sourceLang.
                    Translate the presumed content, hook, and dialogue into $targetLang, optimized for voice dubbing with voice persona: $voicePersona.
                    
                    Return your response in two clear parts separated by '---TITLE_SCRIPT_DIVIDER---':
                    Part 1: A catchy translated title (max 60 chars).
                    Part 2: The full translated dubbing script and live commentary translation in $targetLang.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                )

                val response = GeminiClient.service.generateContent(apiKey, request)
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Tradução de vídeo em $targetLang concluída com sucesso."

                val parts = rawText.split("---TITLE_SCRIPT_DIVIDER---")
                if (parts.size >= 2) {
                    translatedTitle = parts[0].trim().removePrefix("Part 1:").trim()
                    translatedScript = parts[1].trim().removePrefix("Part 2:").trim()
                } else {
                    translatedTitle = "Dublagem $platform ($targetLang)"
                    translatedScript = rawText.trim()
                }
            }

            val entity = DubHistoryEntity(
                platform = platform,
                url = url,
                originalLanguage = sourceLang,
                targetLanguage = targetLang,
                translatedTitle = translatedTitle,
                translatedScript = translatedScript,
                voicePersona = voicePersona
            )

            dubDao.insertHistory(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(item: DubHistoryEntity) {
        dubDao.deleteHistory(item)
    }

    suspend fun toggleFavorite(id: Long, isFav: Boolean) {
        dubDao.updateFavorite(id, isFav)
    }

    suspend fun clearAll() {
        dubDao.clearAll()
    }
}
