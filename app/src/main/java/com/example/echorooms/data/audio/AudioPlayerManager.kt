package com.example.echorooms.data.audio

import android.media.MediaPlayer
import android.media.PlaybackParams
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

/**
 * Playback states for the audio player.
 */
enum class PlaybackState {
    IDLE,
    PLAYING,
    PAUSED
}

/**
 * Morphing effects available for voice memories.
 */
enum class VoiceMorphEffect(val pitch: Float, val speed: Float, val displayName: String) {
    NORMAL(1.0f, 1.0f, "Original"),
    ETHEREAL(0.8f, 0.9f, "Ethereal Echo"),
    CYBER(1.25f, 1.15f, "Cyber Grid"),
    DEEP(0.6f, 0.85f, "Deep Resonance")
}

/**
 * Manages audio playback using MediaPlayer.
 * Tracks playback state, position, and duration for UI updates.
 */
class AudioPlayerManager {

    private var mediaPlayer: MediaPlayer? = null
    private var positionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(PlaybackState.IDLE)
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _currentFilePath = MutableStateFlow<String?>(null)
    val currentFilePath: StateFlow<String?> = _currentFilePath.asStateFlow()

    /**
     * Play an audio file from the given path with a voice morph effect.
     */
    fun play(filePath: String, effect: VoiceMorphEffect = VoiceMorphEffect.NORMAL) {
        // If same file is paused, just resume
        if (_currentFilePath.value == filePath && _state.value == PlaybackState.PAUSED) {
            resume()
            return
        }

        // Stop any current playback
        stop()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        val params = PlaybackParams().apply {
                            pitch = effect.pitch
                            speed = effect.speed
                        }
                        playbackParams = params
                    } catch (e: Exception) {
                        // Fallback gracefully on devices that fail to set playback params
                    }
                }
                
                start()
                setOnCompletionListener {
                    _state.value = PlaybackState.IDLE
                    _currentPosition.value = 0
                    positionJob?.cancel()
                }
            }
            _currentFilePath.value = filePath
            _state.value = PlaybackState.PLAYING
            _duration.value = mediaPlayer?.duration ?: 0
            startPositionTracking()
        } catch (e: Exception) {
            release()
        }
    }

    /**
     * Pause current playback.
     */
    fun pause() {
        if (_state.value == PlaybackState.PLAYING) {
            try {
                mediaPlayer?.pause()
                _state.value = PlaybackState.PAUSED
                positionJob?.cancel()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Resume paused playback.
     */
    fun resume() {
        if (_state.value == PlaybackState.PAUSED) {
            try {
                mediaPlayer?.start()
                _state.value = PlaybackState.PLAYING
                startPositionTracking()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Stop playback and reset position.
     */
    fun stop() {
        positionJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaPlayer = null
        _state.value = PlaybackState.IDLE
        _currentPosition.value = 0
        _currentFilePath.value = null
    }

    /**
     * Seek to a specific position in milliseconds.
     */
    fun seekTo(position: Int) {
        try {
            mediaPlayer?.seekTo(position)
            _currentPosition.value = position
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Release all resources.
     */
    fun release() {
        positionJob?.cancel()
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaPlayer = null
        _state.value = PlaybackState.IDLE
        _currentPosition.value = 0
        _duration.value = 0
        _currentFilePath.value = null
    }

    private fun startPositionTracking() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (isActive && _state.value == PlaybackState.PLAYING) {
                _currentPosition.value = mediaPlayer?.currentPosition ?: 0
                delay(100L)
            }
        }
    }
}
