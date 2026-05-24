package com.example.echorooms.data.database.entity

import androidx.compose.ui.graphics.Color
import org.json.JSONObject

data class CustomThemeData(
    val displayName: String,
    val primaryColor: Color,
    val accentColor: Color,
    val glowColor: Color,
    val particleType: String,
    val soundscape: String
)

fun MoodTheme.toCustomThemeData(): CustomThemeData {
    return CustomThemeData(
        displayName = this.displayName,
        primaryColor = this.primary(),
        accentColor = this.accent(),
        glowColor = this.glow(),
        particleType = this.name,
        soundscape = this.name
    )
}

fun RoomEntity.getThemeColorsAndAtmosphere(): CustomThemeData {
    val defaultTheme = getMoodTheme()
    if (customThemeJson.isNullOrBlank()) {
        return defaultTheme.toCustomThemeData()
    }
    return try {
        val obj = JSONObject(customThemeJson)
        CustomThemeData(
            displayName = obj.optString("displayName", "Custom Theme"),
            primaryColor = Color(android.graphics.Color.parseColor(obj.getString("primary"))),
            accentColor = Color(android.graphics.Color.parseColor(obj.getString("accent"))),
            glowColor = Color(android.graphics.Color.parseColor(obj.getString("glow"))),
            particleType = obj.optString("particleType", defaultTheme.name),
            soundscape = obj.optString("soundscape", defaultTheme.name)
        )
    } catch (e: Exception) {
        defaultTheme.toCustomThemeData()
    }
}

