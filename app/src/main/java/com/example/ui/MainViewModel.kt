package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.BatteryManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppNavigationTab
import com.example.data.ChatMessage
import com.example.data.GeminiService
import com.example.data.WebGenerator
import com.example.data.WebProject
import com.example.voice.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val geminiService = GeminiService()

    // Get API Key safely from AndroidManifest meta-data or use a placeholder
    private val apiKey: String = try {
        val appInfo = application.packageManager.getApplicationInfo(application.packageName, android.content.pm.PackageManager.GET_META_DATA)
        appInfo.metaData?.getString("GEMINI_API_KEY") ?: ""
    } catch (e: Exception) {
        ""
    }

    val voiceManager = VoiceManager(apiKey) { active ->
        _isVoiceActive.value = active
    }

    private val _currentTab = MutableStateFlow(AppNavigationTab.VOICE_HUB)
    val currentTab: StateFlow<AppNavigationTab> = _currentTab.asStateFlow()

    private val _isVoiceActive = MutableStateFlow(false)
    val isVoiceActive: StateFlow<Boolean> = _isVoiceActive.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Namaste! Main Laprri hoon — aapki Next-Gen AI Assistant. Login aur subscription requirements ko poori tarah se hata diya gaya hai! Sabhi features jaise Voice AI, Chat, WebForge website builder aur System Tools 100% unlocked aur free hain. Main aapki kya madad karoon?",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _webProjects = MutableStateFlow<List<WebProject>>(WebGenerator.getDefaultProjects())
    val webProjects: StateFlow<List<WebProject>> = _webProjects.asStateFlow()

    private val _activeWebProject = MutableStateFlow<WebProject?>(WebGenerator.getDefaultProjects().firstOrNull())
    val activeWebProject: StateFlow<WebProject?> = _activeWebProject.asStateFlow()

    // System Tools State
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = application.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private val _volumeLevel = MutableStateFlow(getSystemVolume())
    val volumeLevel: StateFlow<Int> = _volumeLevel.asStateFlow()

    private val _flashlightOn = MutableStateFlow(false)
    val flashlightOn: StateFlow<Boolean> = _flashlightOn.asStateFlow()

    private val _batteryPercent = MutableStateFlow(getBatteryPercentage())
    val batteryPercent: StateFlow<Int> = _batteryPercent.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _speechPitch = MutableStateFlow(1.05f)
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    fun setTab(tab: AppNavigationTab) {
        _currentTab.value = tab
    }

    fun handleUserInput(text: String, speakResponse: Boolean = true) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _isGenerating.value) return

        val userMsg = ChatMessage(text = trimmed, isUser = true)
        _messages.value = _messages.value + userMsg
        _isGenerating.value = true

        viewModelScope.launch {
            try {
                if (trimmed.contains("website", ignoreCase = true) || trimmed.contains("web page", ignoreCase = true)) {
                    val newHtml = WebGenerator.generateHtmlFromPrompt(trimmed)
                    val project = WebProject(
                        title = trimmed.take(24).replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() },
                        description = "Generated website based on: $trimmed",
                        htmlContent = newHtml
                    )
                    _webProjects.value = listOf(project) + _webProjects.value
                    _activeWebProject.value = project

                    val reply = "Maine aapke prompt ke anusaar WebForge me naya website project generate kar diya hai! Aap 'WebForge' tab me jakar iska live interactive preview dekh sakte hain."
                    val assistantMsg = ChatMessage(
                        text = reply,
                        isUser = false,
                        codeSnippet = newHtml
                    )
                    _messages.value = _messages.value + assistantMsg
                } else {
                    val history = _messages.value.takeLast(6).map {
                        Pair(if (it.isUser) "user" else "model", it.text)
                    }
                    val reply = geminiService.generateResponse(trimmed, history)
                    val assistantMsg = ChatMessage(text = reply, isUser = false)
                    _messages.value = _messages.value + assistantMsg
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error in handling input", e)
                val errMsg = "Kshama karein, ek takneeki samasya aayi. Kripya punah prayas karein."
                _messages.value = _messages.value + ChatMessage(text = errMsg, isUser = false)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun toggleVoiceListening() {
        if (voiceManager.isStreaming) {
            voiceManager.stopLiveSession()
        } else {
            voiceManager.startLiveSession()
        }
    }

    fun speakMessage(message: String) {
        // Text chat response helper if needed
    }

    fun stopSpeaking() {
        voiceManager.stopLiveSession()
    }

    fun generateNewWebsite(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) return
        val newHtml = WebGenerator.generateHtmlFromPrompt(trimmed)
        val project = WebProject(
            title = trimmed.take(25),
            description = "Custom build: $trimmed",
            htmlContent = newHtml
        )
        _webProjects.value = listOf(project) + _webProjects.value
        _activeWebProject.value = project
    }

    fun selectWebProject(project: WebProject) {
        _activeWebProject.value = project
    }

    // System Tools Actions
    private fun getSystemVolume(): Int {
        return try {
            val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
            val current = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 8
            ((current.toFloat() / max) * 100).toInt()
        } catch (e: Exception) {
            50
        }
    }

    fun setVolume(percent: Int) {
        try {
            audioManager?.let { am ->
                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val target = ((percent.toFloat() / 100) * max).toInt()
                am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                _volumeLevel.value = percent
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Failed to set volume", e)
        }
    }

    fun toggleFlashlight() {
        try {
            cameraManager?.let { cm ->
                val cameraId = cm.cameraIdList.firstOrNull() ?: return
                val newState = !_flashlightOn.value
                cm.setTorchMode(cameraId, newState)
                _flashlightOn.value = newState
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Flashlight toggle failed", e)
            _flashlightOn.value = !_flashlightOn.value
        }
    }

    private fun getBatteryPercentage(): Int {
        return try {
            val batteryStatus = getApplication<Application>().registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                (level * 100) / scale
            } else 88
        } catch (e: Exception) {
            88
        }
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
    }

    fun setSpeechPitch(pitch: Float) {
        _speechPitch.value = pitch
    }

    fun clearChatHistory() {
        _messages.value = listOf(
            ChatMessage(
                text = "Chat history clear kar di gayi hai. Main nayi baat-cheet ke liye tayar hoon!",
                isUser = false
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.stopLiveSession()
    }
}
