package com.example.voice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var voiceManager: VoiceManager
    private var isVoiceActive by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            toggleVoiceSession()
        } else {
            Toast.makeText(this, "Microphone permission required for Live Voice", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiKey = try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            appInfo.metaData?.getString("GEMINI_API_KEY") ?: ""
        } catch (e: Exception) {
            ""
        }

        voiceManager = VoiceManager(apiKey) { active ->
            isVoiceActive = active
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LiveVoiceScreen(
                        isVoiceActive = isVoiceActive,
                        onToggleVoice = { checkPermissionAndToggle() }
                    )
                }
            }
        }
    }

    private fun checkPermissionAndToggle() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            toggleVoiceSession()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun toggleVoiceSession() {
        if (voiceManager.isStreaming) {
            voiceManager.stopLiveSession()
        } else {
            voiceManager.startLiveSession()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.stopLiveSession()
    }
}

@Composable
fun LiveVoiceScreen(
    isVoiceActive: Boolean,
    onToggleVoice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isVoiceActive) "Laprri AI Voice Mode Active" else "Tap below to Start Live Voice",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onToggleVoice,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isVoiceActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (isVoiceActive) "End Live Session" else "Start Live Voice")
        }
    }
}
