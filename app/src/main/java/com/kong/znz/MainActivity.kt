 package com.kong.znz
 
 import android.Manifest
 import android.content.pm.PackageManager
 import android.location.Location
 import android.os.Bundle
 import android.view.View
 import android.widget.TextView
 import android.widget.Toast
 import androidx.activity.result.contract.ActivityResultContracts
 import androidx.appcompat.app.AppCompatActivity
 import androidx.core.content.ContextCompat
 import com.google.android.gms.location.FusedLocationProviderClient
 import com.google.android.gms.location.LocationServices
 import com.google.android.material.switchmaterial.SwitchMaterial
 import java.text.SimpleDateFormat
 import java.util.*
 import kotlin.math.roundToInt
 
 class MainActivity : AppCompatActivity() {
 
     private lateinit var compassView: CompassView
     private lateinit var tvBearing: TextView
     private lateinit var tvDirection: TextView
     private lateinit var tvInfo: TextView
     private lateinit var tvTime: TextView
     private lateinit var switchNorth: SwitchMaterial
 
     private lateinit var sensorHelper: SensorHelper
     private lateinit var fusedLocationClient: FusedLocationProviderClient
 
     private var useTrueNorth = true
     private var currentMagneticBearing = 0f
     private var declination = 0f
 
     private val timeFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.CHINA)
 
     private val locationPermissionRequest = registerForActivityResult(
         ActivityResultContracts.RequestMultiplePermissions()
     ) { permissions ->
         if (permissions.all { it.value }) {
             startLocationUpdates()
         } else {
             tvInfo.text = getString(R.string.permission_location_denied)
         }
     }
 
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_main)
 
         compassView = findViewById(R.id.compassView)
         tvBearing = findViewById(R.id.tv_bearing)
         tvDirection = findViewById(R.id.tv_direction)
         tvInfo = findViewById(R.id.tv_info)
         tvTime = findViewById(R.id.tv_time)
         switchNorth = findViewById(R.id.switch_north)
 
         fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
         sensorHelper = SensorHelper(this)
 
         switchNorth.setOnCheckedChangeListener { _, isChecked ->
             useTrueNorth = isChecked
             switchNorth.text = if (isChecked) getString(R.string.true_north) else getString(R.string.magnetic_north)
             applyBearing()
         }
 
         sensorHelper.onAzimuthChanged = { azimuth ->
             currentMagneticBearing = azimuth
             runOnUiThread { applyBearing() }
         }
 
         checkAndRequestPermissions()
         startClock()
     }
 
     override fun onResume() {
         super.onResume()
         sensorHelper.register()
     }
 
     override fun onPause() {
         super.onPause()
         sensorHelper.unregister()
     }
 
     private fun checkAndRequestPermissions() {
         when {
             ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                 == PackageManager.PERMISSION_GRANTED -> {
                 startLocationUpdates()
             }
             else -> {
                 locationPermissionRequest.launch(
                     arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                 )
             }
         }
     }
 
     private fun startLocationUpdates() {
         if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
             != PackageManager.PERMISSION_GRANTED
         ) return
 
         fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
             location?.let {
                 sensorHelper.location = it
                 updateLocationInfo(it)
                 // Calculate declination
                 val field = android.hardware.GeomagneticField(
                     it.latitude.toFloat(), it.longitude.toFloat(), it.altitude.toFloat(),
                     System.currentTimeMillis()
                 )
                 declination = field.declination
             }
         }
     }
 
     private fun applyBearing() {
         val displayBearing = if (useTrueNorth) {
             var b = currentMagneticBearing + declination
             if (b < 0) b += 360
             if (b >= 360) b -= 360
             b
         } else {
             currentMagneticBearing
         }
 
         compassView.setBearing(displayBearing)
 
         val degrees = displayBearing.roundToInt()
         tvBearing.text = getString(R.string.degree_format, displayBearing)
         tvDirection.text = getDirectionName(displayBearing)
     }
 
     private fun getDirectionName(bearing: Float): String {
         val dirs = arrayOf(
             getString(R.string.direction_north),
             getString(R.string.direction_north_east),
             getString(R.string.direction_east),
             getString(R.string.direction_south_east),
             getString(R.string.direction_south),
             getString(R.string.direction_south_west),
             getString(R.string.direction_west),
             getString(R.string.direction_north_west)
         )
         val index = ((bearing + 22.5f) / 45f).toInt() % 8
         return dirs[index]
     }
 
     private fun updateLocationInfo(location: Location) {
         tvInfo.text = getString(
             R.string.location_format,
             location.longitude, location.latitude
         )
     }
 
     private fun startClock() {
         val clock = Runnable {
             tvTime.text = timeFormatter.format(Date())
             tvTime.postDelayed(this::startClock, 1000)
         }
         tvTime.post(clock)
     }
 }
