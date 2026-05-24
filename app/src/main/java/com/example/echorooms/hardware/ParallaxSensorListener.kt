package com.example.echorooms.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ParallaxSensorListener : SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var gravitySensor: Sensor? = null

    private val _tiltOffset = MutableStateFlow(Pair(0f, 0f))
    val tiltOffset: StateFlow<Pair<Float, Float>> = _tiltOffset.asStateFlow()

    fun start(context: Context) {
        if (sensorManager != null) return
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return
        sensorManager = sm
        // Gravity is smoother; fallback to Accelerometer
        val sensor = sm.getDefaultSensor(Sensor.TYPE_GRAVITY) ?: sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gravitySensor = sensor
        if (sensor != null) {
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
        gravitySensor = null
        _tiltOffset.value = Pair(0f, 0f)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type == Sensor.TYPE_GRAVITY || event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val rawX = event.values[0]
            val rawY = event.values[1]

            // Apply low-pass filter for smooth motion and map to maximum translation deflections
            val targetX = -rawX * 2.5f
            val targetY = rawY * 2.5f

            val current = _tiltOffset.value
            val k = 0.15f // low-pass smoothing factor
            val newX = current.first + k * (targetX - current.first)
            val newY = current.second + k * (targetY - current.second)

            _tiltOffset.value = Pair(newX, newY)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
