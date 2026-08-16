package com.mjieg.composables.components.parallax.sensor

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.mjieg.composables.components.parallax.model.SensorData
import kotlinx.coroutines.channels.Channel

private const val TAG = "SensorDataManager"
internal class SensorDataManager(context: Context) : SensorEventListener {

    private val sensorManager by lazy { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val accelerometer by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) }
    private val magnetometer by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }
    private var geomagnetic: FloatArray? = null
    private var gravity: FloatArray? = null
    private var screenOrientation: Int = Configuration.ORIENTATION_PORTRAIT

    val data by lazy { Channel<SensorData>(Channel.UNLIMITED) }

    fun init(newScreenOrientation: Int) {
        screenOrientation = newScreenOrientation
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun cancel() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_GRAVITY -> gravity = it.values
                Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = it.values
            }
            if (gravity != null && geomagnetic != null) {
                processSensorData()
            }
        }
    }

    private fun processSensorData() {
        val r = FloatArray(9)
        val i = FloatArray(9)

        if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(r, orientation)
            val (pitch, roll) = when (screenOrientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    Pair(orientation[1], orientation[2])
                }

                else -> {
                    Pair(orientation[2], orientation[1])
                }
            }

            if (pitch in -1.5f..1.5f && roll in -1.5f..1.5f) {
                data.trySend(SensorData(roll = roll, pitch = pitch))
            }
        }
    }
}