package com.example.echorooms.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.echorooms.data.database.entity.CustomThemeData
import com.example.echorooms.theme.DeepSpaceBg
import kotlinx.coroutines.isActive
import java.util.Random

private class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var alpha: Float,
    val color: Color,
    var maxLife: Float,
    var currentLife: Float
)

val DefaultThemeData = CustomThemeData(
    displayName = "Cyberpunk",
    primaryColor = Color(0xFFFF0080),
    accentColor = Color(0xFF00FFFF),
    glowColor = Color(0xFFFF0080),
    particleType = "CYBERPUNK",
    soundscape = "CYBERPUNK"
)

/**
 * Renders an animated gradient background with drifting holographic orbs and theme-specific particle overlays.
 * Smoothly blends colors according to the selected mood theme.
 */
@Composable
fun AmbientBackground(
    themeData: CustomThemeData = DefaultThemeData,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val primaryColor = themeData.primaryColor
    val accentColor = themeData.accentColor

    val transition = rememberInfiniteTransition(label = "ambient_orbs")

    // Slow drifting movements for ambient gradients
    val orb1X by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1X"
    )
    val orb1Y by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1Y"
    )

    val orb2X by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2X"
    )
    val orb2Y by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2Y"
    )

    // Dynamic particles list
    val particles = remember { mutableStateListOf<Particle>() }
    val rand = remember { Random() }
    var frameTick by remember { mutableStateOf(0L) }

    LaunchedEffect(themeData) {
        particles.clear()
        // Pre-populate particles
        repeat(40) {
            particles.add(
                Particle(
                    x = rand.nextFloat() * 1080f,
                    y = rand.nextFloat() * 2400f,
                    vx = 0f,
                    vy = 0f,
                    size = 2f + rand.nextFloat() * 6f,
                    alpha = 0.1f + rand.nextFloat() * 0.7f,
                    color = Color.White,
                    maxLife = 1f + rand.nextFloat() * 4f,
                    currentLife = rand.nextFloat() * 2f
                )
            )
        }

        while (isActive) {
            withFrameMillis { frameTime ->
                frameTick = frameTime
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Draw primary color orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(width * orb1X, height * orb1Y),
                    radius = width * 0.75f
                ),
                center = Offset(width * orb1X, height * orb1Y),
                radius = width * 0.75f
            )

            // Draw accent color orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentColor.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(width * orb2X, height * orb2Y),
                    radius = width * 0.65f
                ),
                center = Offset(width * orb2X, height * orb2Y),
                radius = width * 0.65f
            )

            val dt = 0.016f // approximate delta time for UI frame ticks

            // Force evaluation of frameTick to trigger canvas redraw
            @Suppress("UNUSED_VARIABLE")
            val tickVal = frameTick

            particles.forEach { p ->
                p.currentLife += dt
                if (p.currentLife >= p.maxLife || p.x < 0 || p.x > width || p.y < 0 || p.y > height) {
                    // Respawn particle at appropriate boundaries
                    p.currentLife = 0f
                    p.maxLife = 2f + rand.nextFloat() * 4f
                    p.size = 2f + rand.nextFloat() * 6f
                    p.alpha = 0.1f + rand.nextFloat() * 0.7f

                    when (themeData.particleType) {
                        "CYBERPUNK" -> {
                            p.x = rand.nextFloat() * width
                            p.y = height + 10f
                            p.vx = -0.5f + rand.nextFloat() * 1.0f
                            p.vy = -1.5f - rand.nextFloat() * 2f // Upwards float
                        }
                        "RAINY_NIGHT" -> {
                            p.x = rand.nextFloat() * width
                            p.y = -10f
                            p.vx = -2f - rand.nextFloat() * 3f
                            p.vy = 12f + rand.nextFloat() * 10f // Angled rain downward
                            p.size = 1.5f + rand.nextFloat() * 2f
                        }
                        "SUNSET_GLOW" -> {
                            p.x = rand.nextFloat() * width
                            p.y = height + 10f
                            p.vx = -0.3f + rand.nextFloat() * 0.6f
                            p.vy = -0.5f - rand.nextFloat() * 1.0f // Golden dusk floating up
                        }
                        "SPACE_DRIFT" -> {
                            p.x = rand.nextFloat() * width
                            p.y = rand.nextFloat() * height
                            p.vx = -0.4f + rand.nextFloat() * 0.8f
                            p.vy = -0.4f + rand.nextFloat() * 0.8f // Drifting cosmic particles
                        }
                        "RETRO_TERMINAL" -> {
                            p.x = rand.nextFloat() * width
                            p.y = -10f
                            p.vx = 0f
                            p.vy = 2f + rand.nextFloat() * 3f // Green falling phosphor blocks
                            p.size = 3f + rand.nextFloat() * 4f
                        }
                        "MIDNIGHT_BLUE" -> {
                            p.x = rand.nextFloat() * width
                            p.y = rand.nextFloat() * height
                            p.vx = -0.1f + rand.nextFloat() * 0.2f
                            p.vy = -0.1f + rand.nextFloat() * 0.2f
                            p.size = 8f + rand.nextFloat() * 12f // Large soft midnight blobs
                        }
                    }
                } else {
                    p.x += p.vx
                    p.y += p.vy
                }

                val lifeRatio = 1f - (p.currentLife / p.maxLife)
                val finalAlpha = p.alpha * lifeRatio

                when (themeData.particleType) {
                    "CYBERPUNK" -> {
                        // Sparks
                        val sparkColor = if (rand.nextBoolean()) primaryColor else accentColor
                        drawCircle(
                            color = sparkColor,
                            radius = p.size,
                            center = Offset(p.x, p.y),
                            alpha = finalAlpha
                        )
                    }
                    "RAINY_NIGHT" -> {
                        // Rain streaks
                        drawLine(
                            color = Color.White.copy(alpha = 0.3f),
                            start = Offset(p.x, p.y),
                            end = Offset(p.x + p.vx * 1.5f, p.y + p.vy * 1.5f),
                            strokeWidth = p.size,
                            alpha = finalAlpha
                        )
                    }
                    "SUNSET_GLOW" -> {
                        // Slow gold dust
                        drawCircle(
                            color = Color(0xFFFFD54F),
                            radius = p.size,
                            center = Offset(p.x, p.y),
                            alpha = finalAlpha
                        )
                    }
                    "SPACE_DRIFT" -> {
                        // Tiny glowing stars
                        drawCircle(
                            color = Color(0xFFE040FB),
                            radius = p.size * 0.7f,
                            center = Offset(p.x, p.y),
                            alpha = finalAlpha
                        )
                    }
                    "RETRO_TERMINAL" -> {
                        // Square green phosphor dots
                        drawRect(
                            color = accentColor,
                            topLeft = Offset(p.x, p.y),
                            size = androidx.compose.ui.geometry.Size(p.size, p.size),
                            alpha = finalAlpha
                        )
                    }
                    "MIDNIGHT_BLUE" -> {
                        // Pulsing pastel/midnight blobs
                        drawCircle(
                            color = accentColor,
                            radius = p.size * (1f + 0.3f * kotlin.math.sin(p.currentLife * 2f)),
                            center = Offset(p.x, p.y),
                            alpha = finalAlpha * 0.35f
                        )
                    }
                }
            }
        }
        content()
    }
}
