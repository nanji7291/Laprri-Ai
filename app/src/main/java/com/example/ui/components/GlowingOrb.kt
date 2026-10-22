package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.voice.AssistantAudioState

@Composable
fun GlowingOrb(
    audioState: AssistantAudioState,
    amplitude: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )

    val coreColor = when (audioState) {
        AssistantAudioState.IDLE -> NeonCyan
        AssistantAudioState.LISTENING -> NeonEmerald
        AssistantAudioState.THINKING -> ElectricViolet
        AssistantAudioState.SPEAKING -> NeonCyan
    }

    val glowColor = when (audioState) {
        AssistantAudioState.IDLE -> ElectricViolet
        AssistantAudioState.LISTENING -> NeonCyan
        AssistantAudioState.THINKING -> DeepViolet
        AssistantAudioState.SPEAKING -> ElectricViolet
    }

    val dynamicBoost = if (audioState == AssistantAudioState.LISTENING || audioState == AssistantAudioState.SPEAKING) {
        amplitude * 0.4f
    } else 0f

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(240.dp)
            .testTag("glowing_orb")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2.8f) * (pulseScale + dynamicBoost)

            // Outer soft ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.35f),
                        coreColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.55f
                ),
                radius = baseRadius * 1.55f,
                center = center
            )

            // Mid aura ripple
            drawCircle(
                color = coreColor.copy(alpha = 0.25f),
                radius = baseRadius * 1.25f,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // Concentric pulsating orbit ring
            drawCircle(
                color = glowColor.copy(alpha = 0.4f),
                radius = baseRadius * 1.1f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Solid vibrant core sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        coreColor,
                        glowColor,
                        DeepViolet
                    ),
                    center = center.copy(y = center.y - 10f),
                    radius = baseRadius * 0.85f
                ),
                radius = baseRadius * 0.85f,
                center = center
            )

            // Inner high-tech specular reflection highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = baseRadius * 0.25f,
                center = Offset(center.x - baseRadius * 0.3f, center.y - baseRadius * 0.35f)
            )
        }
    }
}
