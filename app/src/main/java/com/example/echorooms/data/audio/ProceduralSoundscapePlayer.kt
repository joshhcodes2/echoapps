package com.example.echorooms.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import kotlin.math.sin

object ProceduralSoundscapePlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playbackJob: Job? = null
    private var currentTheme: String? = null
    private var volume = 0.25f // Soft ambient background volume

    fun start(themeName: String) {
        if (currentTheme == themeName && playbackJob != null) return
        stop()
        currentTheme = themeName

        playbackJob = scope.launch {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

            val audioTrack = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                return@launch
            }

            try {
                audioTrack.play()
                audioTrack.setVolume(volume)
            } catch (e: Exception) {
                audioTrack.release()
                return@launch
            }

            val shortBuffer = ShortArray(bufferSize / 2)
            var sampleIndex = 0L

            try {
                while (isActive) {
                    for (i in shortBuffer.indices) {
                        val t = sampleIndex / sampleRate.toDouble()
                        val sample = when (themeName.uppercase()) {
                            "CYBERPUNK" -> {
                                // Deep space hum: detuned waves + slow LFO
                                val lfo = 0.8 + 0.2 * sin(2.0 * Math.PI * 0.1 * t)
                                val w1 = sin(2.0 * Math.PI * 55.0 * t)
                                val w2 = sin(2.0 * Math.PI * 82.5 * t)
                                val w3 = sin(2.0 * Math.PI * 110.0 * t)
                                ((w1 * 0.5 + w2 * 0.3 + w3 * 0.2) * lfo * 32767.0 * 0.2).toInt().coerceIn(-32768, 32767).toShort()
                            }
                            "RAINY_NIGHT" -> {
                                // Rainy static: white/pink noise modulated by slow swell
                                val noise = (Math.random() * 2.0 - 1.0)
                                val swell = 0.6 + 0.4 * sin(2.0 * Math.PI * 0.15 * t)
                                (noise * swell * 32767.0 * 0.08).toInt().coerceIn(-32768, 32767).toShort()
                            }
                            "SUNSET_GLOW" -> {
                                // Soft warm drone: major seventh chord (G3, B3, D4, F#4) with slower swell
                                val w1 = sin(2.0 * Math.PI * 196.00 * t)
                                val w2 = sin(2.0 * Math.PI * 246.94 * t)
                                val w3 = sin(2.0 * Math.PI * 293.66 * t)
                                val w4 = sin(2.0 * Math.PI * 370.00 * t)
                                val swell = 0.6 + 0.4 * sin(2.0 * Math.PI * 0.08 * t)
                                ((w1 * 0.3 + w2 * 0.2 + w3 * 0.2 + w4 * 0.3) * swell * 32767.0 * 0.2).toInt().coerceIn(-32768, 32767).toShort()
                            }
                            "SPACE_DRIFT" -> {
                                // Ethereal drift drone: higher pitch sine chords with deep detuning
                                val w1 = sin(2.0 * Math.PI * 220.00 * t)
                                val w2 = sin(2.0 * Math.PI * 330.00 * t)
                                val w3 = sin(2.0 * Math.PI * 440.00 * t)
                                val w4 = sin(2.0 * Math.PI * 550.00 * t)
                                val sweep = 0.5 + 0.5 * sin(2.0 * Math.PI * 0.05 * t)
                                ((w1 * 0.4 + w2 * 0.2 + w3 * 0.2 + w4 * 0.2) * sweep * 32767.0 * 0.15).toInt().coerceIn(-32768, 32767).toShort()
                            }
                            "RETRO_TERMINAL" -> {
                                // Low digital console rumble + vinyl pop cracks
                                val rumble = sin(2.0 * Math.PI * 30.0 * t) * 0.3
                                var pop = 0.0
                                if (Math.random() < 0.0003) {
                                    pop = (Math.random() * 2.0 - 1.0) * 0.6
                                }
                                ((rumble + pop) * 32767.0 * 0.15).toInt().coerceIn(-32768, 32767).toShort()
                            }
                            "MIDNIGHT_BLUE" -> {
                                // Very low deep meditation drone
                                val w1 = sin(2.0 * Math.PI * 40.0 * t)
                                val w2 = sin(2.0 * Math.PI * 60.0 * t)
                                val swell = 0.7 + 0.3 * sin(2.0 * Math.PI * 0.04 * t)
                                ((w1 * 0.6 + w2 * 0.4) * swell * 32767.0 * 0.25).toInt().coerceIn(-32768, 32767).toShort()
                            }
                            else -> 0.toShort()
                        }
                        shortBuffer[i] = sample
                        sampleIndex++
                    }
                    audioTrack.write(shortBuffer, 0, shortBuffer.size)
                }
            } catch (e: Exception) {
                // Ignore
            } finally {
                try {
                    audioTrack.stop()
                } catch (e: Exception) {}
                try {
                    audioTrack.release()
                } catch (e: Exception) {}
            }
        }
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        currentTheme = null
    }

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
    }
}
