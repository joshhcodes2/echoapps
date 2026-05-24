package com.example.echorooms.ui.detail

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echorooms.data.audio.AudioPlayerManager
import com.example.echorooms.data.audio.AudioRecorderManager
import com.example.echorooms.data.database.entity.MemoryEntry
import com.example.echorooms.data.database.entity.MemoryType
import com.example.echorooms.data.database.entity.RoomEntity
import com.example.echorooms.data.repository.MemoryRepository
import com.example.echorooms.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class RoomDetailViewModel(
    val roomId: Long,
    private val roomRepository: RoomRepository,
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    val audioRecorder = AudioRecorderManager()
    val audioPlayer = AudioPlayerManager()

    // Observe room details reactive Flow
    val roomState: StateFlow<RoomEntity?> = roomRepository.getRoomById(roomId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Observe memory entries for this room
    val entriesState: StateFlow<List<MemoryEntry>> = memoryRepository.getEntriesForRoom(roomId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Semantic search & filtering state flows
    val searchQuery = MutableStateFlow("")
    val selectedTypes = MutableStateFlow<Set<String>>(emptySet())
    val selectedWeathers = MutableStateFlow<Set<String>>(emptySet())

    val filteredEntriesState: StateFlow<List<MemoryEntry>> = kotlinx.coroutines.flow.combine(
        entriesState,
        searchQuery,
        selectedTypes,
        selectedWeathers
    ) { entries, query, types, weathers ->
        entries.filter { entry ->
            val matchesQuery = query.isBlank() ||
                    entry.title.contains(query, ignoreCase = true) ||
                    entry.content.contains(query, ignoreCase = true) ||
                    (entry.locationName?.contains(query, ignoreCase = true) == true)

            val matchesType = types.isEmpty() || types.contains(entry.type)
            val matchesWeather = weathers.isEmpty() || weathers.contains(entry.weather)

            matchesQuery && matchesType && matchesWeather
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class EmotionRatios(
        val peace: Float = 0.5f,
        val warmth: Float = 0.5f,
        val anxiety: Float = 0.2f,
        val melancholy: Float = 0.2f
    )

    val emotionRatiosState: StateFlow<EmotionRatios> = entriesState
        .map { entries ->
            if (entries.isEmpty()) {
                EmotionRatios()
            } else {
                var totalPeace = 0f
                var totalWarmth = 0f
                var totalAnxiety = 0f
                var totalMelancholy = 0f
                entries.forEach { entry ->
                    totalPeace += entry.valPeace
                    totalWarmth += entry.valWarmth
                    totalAnxiety += entry.valAnxiety
                    totalMelancholy += entry.valMelancholy
                }
                val size = entries.size.toFloat()
                EmotionRatios(
                    peace = totalPeace / size,
                    warmth = totalWarmth / size,
                    anxiety = totalAnxiety / size,
                    melancholy = totalMelancholy / size
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EmotionRatios())



    // Tracks live amplitude values for the visualizer
    private val _recordingAmplitudes = MutableStateFlow<List<Float>>(emptyList())
    val recordingAmplitudes: StateFlow<List<Float>> = _recordingAmplitudes.asStateFlow()

    // Form inputs
    var textTitle by mutableStateOf("")
    var textContent by mutableStateOf("")
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var imageTitle by mutableStateOf("")
    var isMilestone by mutableStateOf(false)

    // Upgraded Metadata States
    var entryWeather by mutableStateOf("Clear Night")
    var entryLocationName by mutableStateOf("")
    var entryIsDeletable by mutableStateOf(true)

    // Emotional slider values
    var valPeace by mutableStateOf(0.5f)
    var valWarmth by mutableStateOf(0.5f)
    var valAnxiety by mutableStateOf(0.2f)
    var valMelancholy by mutableStateOf(0.2f)

    // Current voice note recording parameters
    private var voiceFile: File? = null

    init {
        // Collect recording amplitudes in real-time
        viewModelScope.launch {
            audioRecorder.amplitude.collect { amp ->
                if (audioRecorder.state.value == com.example.echorooms.data.audio.RecordingState.RECORDING) {
                    val current = _recordingAmplitudes.value.toMutableList()
                    current.add(amp)
                    if (current.size > 40) current.removeAt(0)
                    _recordingAmplitudes.value = current
                } else if (audioRecorder.state.value == com.example.echorooms.data.audio.RecordingState.IDLE) {
                    _recordingAmplitudes.value = emptyList()
                }
            }
        }

        // Touch room to update its updatedAt timestamp
        viewModelScope.launch {
            roomRepository.touchRoom(roomId)
        }
    }

    // Text & Milestone notes
    fun saveTextNote() {
        if (textContent.isBlank()) return
        viewModelScope.launch {
            val entry = MemoryEntry(
                roomId = roomId,
                title = textTitle.trim(),
                content = textContent.trim(),
                type = if (isMilestone) MemoryType.MILESTONE.name else MemoryType.TEXT.name,
                weather = entryWeather,
                locationName = entryLocationName.trim().ifEmpty { null },
                isDeletable = entryIsDeletable,
                valPeace = valPeace,
                valWarmth = valWarmth,
                valAnxiety = valAnxiety,
                valMelancholy = valMelancholy
            )
            memoryRepository.addEntry(entry)
            resetInputs()
        }
    }

    // Voice recording lifecycle
    fun startVoiceRecording(context: Context) {
        try {
            val file = File(context.cacheDir, "recording_${System.currentTimeMillis()}.m4a")
            voiceFile = file
            _recordingAmplitudes.value = emptyList()
            audioRecorder.startRecording(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopVoiceRecording(title: String) {
        viewModelScope.launch {
            val path = audioRecorder.stopRecording()
            val file = voiceFile
            if (path != null && file != null && file.exists()) {
                // Copy from cache to persistent files dir
                val persistentFile = File(file.parentFile?.parentFile, "voice_${System.currentTimeMillis()}.m4a")
                file.renameTo(persistentFile)

                val entry = MemoryEntry(
                    roomId = roomId,
                    title = if (title.isBlank()) "Voice Memory" else title.trim(),
                    type = MemoryType.VOICE.name,
                    filePath = persistentFile.absolutePath,
                    audioDurationMs = audioRecorder.durationMs.value,
                    weather = entryWeather,
                    locationName = entryLocationName.trim().ifEmpty { null },
                    isDeletable = entryIsDeletable,
                    valPeace = valPeace,
                    valWarmth = valWarmth,
                    valAnxiety = valAnxiety,
                    valMelancholy = valMelancholy
                )
                memoryRepository.addEntry(entry)
            }
            voiceFile = null
            resetInputs()
        }
    }

    fun cancelVoiceRecording() {
        audioRecorder.cancelRecording()
        voiceFile = null
    }

    // Image memories picker copies images persistently
    fun saveImageMemory(context: Context) {
        val uri = selectedImageUri ?: return
        viewModelScope.launch {
            val localPath = copyImageToInternalStorage(context, uri)
            if (localPath != null) {
                val entry = MemoryEntry(
                    roomId = roomId,
                    title = if (imageTitle.isBlank()) "Image Capture" else imageTitle.trim(),
                    type = MemoryType.IMAGE.name,
                    filePath = localPath,
                    weather = entryWeather,
                    locationName = entryLocationName.trim().ifEmpty { null },
                    isDeletable = entryIsDeletable,
                    valPeace = valPeace,
                    valWarmth = valWarmth,
                    valAnxiety = valAnxiety,
                    valMelancholy = valMelancholy
                )
                memoryRepository.addEntry(entry)
            }
            resetInputs()
        }
    }

    // Save Handdrawn Sketch note
    fun saveSketchMemory(filePath: String) {
        viewModelScope.launch {
            val entry = MemoryEntry(
                roomId = roomId,
                title = "Atmospheric Sketch",
                type = MemoryType.SKETCH.name,
                filePath = filePath,
                weather = entryWeather,
                locationName = entryLocationName.trim().ifEmpty { null },
                isDeletable = entryIsDeletable,
                valPeace = valPeace,
                valWarmth = valWarmth,
                valAnxiety = valAnxiety,
                valMelancholy = valMelancholy
            )
            memoryRepository.addEntry(entry)
            resetInputs()
        }
    }

    fun unlockRoom() {
        viewModelScope.launch {
            roomState.value?.let { r ->
                val updated = r.copy(isTimeCapsule = false, timeCapsuleUnlockDate = null)
                roomRepository.updateRoom(updated)
            }
        }
    }

    private fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val resolver = context.contentResolver
            resolver.openInputStream(uri)?.use { inputStream ->
                val fileName = "mem_${System.currentTimeMillis()}.jpg"
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

    // Audio Playback trigger
    fun playVoiceEntry(filePath: String) {
        audioPlayer.play(filePath)
    }

    fun playVoiceEntryWithEffect(filePath: String, effect: com.example.echorooms.data.audio.VoiceMorphEffect) {
        audioPlayer.play(filePath, effect)
    }

    fun pauseVoiceEntry() {
        audioPlayer.pause()
    }

    // Deletion
    fun deleteEntry(entry: MemoryEntry) {
        if (!entry.isDeletable) return // Prevent deletion of permanent records
        viewModelScope.launch {
            // Delete physical file if present (voice / image / sketch)
            entry.filePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
            memoryRepository.deleteEntry(entry)
        }
    }

    private fun resetInputs() {
        textTitle = ""
        textContent = ""
        selectedImageUri = null
        imageTitle = ""
        isMilestone = false
        entryWeather = "Clear Night"
        entryLocationName = ""
        entryIsDeletable = true
        valPeace = 0.5f
        valWarmth = 0.5f
        valAnxiety = 0.2f
        valMelancholy = 0.2f
    }

    override fun onCleared() {
        super.onCleared()
        // Release hardware resources to avoid context leak
        audioRecorder.release()
        audioPlayer.release()
    }
}
