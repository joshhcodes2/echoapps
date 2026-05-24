package com.example.echorooms.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echorooms.data.database.entity.CustomThemeData
import com.example.echorooms.data.database.entity.MemoryEntry
import com.example.echorooms.hardware.HapticManager
import com.example.echorooms.theme.GlassBorder
import com.example.echorooms.theme.TextGray
import com.example.echorooms.theme.TextSilver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun MilestoneConstellationMap(
    milestones: List<MemoryEntry>,
    themeData: CustomThemeData,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticManager = remember { HapticManager(context) }
    
    var selectedMilestone by remember { mutableStateOf<MemoryEntry?>(null) }
    val density = LocalDensity.current
    
    val spacingPx = with(density) { 180.dp.toPx() }
    val waveAmpPx = with(density) { 60.dp.toPx() }
    val starRadiusPx = with(density) { 10.dp.toPx() }
    val paddingPx = with(density) { 100.dp.toPx() }
    
    val transition = rememberInfiniteTransition(label = "constellation")
    
    // Rotating pulse glow for selected stars
    val glowScale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "MILESTONE CONSTELLATION PATH",
            style = MaterialTheme.typography.labelSmall,
            color = themeData.accentColor,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            letterSpacing = 1.sp
        )

        if (milestones.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 24.dp)
                    .glassmorphic(cornerRadius = 16.dp)
            ) {
                Text(
                    text = "No milestones registered. Add items with the 'Milestone' toggle to view constellation stars.",
                    color = TextGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(24.dp),
                    fontWeight = FontWeight.Light
                )
            }
        } else {
            val scrollState = rememberScrollState()
            val mapWidth = (180.dp * milestones.size) + 200.dp
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                // Scrollable Constellation Canvas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(scrollState)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(mapWidth)
                            .pointerInput(milestones) {
                                detectTapGestures { offset ->
                                    val centerY = size.height / 2f
                                    milestones.forEachIndexed { index, milestone ->
                                        val x = paddingPx + index * spacingPx
                                        val y = centerY + (sin(index * 1.5) * waveAmpPx).toFloat()
                                        val dist = sqrt((offset.x - x) * (offset.x - x) + (offset.y - y) * (offset.y - y))
                                        if (dist <= starRadiusPx * 2.5f) {
                                            hapticManager.playClick()
                                            selectedMilestone = if (selectedMilestone?.id == milestone.id) null else milestone
                                        }
                                    }
                                }
                            }
                    ) {
                        val centerY = size.height / 2f
                        
                        // 1. Draw Constellation lines connecting the milestones
                        if (milestones.size > 1) {
                            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            for (i in 0 until milestones.size - 1) {
                                val startX = paddingPx + i * spacingPx
                                val startY = centerY + (sin(i * 1.5) * waveAmpPx).toFloat()
                                val endX = paddingPx + (i + 1) * spacingPx
                                val endY = centerY + (sin((i + 1) * 1.5) * waveAmpPx).toFloat()
                                
                                drawLine(
                                    color = themeData.primaryColor.copy(alpha = 0.4f),
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = 2.dp.toPx(),
                                    pathEffect = pathEffect
                                )
                            }
                        }

                        // 2. Draw stars
                        milestones.forEachIndexed { index, milestone ->
                            val x = paddingPx + index * spacingPx
                            val y = centerY + (sin(index * 1.5) * waveAmpPx).toFloat()
                            
                            val isSelected = selectedMilestone?.id == milestone.id
                            
                            // Pulse Glow Ring
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(themeData.glowColor.copy(alpha = if (isSelected) 0.5f else 0.15f), Color.Transparent),
                                    center = Offset(x, y),
                                    radius = starRadiusPx * (if (isSelected) 3.5f else 2.5f) * glowScale
                                ),
                                center = Offset(x, y),
                                radius = starRadiusPx * (if (isSelected) 3.5f else 2.5f) * glowScale
                            )
                            
                            // Star Core
                            drawCircle(
                                color = if (isSelected) Color.White else themeData.accentColor,
                                radius = starRadiusPx * (if (isSelected) 1.2f else 1.0f),
                                center = Offset(x, y)
                            )
                            
                            // Sparkle Lines for Selected Star
                            if (isSelected) {
                                drawLine(
                                    color = Color.White,
                                    start = Offset(x - starRadiusPx * 2f, y),
                                    end = Offset(x + starRadiusPx * 2f, y),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                                drawLine(
                                    color = Color.White,
                                    start = Offset(x, y - starRadiusPx * 2f),
                                    end = Offset(x, y + starRadiusPx * 2f),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                            }
                        }
                    }
                }
            }

            // Milestone Detail Glass Card Overlay
            AnimatedVisibility(visible = selectedMilestone != null) {
                selectedMilestone?.let { milestone ->
                    val dateFormatted = remember(milestone.createdAt) {
                        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                        sdf.format(Date(milestone.createdAt))
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .glassmorphic(cornerRadius = 16.dp)
                            .border(1.dp, themeData.accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✨ " + milestone.title.ifEmpty { "Milestone Memory" }.uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = dateFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGray,
                                fontSize = 10.sp
                            )
                        }
                        
                        if (!milestone.locationName.isNullOrBlank() || !milestone.weather.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = listOfNotNull(milestone.locationName, milestone.weather).joinToString("  |  "),
                                style = MaterialTheme.typography.bodySmall,
                                color = themeData.accentColor,
                                fontSize = 10.sp
                            )
                        }

                        if (milestone.content.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = milestone.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSilver,
                                lineHeight = 18.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            EmotionTag(label = "Peace", value = milestone.valPeace, color = Color.Green)
                            EmotionTag(label = "Warmth", value = milestone.valWarmth, color = Color.Yellow)
                            EmotionTag(label = "Anxiety", value = milestone.valAnxiety, color = Color.Red)
                            EmotionTag(label = "Melancholy", value = milestone.valMelancholy, color = Color.Cyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmotionTag(label: String, value: Float, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: ${(value * 100).toInt()}%",
            color = TextSilver,
            fontSize = 9.sp
        )
    }
}
