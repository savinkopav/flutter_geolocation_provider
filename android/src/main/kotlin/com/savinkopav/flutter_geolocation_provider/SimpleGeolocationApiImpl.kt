package com.savinkopav.flutter_geolocation_provider

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.PluginRegistry

class SimpleGeolocationImpl: SimpleGeolocationApi, PluginRegistry.RequestPermissionsResultListener {

    private val gpsLocationListener = object : LocationListener {

        var platformCallback: ((Result<Location>) -> Unit)? = null

        override fun onLocationChanged(location: android.location.Location) {
            Log.d(TAG, "onLocationChanged - GPS")
            platformCallback?.invoke(Result.success(Location(location.latitude, location.longitude)))
        }
    }

    private val networkLocationListener = object : LocationListener {

        var platformCallback: ((Result<Location>) -> Unit)? = null

        override fun onLocationChanged(location: android.location.Location) {
            Log.d(TAG, "onLocationChanged - NETWORK")
            platformCallback?.invoke(Result.success(Location(location.latitude, location.longitude)))
        }
    }

    private var locationManager: LocationManager? = null
    private var activityPluginBinding: ActivityPluginBinding? = null
    private var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding? = null //TODO: last upd
    private var permissionCallback: ((Result<Unit>) -> Unit)? = null

    companion object {
        private const val TAG = "SimpleGeolocationImpl"
        private const val LATITUDE = 53.0
        private const val LONGITUDE = 27.0

        private const val ACCESS_FINE_LOCATION = 10000000
        private const val ACCESS_COARSE_LOCATION = 10000001
    }

    fun onActivityAttach(pluginBinding: FlutterPlugin.FlutterPluginBinding, activityBinding: ActivityPluginBinding) {
        Log.d(TAG, "onActivityAttach")
        this.activityPluginBinding = activityBinding
        this.flutterPluginBinding = pluginBinding
        activityPluginBinding?.addRequestPermissionsResultListener(this)
        locationManager = activityBinding.activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    }

    fun onActivityDetach() {
        Log.d(TAG, "onActivityDetach")
        activityPluginBinding?.removeRequestPermissionsResultListener(this)
        this.activityPluginBinding = null
        this.flutterPluginBinding = null
        this.locationManager = null
    }

    override fun requestLocationPermission(callback: (Result<Unit>) -> Unit) {
        Log.d(TAG, "requestLocationPermission")
        if (ContextCompat.checkSelfPermission(activityPluginBinding!!.activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionCallback = callback
            requestPermissions(activityPluginBinding!!.activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION)
        } else {
            callback(Result.success(Unit))
        }
    }

    override fun getLastLocation(): Location {
        Log.d(TAG, "getLastLocation")
        locationManager?.let {
            Log.d(TAG, "locationManager = $it")
            it.getLastKnownLocation(
                LocationManager.GPS_PROVIDER
            )?.let { gpsLocationResult ->
                Log.d(TAG, "gpsLocationResult = ${gpsLocationResult.latitude}, ${gpsLocationResult.longitude}")
                return provideLocationFromCoordinates(gpsLocationResult.latitude, gpsLocationResult.longitude)
            } ?: run {
                Log.d(TAG, "gpsLocationResult = null")
                it.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER
                )?.let { networkLocationResult ->
                    Log.d(TAG, "networkLocationResult = ${networkLocationResult.latitude}, ${networkLocationResult.longitude}")
                    return provideLocationFromCoordinates(networkLocationResult.latitude, networkLocationResult.longitude)
                } ?: run {
                    Log.d(TAG, "networkLocationResult = null")
                    return provideLocationFromCoordinates(LATITUDE, LONGITUDE)
                }
            }
        } ?: run {
            Log.d(TAG, "locationManager = null")
            return provideLocationFromCoordinates(LATITUDE, LONGITUDE)
        }
    }

    override fun requestLocationUpdates(callback: (Result<Location>) -> Unit) {
        Log.d(TAG, "requestLocationUpdates")
        locationManager?.let {
            it.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, gpsLocationListener.apply { platformCallback = callback })
            it.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, networkLocationListener.apply { platformCallback = callback })
        }
    }

    override fun removeLocationUpdates() {
        Log.d(TAG, "removeLocationUpdates")
        locationManager?.removeUpdates(gpsLocationListener)
        locationManager?.removeUpdates(networkLocationListener)
    }

    private fun provideLocationFromCoordinates(lat: Double?, long: Double?) : Location {
        Log.d(TAG, "provideLocationFromCoordinates")
        return Location().apply {
            latitude = lat
            longitude = long
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        Log.d(TAG, "onRequestPermissionsResult")
        return when (requestCode) {
            ACCESS_FINE_LOCATION -> {
                val callback = permissionCallback
                permissionCallback = null
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    callback!!.invoke(Result.success(Unit))
                } else {
                    callback!!.invoke(Result.failure(LocationAccessDenied()))
                }
                true
            }
            else -> false
        }
    }
}

class LocationAccessDenied: Exception("LocationAccessDenied")