 package com.kong.znz
 
 import android.content.Context
 import android.hardware.Sensor
 import android.hardware.SensorEvent
 import android.hardware.SensorEventListener
 import android.hardware.SensorManager
 import android.location.Location
 import java.util.*
 
 class SensorHelper(context: Context) : SensorEventListener {
 
     private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
     private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
     private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
 
     private val gravity = FloatArray(3)
     private val geomagnetic = FloatArray(3)
     private val rotationMatrix = FloatArray(9)
     private val orientation = FloatArray(3)
 
     private var hasGravity = false
     private var hasGeomagnetic = false
 
     // Low-pass filter alpha (0.0 = no filter, 1.0 = full filter)
     private val filterAlpha = 0.18f
     private var filteredAzimuth = 0f
 
     var location: Location? = null
 
     var onAzimuthChanged: ((Float) -> Unit)? = null
 
     fun register() {
         accelerometer?.also {
             sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
         }
         magnetometer?.also {
             sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
         }
     }
 
     fun unregister() {
         sensorManager.unregisterListener(this)
     }
 
     override fun onSensorChanged(event: SensorEvent) {
         when (event.sensor.type) {
             Sensor.TYPE_ACCELEROMETER -> {
                 lowPass(event.values, gravity)
                 hasGravity = true
             }
             Sensor.TYPE_MAGNETIC_FIELD -> {
                 lowPass(event.values, geomagnetic)
                 hasGeomagnetic = true
             }
         }
 
         if (hasGravity && hasGeomagnetic) {
             if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                 SensorManager.getOrientation(rotationMatrix, orientation)
                 var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
 
                 // Magnetic declination correction for true north
                 location?.let { loc ->
                     val geomagField = GeomagneticField(
                         loc.latitude.toFloat(),
                         loc.longitude.toFloat(),
                         loc.altitude.toFloat(),
                         System.currentTimeMillis()
                     )
                     azimuth += geomagField.declination
                 }
 
                 if (azimuth < 0) azimuth += 360
                 if (azimuth >= 360) azimuth -= 360
 
                 onAzimuthChanged?.invoke(azimuth)
             }
         }
     }
 
     override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
 
     private fun lowPass(input: FloatArray, output: FloatArray) {
         if (output[0] == 0f && output[1] == 0f && output[2] == 0f) {
             output[0] = input[0]
             output[1] = input[1]
             output[2] = input[2]
         } else {
             output[0] = output[0] + filterAlpha * (input[0] - output[0])
             output[1] = output[1] + filterAlpha * (input[1] - output[1])
             output[2] = output[2] + filterAlpha * (input[2] - output[2])
         }
     }
 }
