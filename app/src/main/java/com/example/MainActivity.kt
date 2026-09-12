package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppTab
import com.example.ui.components.FloatingOverlayContainer
import com.example.ui.components.SocialVoiceBottomBar
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveMonitorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DubViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: DubViewModel = viewModel()
                var currentTab by rememberSaveable { mutableStateOf(AppTab.HOME) }

                val platform by viewModel.selectedPlatform.collectAsState()
                val url by viewModel.inputUrl.collectAsState()
                val sourceLang by viewModel.sourceLanguage.collectAsState()
                val targetLang by viewModel.targetLanguage.collectAsState()
                val voicePersona by viewModel.voicePersona.collectAsState()
                val isTranslating by viewModel.isTranslating.collectAsState()
                val currentDub by viewModel.currentActiveDub.collectAsState()
                val isFloatingActive by viewModel.isFloatingOverlayActive.collectAsState()
                val isLiveMonitoring by viewModel.isLiveMonitoring.collectAsState()
                val isPlayingAudio by viewModel.isPlayingAudio.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()

                val historyList by viewModel.allHistory.collectAsState()

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            SocialVoiceBottomBar(
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it }
                            )
                        }
                    ) { innerPadding ->
                        val modifier = Modifier.padding(innerPadding)
                        when (currentTab) {
                            AppTab.HOME -> HomeScreen(
                                viewModel = viewModel,
                                platform = platform,
                                url = url,
                                sourceLang = sourceLang,
                                targetLang = targetLang,
                                voicePersona = voicePersona,
                                isTranslating = isTranslating,
                                currentDub = currentDub,
                                isFloatingActive = isFloatingActive,
                                isPlayingAudio = isPlayingAudio,
                                errorMessage = errorMessage,
                                modifier = modifier
                            )
                            AppTab.LIVE -> LiveMonitorScreen(
                                viewModel = viewModel,
                                isLiveMonitoring = isLiveMonitoring,
                                modifier = modifier
                            )
                            AppTab.HISTORY -> HistoryScreen(
                                viewModel = viewModel,
                                historyList = historyList,
                                modifier = modifier
                            )
                            AppTab.SETTINGS -> SettingsScreen(
                                viewModel = viewModel,
                                isFloatingActive = isFloatingActive,
                                modifier = modifier
                            )
                        }
                    }

                    // Floating Overlay Widget Simulator across social apps
                    FloatingOverlayContainer(
                        isActive = isFloatingActive,
                        platform = platform,
                        onClose = { viewModel.toggleFloatingOverlay() }
                    )
                }
            }
        }
    }
}
