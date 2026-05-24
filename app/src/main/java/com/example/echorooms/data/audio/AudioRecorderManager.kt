package com.example.echorooms.data.audio

import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Recording states for the audio manager.
 */
enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED
}

/**
 * Manages audio recording using MediaRecorder.
 * Tracks recording state, duration, and amplitude for waveform visualization.
 */
class AudioRecorderManager {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(RecordingState.IDLE)
    val state: StateFlow<RecordingState> = _state.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    /**
     * Start recording audio to the specified file.
     */
    @Suppress("DEPRECATION")
    fun startRecording(context: android.content.Context, file: File) {
        try {
            outputFile = file
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context.applicationContext)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            _state.value = RecordingState.RECORDING
            _durationMs.value = 0L
            startTimer()
        } catch (e: Exception) {
            release()
        }
    }

    /**
     * Pause the current recording (API 24+).
     */
    fun pauseRecording() {
        if (_state.value == RecordingState.RECORDING) {
            try {
                mediaRecorder?.pause()
                _state.value = RecordingState.PAUSED
                timerJob?.cancel()
            } catch (e: Exception) {
                // Ignore pause errors
            }
        }
    }

    /**
     * Resume a paused recording.
     */
    fun resumeRecording() {
        if (_state.value == RecordingState.PAUSED) {
            try {
                mediaRecorder?.resume()
                _state.value = RecordingState.RECORDING
                startTimer()
            } catch (e: Exception) {
                // Ignore resume errors
            }
        }
    }

    /**
     * Stop recording and return the output file path.
     */
    fun stopRecording(): String? {
        return try {
            timerJob?.cancel()
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            _state.value = RecordingState.IDLE
            outputFile?.absolutePath
        } catch (e: Exception) {
            release()
            null
        }
    }

    /**
     * Cancel the recording and delete the output file.
     */
    fun cancelRecording() {
        try {
            timerJob?.cancel()
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            outputFile?.delete()
            outputFile = null
            _state.value = RecordingState.IDLE
            _durationMs.value = 0L
        } catch (e: Exception) {
            mediaRecorder?.release()
            mediaRecorder = null
            _state.value = RecordingState.IDLE
        }
    }

    /**
     * Release all resources.
     */
    fun release() {
        timerJob?.cancel()
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaRecorder = null
        _state.value = RecordingState.IDLE
        _durationMs.value = 0L
        _amplitude.value = 0f
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _state.value == RecordingState.RECORDING) {
                delay(50L) // Update every 50ms for smooth waveform
                _durationMs.value += 50L
                // Get amplitude for waveform (normalized to 0-1)
                val maxAmplitude = try {
                    mediaRecorder?.maxAmplitude?.toFloat() ?: 0f
                } catch (e: Exception) {
                    0f
                }
                _amplitude.value = (maxAmplitude / 32768f).coerceIn(0f, 1f)
            }
        }
    }

    /**
     * Format duration in mm:ss format.
     */
    companion object {
        fun formatDuration(ms: Long): String {
            val totalSeconds = ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
    }
}
