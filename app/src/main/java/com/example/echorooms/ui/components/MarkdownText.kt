package com.example.echorooms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echorooms.theme.TextSilver

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextSilver
) {
    val lines = text.split("\n")
    Column(modifier = modifier) {
        lines.forEach { line ->
            when {
                line.startsWith("## ") -> {
                    val cleanText = line.substring(3)
                    Text(
                        text = cleanText,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                line.startsWith("# ") -> {
                    val cleanText = line.substring(2)
                    Text(
                        text = cleanText,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                line.startsWith("> ") -> {
                    val cleanText = line.substring(2)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(1.5.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(cleanText),
                            color = color.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 18.sp
                        )
                    }
                }
                else -> {
                    if (line.isNotBlank()) {
                        Text(
                            text = parseInlineMarkdown(line),
                            color = color,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val boldPattern = Regex("\\*\\*(.*?)\\*\\*")
        val italicPattern = Regex("\\*(.*?)\\*")
        
        var remaining = text
        while (remaining.isNotEmpty()) {
            val nextBold = boldPattern.find(remaining)
            val nextItalic = italicPattern.find(remaining)
            
            val boldIndex = nextBold?.range?.first ?: Int.MAX_VALUE
            val italicIndex = nextItalic?.range?.first ?: Int.MAX_VALUE
            
            if (boldIndex == Int.MAX_VALUE && italicIndex == Int.MAX_VALUE) {
                append(remaining)
                break
            }
            
            if (boldIndex < italicIndex) {
                val match = nextBold!!
                append(remaining.substring(0, match.range.first))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groupValues[1])
                }
                remaining = remaining.substring(match.range.last + 1)
            } else {
                val match = nextItalic!!
                append(remaining.substring(0, match.range.first))
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(match.groupValues[1])
                }
                remaining = remaining.substring(match.range.last + 1)
            }
        }
    }
}
