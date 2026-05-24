package com.example.echorooms.data.database.entity

import androidx.compose.ui.graphics.Color

/**
 * Mood themes define the visual atmosphere of each memory room.
 * Each theme carries its own color palette and emotional identity.
 */
enum class MoodTheme(
    val displayName: String,
    val primaryColor: Long,
    val accentColor: Long,
    val glowColor: Long,
    val iconDefault: String,
    val description: String
) {
    CYBERPUNK(
        displayName = "Cyberpunk",
        primaryColor = 0xFFFF0080,
        accentColor = 0xFF00FFFF,
        glowColor = 0xFFFF0080,
        iconDefault = "⚡",
        description = "Neon-drenched digital nights"
    ),
    RAINY_NIGHT(
        displayName = "Rainy Night",
        primaryColor = 0xFF4A6FA5,
        accentColor = 0xFF7BA7D7,
        glowColor = 0xFF4A6FA5,
        iconDefault = "🌧",
        description = "Moody reflections and quiet storms"
    ),
    SUNSET_GLOW(
        displayName = "Sunset Glow",
        primaryColor = 0xFFFF6B35,
        accentColor = 0xFFFFB347,
        glowColor = 0xFFFF6B35,
        iconDefault = "🌅",
        description = "Warm light fading into memory"
    ),
    SPACE_DRIFT(
        displayName = "Space Drift",
        primaryColor = 0xFF7B2FBE,
        accentColor = 0xFFB388FF,
        glowColor = 0xFF7B2FBE,
        iconDefault = "🚀",
        description = "Ethereal voids and cosmic whispers"
    ),
    RETRO_TERMINAL(
        displayName = "Retro Terminal",
        primaryColor = 0xFF00FF41,
        accentColor = 0xFF39FF14,
        glowColor = 0xFF00FF41,
        iconDefault = "💻",
        description = "Phosphor screens and digital echoes"
    ),
    MIDNIGHT_BLUE(
        displayName = "Midnight Blue",
        primaryColor = 0xFF1A237E,
        accentColor = 0xFF536DFE,
        glowColor = 0xFF536DFE,
        iconDefault = "🌙",
        description = "Deep contemplation in the dark"
    );

    /** Compose Color for the primary color */
    fun primary(): Color = Color(primaryColor)

    /** Compose Color for the accent color */
    fun accent(): Color = Color(accentColor)

    /** Compose Color for the glow effect */
    fun glow(): Color = Color(glowColor)
}
