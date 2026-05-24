package com.example.echorooms.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echorooms.theme.NeonPink
import java.io.File
import java.io.FileOutputStream

data class DrawnPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

@Composable
fun DrawingCanvas(
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    val paths = remember { mutableStateListOf<DrawnPath>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }
    var selectedColor by remember { mutableStateOf(NeonPink) }
    val strokeWidth = 8f

    val colors = listOf(
        NeonPink,
        Color(0xFF00E5FF), // Neon Cyan
        Color(0xFFD4FF00), // Neon Yellow/Green
        Color.White
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0C0E14), RoundedCornerShape(24.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎨 EXPRESSIVE SKETCH CANVAS",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // The drawing board area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF06070B))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPoints.clear()
                                currentPoints.add(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPoints.add(change.position)
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty()) {
                                    paths.add(
                                        DrawnPath(
                                            points = currentPoints.toList(),
                                            color = selectedColor,
                                            strokeWidth = strokeWidth
                                        )
                                    )
                                    currentPoints.clear()
                                }
                            }
                        )
                    }
            ) {
                // Draw past paths
                paths.forEach { path ->
                    if (path.points.size > 1) {
                        val composePath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(path.points.first().x, path.points.first().y)
                            for (i in 1 until path.points.size) {
                                lineTo(path.points[i].x, path.points[i].y)
                            }
                        }
                        drawPath(
                            path = composePath,
                            color = path.color,
                            style = Stroke(width = path.strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Draw currently active path
                if (currentPoints.size > 1) {
                    val activePath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(currentPoints.first().x, currentPoints.first().y)
                        for (i in 1 until currentPoints.size) {
                            lineTo(currentPoints[i].x, currentPoints[i].y)
                        }
                    }
                    drawPath(
                        path = activePath,
                        color = selectedColor,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tool Palette
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Colors
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 2.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }

            // Undo / Clear
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { if (paths.isNotEmpty()) paths.removeLast() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Undo", color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = { paths.clear() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Clear", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save & Cancel Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }

            Button(
                onClick = {
                    val sketchFilePath = savePathsToPng(context, paths.toList(), 800, 600)
                    if (sketchFilePath != null) {
                        onSave(sketchFilePath)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Sketch", color = Color.White)
            }
        }
    }
}

private fun savePathsToPng(context: Context, paths: List<DrawnPath>, width: Int, height: Int): String? {
    return try {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // Dark background matching the app's aesthetic
        canvas.drawColor(android.graphics.Color.parseColor("#06070B"))
        
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        if (paths.isNotEmpty()) {
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE
            paths.forEach { p ->
                p.points.forEach { pt ->
                    if (pt.x < minX) minX = pt.x
                    if (pt.y < minY) minY = pt.y
                    if (pt.x > maxX) maxX = pt.x
                    if (pt.y > maxY) maxY = pt.y
                }
            }
            
            val pad = 40f
            val contentW = maxX - minX
            val contentH = maxY - minY
            val scaleX = if (contentW > 0) (width - pad * 2) / contentW else 1f
            val scaleY = if (contentH > 0) (height - pad * 2) / contentH else 1f
            val scale = minOf(scaleX, scaleY).coerceAtMost(2f)

            val offsetX = pad + (width - pad * 2 - contentW * scale) / 2f - minX * scale
            val offsetY = pad + (height - pad * 2 - contentH * scale) / 2f - minY * scale

            paths.forEach { path ->
                if (path.points.isEmpty()) return@forEach
                paint.color = path.color.toArgb()
                paint.strokeWidth = path.strokeWidth * scale
                
                val androidPath = android.graphics.Path()
                androidPath.moveTo(path.points[0].x * scale + offsetX, path.points[0].y * scale + offsetY)
                for (i in 1 until path.points.size) {
                    androidPath.lineTo(path.points[i].x * scale + offsetX, path.points[i].y * scale + offsetY)
                }
                canvas.drawPath(androidPath, paint)
            }
        }

        val file = File(context.filesDir, "sketch_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
