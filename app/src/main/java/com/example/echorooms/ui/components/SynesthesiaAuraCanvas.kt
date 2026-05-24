package com.example.echorooms.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.example.echorooms.data.database.entity.CustomThemeData
import com.example.echorooms.ui.detail.RoomDetailViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SynesthesiaAuraCanvas(
    emotion: RoomDetailViewModel.EmotionRatios,
    themeData: CustomThemeData,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "aura_blob")
    
    // Slow base rotation of the aura blob
    val rotationAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing pulse cycle
    val pulseFactor by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)

        // Emotional scaling factors
        val baseRadius = (width.coerceAtMost(height) * 0.28f) * (1f + emotion.warmth * 0.25f)
        val speedFactor = 1f - (emotion.melancholy * 0.5f)
        val noiseFreq = 1f + emotion.anxiety * 4f
        val noiseAmp = (baseRadius * 0.18f) * emotion.anxiety

        // Draw multiple layers for premium glowing depth
        val layers = 3
        for (layer in 0 until layers) {
            val phaseOffset = (layer * (Math.PI / 1.5)).toFloat()
            val layerRotation = rotationAngle * speedFactor * (if (layer % 2 == 0) 1 else -1)
            val layerPulse = 1f + (pulseFactor - 1f) * (1f - layer * 0.2f)

            val path = Path()
            val points = 8
            val angleStep = (2 * Math.PI) / points

            val radialPoints = Array(points) { i ->
                val angle = i * angleStep + Math.toRadians(layerRotation.toDouble())
                // Compute dynamic radius with emotional modulation
                val lfoVal = sin(angle * noiseFreq + Math.toRadians(layerRotation.toDouble() * speedFactor) + phaseOffset)
                val radius = baseRadius * layerPulse + (lfoVal * noiseAmp)
                
                // Melancholy drops the center downward slightly
                val centerYOffset = emotion.melancholy * (height * 0.05f)
                
                Offset(
                    x = (center.x + radius * cos(angle)).toFloat(),
                    y = (center.y + centerYOffset + radius * sin(angle)).toFloat()
                )
            }

            // Draw organic curves connecting points via cubic beziers
            path.moveTo(radialPoints[0].x, radialPoints[0].y)
            for (i in 0 until points) {
                val p0 = radialPoints[i]
                val p1 = radialPoints[(i + 1) % points]
                val p2 = radialPoints[(i + 2) % points]

                // Determine control points for smooth fluid shape
                val cp1X = p0.x + (p1.x - p0.x) * 0.58f
                val cp1Y = p0.y + (p1.y - p0.y) * 0.58f
                val cp2X = p1.x - (p2.x - p1.x) * 0.15f
                val cp2Y = p1.y - (p2.y - p1.y) * 0.15f

                path.cubicTo(cp1X, cp1Y, cp2X, cp2Y, p1.x, p1.y)
            }
            path.close()

            // Resolve color for this layer
            val color = when (layer) {
                0 -> themeData.primaryColor.copy(alpha = 0.35f - emotion.anxiety * 0.1f)
                1 -> themeData.accentColor.copy(alpha = 0.3f)
                else -> themeData.glowColor.copy(alpha = 0.25f + emotion.warmth * 0.15f)
            }

            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                    center = center,
                    radius = baseRadius * 1.5f
                )
            )
        }

        // Draw center emotional core node
        val coreRadius = baseRadius * 0.25f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.8f), themeData.accentColor.copy(alpha = 0.1f)),
                center = center,
                radius = coreRadius
            ),
            radius = coreRadius,
            center = center
        )
    }
}
