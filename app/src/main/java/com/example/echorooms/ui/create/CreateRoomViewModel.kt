package com.example.echorooms.ui.create

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echorooms.data.database.entity.MoodTheme
import com.example.echorooms.data.database.entity.RoomEntity
import com.example.echorooms.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class CreateRoomViewModel(private val roomRepository: RoomRepository) : ViewModel() {

    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var moodTheme by mutableStateOf(MoodTheme.CYBERPUNK)
    var iconEmoji by mutableStateOf("⚡")
    var coverImageUri by mutableStateOf<Uri?>(null)
    var isTimeCapsule by mutableStateOf(false)
    var timeCapsuleUnlockHours by mutableStateOf("24") // Default 24 hours
    var isBiometricProtected by mutableStateOf(false)
    var customThemeJson by mutableStateOf<String?>(null)

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    fun onThemeSelected(theme: MoodTheme) {
        moodTheme = theme
        // Automatically default icon to the theme's preset
        iconEmoji = theme.iconDefault
    }

    fun saveRoom(context: Context) {
        if (title.isBlank()) return

        viewModelScope.launch {
            var localImagePath: String? = null

            // Copy selected image to internal storage for persistent offline access
            coverImageUri?.let { uri ->
                localImagePath = copyImageToInternalStorage(context, uri)
            }

            val unlockDate = if (isTimeCapsule) {
                val hours = timeCapsuleUnlockHours.toLongOrNull() ?: 24L
                System.currentTimeMillis() + (hours * 60 * 60 * 1000)
            } else {
                null
            }

            val room = RoomEntity(
                title = title.trim(),
                description = description.trim(),
                moodTheme = moodTheme.name,
                iconEmoji = iconEmoji,
                coverImagePath = localImagePath,
                isTimeCapsule = isTimeCapsule,
                timeCapsuleUnlockDate = unlockDate,
                customThemeJson = customThemeJson,
                isBiometricProtected = isBiometricProtected
            )

            val roomId = roomRepository.createRoom(room)
            if (isTimeCapsule && unlockDate != null) {
                com.example.echorooms.receiver.CapsuleUnlockReceiver.scheduleUnlockAlarm(
                    context = context,
                    roomId = roomId,
                    unlockTimeMs = unlockDate,
                    title = title.trim(),
                    emoji = iconEmoji
                )
            }
            _saveSuccess.emit(true)
        }
    }

    private fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val resolver = context.contentResolver
            resolver.openInputStream(uri)?.use { inputStream ->
                val fileName = "cover_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
