package com.example.echorooms.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Types of memory entries that can be stored in a room.
 */
enum class MemoryType {
    TEXT,
    VOICE,
    IMAGE,
    MILESTONE,
    SKETCH
}

/**
 * A MemoryEntry represents a single moment captured inside a room.
 * It could be a text note, voice recording, image, or milestone marker.
 */
@Entity(
    tableName = "memory_entries",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("roomId")]
)
data class MemoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomId: Long,
    val title: String = "",
    val content: String = "",
    val type: String = MemoryType.TEXT.name,
    val filePath: String? = null,
    val audioDurationMs: Long? = null,
    val weather: String? = null,
    val locationName: String? = null,
    val isDeletable: Boolean = true,
    val sketchPath: String? = null,
    val valPeace: Float = 0f,
    val valWarmth: Float = 0f,
    val valAnxiety: Float = 0f,
    val valMelancholy: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Get the MemoryType enum value */
    fun getMemoryType(): MemoryType {
        return try {
            MemoryType.valueOf(type)
        } catch (e: IllegalArgumentException) {
            MemoryType.TEXT
        }
    }

    /** Get a display-friendly type icon */
    fun typeIcon(): String {
        return when (getMemoryType()) {
            MemoryType.TEXT -> "📝"
            MemoryType.VOICE -> "🎙"
            MemoryType.IMAGE -> "📸"
            MemoryType.MILESTONE -> "⭐"
            MemoryType.SKETCH -> "🎨"
        }
    }
}
