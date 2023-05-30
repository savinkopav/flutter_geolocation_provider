package com.savinkopav.flutter_geolocation_provider

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
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
    private var connectivityManager: ConnectivityManager? = null
    private var activityPluginBinding: ActivityPluginBinding? = null
    private var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding? = null //TODO: last upd
    private var permissionCallback: ((Result<Unit>) -> Unit)? = null

    companion object {
        private const val TAG = "SimpleGeolocationImpl"
        private const val LATITUDE = 0.0
        private const val LONGITUDE = 0.0
        private const val ACCESS_FINE_LOCATION = 10000000
    }

    fun onActivityAttach(pluginBinding: FlutterPlugin.FlutterPluginBinding, activityBinding: ActivityPluginBinding) {
        Log.d(TAG, "onActivityAttach")
        this.activityPluginBinding = activityBinding
        this.flutterPluginBinding = pluginBinding
        activityPluginBinding?.addRequestPermissionsResultListener(this)
        locationManager = activityBinding.activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        connectivityManager = activityBinding.activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    }

    fun onActivityDetach() {
        Log.d(TAG, "onActivityDetach")
        activityPluginBinding?.removeRequestPermissionsResultListener(this)
        this.activityPluginBinding = null
        this.flutterPluginBinding = null
        this.locationManager = null
        this.connectivityManager = null
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

        try {
            if (!isGpsConnected()) {
                callback.invoke(Result.failure(LocationProviderDenied()))
                return
            }
            if (!isNetworkConnected()) {
                callback.invoke(Result.failure(NetworkProviderDenied()))
                return
            }
        } catch (e: Exception) {
            callback.invoke(Result.failure(IllegalStateException().initCause(e)))
            return
        }

        try {
            locationManager?.let {
                it.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0f,
                    gpsLocationListener.apply { platformCallback = callback }
                )
                it.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0,
                    0f,
                    networkLocationListener.apply { platformCallback = callback }
                )
            }
        } catch (e: Exception) {
            callback.invoke(Result.failure(IllegalStateException().initCause(e)))
        }
    }

    private fun isNetworkConnected(): Boolean {
        Log.d(TAG, "isNetworkConnected")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager!!.getNetworkCapabilities(connectivityManager!!.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }

    private fun isGpsConnected(): Boolean {
        Log.d(TAG, "isGpsConnected")
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
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
                    if (shouldShowRequestPermissionRationale(activityPluginBinding!!.activity, Manifest.permission.ACCESS_FINE_LOCATION)) { //TODO returns false when clicked outside the dialog
                        callback!!.invoke(Result.failure(LocationAccessDenied()))
                    } else {
                        callback!!.invoke(Result.failure(LocationAccessPermanentlyDenied()))
                    }
                }
                true
            }
            else -> false
        }
    }
}

class LocationAccessDenied: Exception("LocationAccessDenied")
class LocationAccessPermanentlyDenied: Exception("LocationAccessPermanentlyDenied")
class LocationProviderDenied: Exception("LocationProviderDenied")
class NetworkProviderDenied: Exception("NetworkProviderDenied")