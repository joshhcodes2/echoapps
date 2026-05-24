package com.example.echorooms.hardware

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun vibrate(pattern: LongArray) {
        if (vibrator != null && vibrator.hasVibrator()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            } catch (e: Exception) {
                // Haptic fails gracefully on unsupported emulators or hardware
            }
        }
    }

    fun playHeartbeat() {
        vibrate(longArrayOf(0, 80, 150, 80))
    }

    fun playClick() {
        vibrate(longArrayOf(0, 15))
    }

    fun playDecryptionTick() {
        vibrate(longArrayOf(0, 10, 100))
    }

    fun playExplosion() {
        vibrate(longArrayOf(0, 20, 50, 40, 50, 80, 50, 150))
    }
}
