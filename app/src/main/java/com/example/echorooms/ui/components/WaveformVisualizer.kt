package com.example.echorooms.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Renders a glowing, dynamic audio waveform.
 * Falls back to a subtle breathing wave when no recording is active.
 */
@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        val width = size.width
        val height = size.height
        val barCount = 35
        val spacing = 6f
        val barWidth = (width - (spacing * (barCount - 1))) / barCount

        val displayAmplitudes = if (amplitudes.isEmpty()) {
            // Dormant breathing state
            List(barCount) { index ->
                val wave = sin(index * 0.4) * 0.12f + 0.15f
                wave.toFloat().coerceIn(0.06f, 0.3f)
            }
        } else {
            // Pad/slice to match barCount
            val list = ArrayList<Float>()
            if (amplitudes.size < barCount) {
                repeat(barCount - amplitudes.size) { list.add(0.06f) }
                list.addAll(amplitudes)
            } else {
                list.addAll(amplitudes.takeLast(barCount))
            }
            list
        }

        for (i in 0 until barCount) {
            val amp = displayAmplitudes.getOrNull(i) ?: 0.06f
            val barHeight = height * amp.coerceIn(0.06f, 1.0f)
            val x = i * (barWidth + spacing)
            val y = (height - barHeight) / 2f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(color, color.copy(alpha = 0.4f))
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
