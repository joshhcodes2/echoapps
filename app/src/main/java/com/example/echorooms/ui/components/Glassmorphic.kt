package com.example.echorooms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies a frosted glass styling (translucency + subtle white border).
 */
fun Modifier.glassmorphic(
    cornerRadius: Dp = 16.dp,
    borderColor: Color = Color(0x15FFFFFF),
    backgroundColor: Color = Color(0x0AFFFFFF),
    borderWidth: Dp = 1.dp
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .clip(shape)
        .background(backgroundColor)
        .border(borderWidth, borderColor, shape)
}

/**
 * Applies a glowing border using a linear gradient representing the room's mood accent.
 */
fun Modifier.neonGlow(
    color: Color,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.5.dp,
    alpha: Float = 0.5f
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val gradient = Brush.linearGradient(
        colors = listOf(
            color.copy(alpha = alpha),
            Color.Transparent,
            color.copy(alpha = alpha * 0.2f),
            color.copy(alpha = alpha * 0.8f)
        )
    )
    return this
        .clip(shape)
        .background(Color(0x08FFFFFF))
        .border(borderWidth, gradient, shape)
}
