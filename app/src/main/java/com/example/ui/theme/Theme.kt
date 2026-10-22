package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MyraDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CosmicDark,
    primaryContainer = SlateCard,
    onPrimaryContainer = NeonCyan,
    secondary = ElectricViolet,
    onSecondary = CosmicDark,
    secondaryContainer = SlateCard,
    onSecondaryContainer = ElectricViolet,
    tertiary = NeonEmerald,
    onTertiary = CosmicDark,
    background = CosmicDark,
    onBackground = TextPrimary,
    surface = SlateDark,
    onSurface = TextPrimary,
    surfaceVariant = SlateCard,
    onSurfaceVariant = TextSecondary,
    outline = SlateBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = MyraDarkColorScheme,
        typography = Typography,
        content = content
    )
}
