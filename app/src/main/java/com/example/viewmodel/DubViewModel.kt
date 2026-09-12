package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DubDatabase
import com.example.data.DubHistoryEntity
import com.example.data.DubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DubViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DubRepository

    val allHistory: StateFlow<List<DubHistoryEntity>>
    val favorites: StateFlow<List<DubHistoryEntity>>

    init {
        val dao = DubDatabase.getDatabase(application).dubDao()
        repository = DubRepository(dao)
        allHistory = repository.allHistory.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        favorites = repository.favorites.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    // UI State
    var selectedPlatform = MutableStateFlow("TikTok")
    var inputUrl = MutableStateFlow("")
    var sourceLanguage = MutableStateFlow("Automático (Auto)")
    var targetLanguage = MutableStateFlow("Português (BR)")
    var voicePersona = MutableStateFlow("Natural & Fluido")

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val _currentActiveDub = MutableStateFlow<DubHistoryEntity?>(null)
    val currentActiveDub: StateFlow<DubHistoryEntity?> = _currentActiveDub.asStateFlow()

    private val _isFloatingOverlayActive = MutableStateFlow(false)
    val isFloatingOverlayActive: StateFlow<Boolean> = _isFloatingOverlayActive.asStateFlow()

    private val _isLiveMonitoring = MutableStateFlow(false)
    val isLiveMonitoring: StateFlow<Boolean> = _isLiveMonitoring.asStateFlow()

    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setPlatform(platform: String) {
        selectedPlatform.value = platform
    }

    fun setInputUrl(url: String) {
        inputUrl.value = url
    }

    fun setSourceLanguage(lang: String) {
        sourceLanguage.value = lang
    }

    fun setTargetLanguage(lang: String) {
        targetLanguage.value = lang
    }

    fun setVoicePersona(persona: String) {
        voicePersona.value = persona
    }

    fun toggleFloatingOverlay() {
        _isFloatingOverlayActive.value = !_isFloatingOverlayActive.value
    }

    fun toggleLiveMonitoring() {
        _isLiveMonitoring.value = !_isLiveMonitoring.value
    }

    fun togglePlayAudio() {
        _isPlayingAudio.value = !_isPlayingAudio.value
    }

    fun translateLink() {
        val url = inputUrl.value.trim()
        if (url.isEmpty()) {
            _errorMessage.value = "Por favor, insira o link do vídeo (TikTok, Instagram ou Kwai)."
            return
        }

        viewModelScope.launch {
            _isTranslating.value = true
            _errorMessage.value = null
            val result = repository.translateAndDub(
                platform = selectedPlatform.value,
                url = url,
                sourceLang = sourceLanguage.value,
                targetLang = targetLanguage.value,
                voicePersona = voicePersona.value
            )
            _isTranslating.value = false
            result.onSuccess { entity ->
                _currentActiveDub.value = entity
                _isPlayingAudio.value = true
            }.onFailure { e ->
                _errorMessage.value = "Erro na tradução: ${e.localizedMessage}"
            }
        }
    }

    fun selectHistoryItem(item: DubHistoryEntity) {
        _currentActiveDub.value = item
        _isPlayingAudio.value = true
    }

    fun toggleFavorite(item: DubHistoryEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(item.id, !item.isFavorite)
        }
    }

    fun deleteHistoryItem(item: DubHistoryEntity) {
        viewModelScope.launch {
            repository.delete(item)
            if (_currentActiveDub.value?.id == item.id) {
                _currentActiveDub.value = null
                _isPlayingAudio.value = false
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
            _currentActiveDub.value = null
            _isPlayingAudio.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
