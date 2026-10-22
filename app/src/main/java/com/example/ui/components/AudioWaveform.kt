package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.voice.AssistantAudioState
import kotlin.math.sin

@Composable
fun AudioWaveform(
    audioState: AssistantAudioState,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        val barCount = 20
        val totalSpacing = size.width / barCount
        val barWidth = totalSpacing * 0.45f
        val centerY = size.height / 2f
        val maxBarHeight = size.height * 0.85f

        val isActive = audioState == AssistantAudioState.LISTENING || audioState == AssistantAudioState.SPEAKING
        val isThinking = audioState == AssistantAudioState.THINKING

        for (i in 0 until barCount) {
            val x = i * totalSpacing + (totalSpacing - barWidth) / 2f

            val waveFactor = sin(phase + i * 0.35f).toFloat()

            val barHeight = when {
                isActive -> {
                    val ampFactor = (amplitude * 0.7f + 0.3f)
                    (14f + (maxBarHeight - 14f) * (0.5f + 0.5f * waveFactor) * ampFactor).coerceIn(8f, maxBarHeight)
                }
                isThinking -> {
                    (10f + 25f * (0.5f + 0.5f * sin(phase * 2f + i * 0.5f))).coerceIn(6f, maxBarHeight)
                }
                else -> {
                    // Idle subtle breathing
                    (8f + 6f * (0.5f + 0.5f * waveFactor)).coerceIn(6f, 20f)
                }
            }

            val topY = centerY - barHeight / 2f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NeonCyan,
                        ElectricViolet
                    ),
                    startY = topY,
                    endY = topY + barHeight
                ),
                topLeft = Offset(x, topY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
