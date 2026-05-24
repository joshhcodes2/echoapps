package com.example.echorooms.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonPink,
    secondary = NeonCyan,
    tertiary = NeonPurple,
    background = DeepSpaceBg,
    surface = Color(0xFF0F0F15),
    onBackground = TextSilver,
    onSurface = TextSilver,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    surfaceVariant = Color(0x0CFFFFFF),
    onSurfaceVariant = TextGray,
    outline = GlassBorder
)

@Composable
fun EchoRoomsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
