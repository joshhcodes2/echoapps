package com.example.echorooms.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A Room represents an emotional memory space — a container for moments,
 * thoughts, recordings, and images tied to a particular mood.
 */
@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val moodTheme: String = MoodTheme.CYBERPUNK.name,
    val iconEmoji: String = "⚡",
    val coverImagePath: String? = null,
    val isFavorite: Boolean = false,
    val isTimeCapsule: Boolean = false,
    val timeCapsuleUnlockDate: Long? = null,
    val customThemeJson: String? = null,
    val isBiometricProtected: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** Get the MoodTheme enum value for this room */
    fun getMoodTheme(): MoodTheme {
        return try {
            MoodTheme.valueOf(moodTheme)
        } catch (e: IllegalArgumentException) {
            MoodTheme.CYBERPUNK
        }
    }

    /** Check if a time capsule is locked (unlock date is in the future) */
    fun isLocked(): Boolean {
        return isTimeCapsule && timeCapsuleUnlockDate != null &&
                timeCapsuleUnlockDate > System.currentTimeMillis()
    }
}
