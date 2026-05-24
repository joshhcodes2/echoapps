package com.example.echorooms.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.echorooms.data.database.entity.CustomThemeData
import com.example.echorooms.data.database.entity.MemoryEntry
import com.example.echorooms.data.database.entity.RoomEntity
import com.example.echorooms.data.database.entity.getThemeColorsAndAtmosphere
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AtmosphericExporter {

    fun exportAndShareRoom(context: Context, room: RoomEntity, entries: List<MemoryEntry>) {
        val theme = room.getThemeColorsAndAtmosphere()
        
        // Helper to convert color to hex string
        fun composeColorToHex(color: androidx.compose.ui.graphics.Color): String {
            return String.format("#%06X", 0xFFFFFF and color.value.toLong().toInt())
        }
        
        val primaryHex = composeColorToHex(theme.primaryColor)
        val accentHex = composeColorToHex(theme.accentColor)
        val glowHex = composeColorToHex(theme.glowColor)

        val roomDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(room.createdAt))

        val htmlContent = buildString {
            append("""
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EchoRooms Archive - ${room.title}</title>
    <style>
        :root {
            --primary: $primaryHex;
            --accent: $accentHex;
            --glow: $glowHex;
            --bg: #09090e;
            --card-bg: rgba(255, 255, 255, 0.03);
            --border: rgba(255, 255, 255, 0.08);
            --text-main: #f0f0f5;
            --text-muted: #8a8a93;
        }
        
        body {
            background-color: var(--bg);
            color: var(--text-main);
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            margin: 0;
            padding: 40px 20px;
            display: flex;
            flex-direction: column;
            align-items: center;
            min-height: 100vh;
        }

        .container {
            max-width: 700px;
            width: 100%;
        }

        .header {
            text-align: center;
            margin-bottom: 40px;
            padding: 30px;
            background: var(--card-bg);
            border: 1px solid var(--border);
            border-radius: 24px;
            backdrop-filter: blur(20px);
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5), 0 0 20px rgba(${Integer.parseInt(glowHex.substring(1), 16)}, 0.15);
        }

        .emoji {
            font-size: 48px;
            margin-bottom: 16px;
            display: inline-block;
        }

        h1 {
            font-size: 32px;
            font-weight: 800;
            letter-spacing: -0.5px;
            margin: 0 0 8px 0;
            background: linear-gradient(135deg, #fff 30%, var(--accent));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .room-meta {
            font-size: 13px;
            color: var(--text-muted);
            margin-bottom: 12px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        .desc {
            font-size: 15px;
            color: #d1d1d6;
            line-height: 1.6;
            margin: 0;
        }

        .timeline-title {
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 2px;
            color: var(--accent);
            margin: 40px 0 20px 10px;
            font-weight: 700;
        }

        .entry-card {
            background: var(--card-bg);
            border: 1px solid var(--border);
            border-radius: 20px;
            padding: 24px;
            margin-bottom: 24px;
            backdrop-filter: blur(16px);
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
            transition: transform 0.2s, box-shadow 0.2s;
        }

        .entry-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3), 0 0 12px rgba(${Integer.parseInt(accentHex.substring(1), 16)}, 0.1);
        }

        .entry-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 14px;
        }

        .entry-title {
            font-size: 17px;
            font-weight: 700;
            margin: 0;
        }

        .entry-date {
            font-size: 12px;
            color: var(--text-muted);
        }

        .entry-meta {
            font-size: 12px;
            color: var(--accent);
            margin-bottom: 12px;
            display: flex;
            gap: 12px;
        }

        .entry-content {
            font-size: 14px;
            line-height: 1.6;
            color: #e5e5ea;
            margin: 0 0 16px 0;
            white-space: pre-wrap;
        }

        .entry-media {
            width: 100%;
            max-height: 360px;
            border-radius: 12px;
            object-fit: cover;
            margin-bottom: 16px;
            border: 1px solid var(--border);
        }

        audio {
            width: 100%;
            margin-top: 8px;
            background-color: transparent;
        }

        .emotion-bar {
            display: flex;
            gap: 8px;
            margin-top: 16px;
            padding-top: 16px;
            border-top: 1px solid rgba(255, 255, 255, 0.04);
        }

        .emotion-tag {
            font-size: 10px;
            padding: 4px 8px;
            background: rgba(255, 255, 255, 0.05);
            border-radius: 6px;
            color: #d1d1d6;
        }
        
        .tag-peace { border-left: 2px solid #30d158; }
        .tag-warmth { border-left: 2px solid #ffd60a; }
        .tag-anxiety { border-left: 2px solid #ff453a; }
        .tag-melancholy { border-left: 2px solid #64d2ff; }

        .footer {
            text-align: center;
            font-size: 12px;
            color: var(--text-muted);
            margin-top: 60px;
            letter-spacing: 1px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <span class="emoji">${room.iconEmoji}</span>
            <div class="room-meta">Dimension Memory Capsule  |  $roomDate</div>
            <h1>${room.title}</h1>
            <p class="desc">${room.description}</p>
        </div>

        <div class="timeline-title">Memory Vault Timeline</div>
            """)

            entries.forEach { entry ->
                append("""
        <div class="entry-card">
            <div class="entry-header">
                <h3 class="entry-title">${entry.title.ifEmpty { "Memory Note" }}</h3>
                <span class="entry-date">${SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(entry.createdAt))}</span>
            </div>
            
            <div class="entry-meta">
                <span>⚡ Type: ${entry.type}</span>
                ${if (!entry.locationName.isNullOrBlank()) "<span>📍 Location: ${entry.locationName}</span>" else ""}
                ${if (!entry.weather.isNullOrBlank()) "<span>🌧 Weather: ${entry.weather}</span>" else ""}
            </div>
            
            ${if (entry.content.isNotEmpty()) "<p class=\"entry-content\">${entry.content}</p>" else ""}
                """)

                if (entry.filePath != null && (entry.type == "IMAGE" || entry.type == "SKETCH")) {
                    val base64 = fileToBase64(entry.filePath)
                    if (base64 != null) {
                        append("""
            <img class="entry-media" src="data:image/jpeg;base64,$base64" alt="Memory Image">
                        """)
                    }
                }

                if (entry.filePath != null && entry.type == "VOICE") {
                    val base64 = fileToBase64(entry.filePath)
                    if (base64 != null) {
                        append("""
            <audio controls>
                <source src="data:audio/mp4;base64,$base64" type="audio/mp4">
                Your browser does not support the audio element.
            </audio>
                        """)
                    }
                }

                append("""
            <div class="emotion-bar">
                <span class="emotion-tag tag-peace">Peace: ${(entry.valPeace * 100).toInt()}%</span>
                <span class="emotion-tag tag-warmth">Warmth: ${(entry.valWarmth * 100).toInt()}%</span>
                <span class="emotion-tag tag-anxiety">Anxiety: ${(entry.valAnxiety * 100).toInt()}%</span>
                <span class="emotion-tag tag-melancholy">Melancholy: ${(entry.valMelancholy * 100).toInt()}%</span>
            </div>
        </div>
                """)
            }

            append("""
        <div class="footer">
            GENERATED BY ECHOROOMS MEMORY CAPSULE &copy; 2026
        </div>
    </div>
</body>
</html>
            """)
        }

        try {
            val fileName = "${room.title.replace("\\s+".toRegex(), "_")}_EchoArchive.html"
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeText(htmlContent)

            val fileUri = FileProvider.getUriForFile(
                context,
                "com.example.echorooms.fileprovider",
                cacheFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "EchoRooms Archive - ${room.title}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Export Memory Archive")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fileToBase64(filePath: String?): String? {
        if (filePath == null) return null
        val file = File(filePath)
        if (!file.exists()) return null
        return try {
            val bytes = file.readBytes()
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
}
