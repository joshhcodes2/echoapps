package com.example.echorooms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echorooms.theme.TextGray

@Composable
fun EmotionSliders(
    valPeace: Float,
    onPeaceChange: (Float) -> Unit,
    valWarmth: Float,
    onWarmthChange: (Float) -> Unit,
    valAnxiety: Float,
    onAnxietyChange: (Float) -> Unit,
    valMelancholy: Float,
    onMelancholyChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val blendColor = remember(valPeace, valWarmth, valAnxiety, valMelancholy) {
        val r = (valWarmth * 0.8f + valAnxiety * 0.4f).coerceIn(0f, 1f)
        val g = (valPeace * 0.8f + valWarmth * 0.2f).coerceIn(0f, 1f)
        val b = (valMelancholy * 0.8f + valAnxiety * 0.5f).coerceIn(0f, 1f)
        Color(r, g, b, 0.85f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "EMOTIONAL SIGNATURE",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Calibrate the emotional frequencies of this capsule",
                    color = TextGray,
                    fontSize = 10.sp
                )
            }
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(blendColor, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    .blur(if (valAnxiety > 0.5f) 4.dp else 0.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        EmotionSliderRow(
            label = "Peacefulness",
            value = valPeace,
            onValueChange = onPeaceChange,
            activeColor = Color(0xFFD4FF00)
        )

        EmotionSliderRow(
            label = "Warmth",
            value = valWarmth,
            onValueChange = onWarmthChange,
            activeColor = Color(0xFFFF4081)
        )

        EmotionSliderRow(
            label = "Melancholy",
            value = valMelancholy,
            onValueChange = onMelancholyChange,
            activeColor = Color(0xFF00E5FF)
        )

        EmotionSliderRow(
            label = "Anxiety",
            value = valAnxiety,
            onValueChange = onAnxietyChange,
            activeColor = Color(0xFF9C27B0)
        )
    }
}

@Composable
private fun EmotionSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    activeColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp,
            modifier = Modifier.width(90.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor,
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${(value * 100).toInt()}%",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            modifier = Modifier.width(30.dp)
        )
    }
}
