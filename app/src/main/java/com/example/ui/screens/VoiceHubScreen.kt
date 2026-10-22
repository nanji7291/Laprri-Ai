package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.AudioWaveform
import com.example.ui.components.GlowingOrb
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.voice.AssistantAudioState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceHubScreen(
    viewModel: MainViewModel,
    onRequestMicrophonePermission: () -> Unit
) {
    val audioState by viewModel.voiceManager.audioState.collectAsState()
    val recognizedText by viewModel.voiceManager.recognizedText.collectAsState()
    val audioAmplitude by viewModel.voiceManager.audioAmplitude.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    val lastAssistantMessage = messages.lastOrNull { !it.isUser }?.text.orEmpty()

    val statusText = when (audioState) {
        AssistantAudioState.IDLE -> if (isGenerating) "AI Processing query..." else "Ready • Tap to speak"
        AssistantAudioState.LISTENING -> "Listening to your voice..."
        AssistantAudioState.THINKING -> "Laprri Neural Core thinking..."
        AssistantAudioState.SPEAKING -> "Laprri is speaking..."
    }

    val statusColor = when (audioState) {
        AssistantAudioState.IDLE -> NeonCyan
        AssistantAudioState.LISTENING -> NeonEmerald
        AssistantAudioState.THINKING -> ElectricViolet
        AssistantAudioState.SPEAKING -> NeonCyan
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Unlocked / No Login Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonEmerald.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .testTag("unlocked_pro_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NeonEmerald)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LOGIN & SUBSCRIPTION REMOVED",
                        color = NeonEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Active",
                        tint = NeonEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PRO UNLOCKED",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Futuristic Central AI Orb
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(260.dp)
        ) {
            GlowingOrb(
                audioState = audioState,
                amplitude = audioAmplitude,
                onClick = {
                    onRequestMicrophonePermission()
                    viewModel.toggleVoiceListening()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // State indicator pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(SlateCard)
                .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (audioState) {
                        AssistantAudioState.IDLE -> Icons.Default.AutoAwesome
                        AssistantAudioState.LISTENING -> Icons.Default.Mic
                        AssistantAudioState.THINKING -> Icons.Default.GraphicEq
                        AssistantAudioState.SPEAKING -> Icons.Default.GraphicEq
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Live Audio Waveform
        AudioWaveform(
            audioState = audioState,
            amplitude = audioAmplitude,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Recognized speech or current answer box
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                .testTag("ai_response_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (audioState == AssistantAudioState.LISTENING) "Live Transcript" else "Laprri Response",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        recognizedText.isNotBlank() && audioState == AssistantAudioState.LISTENING -> "\"$recognizedText\""
                        lastAssistantMessage.isNotBlank() -> lastAssistantMessage
                        else -> "Bolo 'Hello Laprri' ya neeche diye gaye prompts me se kisi par tap karo!"
                    },
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mic Action Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(NeonCyan, ElectricViolet)
                    )
                )
                .shadow(16.dp, CircleShape)
                .clickable {
                    onRequestMicrophonePermission()
                    viewModel.toggleVoiceListening()
                }
                .testTag("mic_toggle_button")
        ) {
            Icon(
                imageVector = if (audioState == AssistantAudioState.LISTENING || audioState == AssistantAudioState.SPEAKING) {
                    Icons.Default.Stop
                } else {
                    Icons.Default.Mic
                },
                contentDescription = "Voice Control",
                tint = CosmicDark,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Command Suggestions
        Text(
            text = "FAST COMMANDS (NO ACTIVATION NEEDED)",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "🌐 Website banao ek portfolio ke liye",
                "💡 Login & Subscribe kaise hataya?",
                "⏰ Abhi ka samay kya hai?",
                "🧠 Tumhare core features kya hain?",
                "🎨 Cyberpunk theme HTML page",
                "😄 Ek mazedaar joke sunao"
            )

            suggestions.forEach { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SlateCard)
                        .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.handleUserInput(prompt)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = prompt,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
