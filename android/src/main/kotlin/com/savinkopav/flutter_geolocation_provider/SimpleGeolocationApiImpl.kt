package com.savinkopav.flutter_geolocation_provider

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.PluginRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SimpleGeolocationImpl: SimpleGeolocationApi, PluginRegistry.RequestPermissionsResultListener {

    //coroutines support
    private val mainScope = CoroutineScope(Dispatchers.Main.immediate + Job())
    private val ioDispatcher = Dispatchers.IO

    private val locationListener = object : LocationListener {

        var locationUpdatesCallback: ((Result<Location>) -> Unit)? = null

        override fun onLocationChanged(location: android.location.Location) {
            Log.d(TAG, "onLocationChanged with the '${Thread.currentThread().name}' thread")
            val callback = locationUpdatesCallback
            removeLocationUpdates() //delete listener after getting result
            callback?.invoke(
                Result.success(
                    Location(
                        location.latitude,
                        location.longitude
                    )
                )
            )
        }

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    private var locationManager: LocationManager? = null
    private var connectivityManager: ConnectivityManager? = null
    private var activityPluginBinding: ActivityPluginBinding? = null
    private var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding? = null
    private var permissionCallback: ((Result<Unit>) -> Unit)? = null
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "SimpleGeolocationImpl"
        private const val LATITUDE = 0.0
        private const val LONGITUDE = 0.0
        private const val ACCESS_FINE_LOCATION_REQUEST_CODE = 10101010
    }

    fun onActivityAttach(
        pluginBinding: FlutterPlugin.FlutterPluginBinding,
        activityBinding: ActivityPluginBinding
    ) {
        Log.d(TAG, "onActivityAttach")
        this.activityPluginBinding = activityBinding
        this.flutterPluginBinding = pluginBinding
        this.activityPluginBinding?.addRequestPermissionsResultListener(this)
        this.locationManager =
            activityBinding.activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        this.connectivityManager =
            activityBinding.activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    }

    fun onActivityDetach() {
        Log.d(TAG, "onActivityDetach")
        removeLocationUpdates()
        this.activityPluginBinding?.removeRequestPermissionsResultListener(this)
        this.activityPluginBinding = null
        this.flutterPluginBinding = null
        this.locationManager = null
        this.connectivityManager = null
        mainScope.coroutineContext.cancelChildren()
    }

    override fun requestLocationPermission(callback: (Result<Unit>) -> Unit) {
        Log.d(TAG, "requestLocationPermission")
        if (ContextCompat.checkSelfPermission(
                activityPluginBinding!!.activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionCallback = callback
            requestPermissions(
                activityPluginBinding!!.activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                ACCESS_FINE_LOCATION_REQUEST_CODE
            )
        } else {
            callback(Result.success(Unit))
        }
    }

    override fun getLastLocation(callback: (Result<Location>) -> Unit) {
        Log.d(TAG, "getLastLocation")
        mainScope.launch {
            callback.invoke(Result.success(getLastLocation()))
        }
    }

    private suspend fun getLastLocation(): Location = withContext(ioDispatcher) {
        locationManager?.let {
            Log.d(TAG, "locationManager = $it")
            it.getLastKnownLocation(
                LocationManager.GPS_PROVIDER
            )?.let { gpsLocationResult ->
                Log.d(TAG, "gpsLocationResult = ${gpsLocationResult.latitude}, ${gpsLocationResult.longitude}")
                provideLocationFromCoordinates(gpsLocationResult.latitude, gpsLocationResult.longitude)
            } ?: run {
                Log.d(TAG, "gpsLocationResult = null")
                it.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER
                )?.let { networkLocationResult ->
                    Log.d(TAG, "networkLocationResult = ${networkLocationResult.latitude}, ${networkLocationResult.longitude}")
                    provideLocationFromCoordinates(networkLocationResult.latitude, networkLocationResult.longitude)
                } ?: run {
                    Log.d(TAG, "networkLocationResult = null")
                    provideLocationFromCoordinates(LATITUDE, LONGITUDE)
                }
            }
        } ?: run {
            Log.d(TAG, "locationManager = null")
            provideLocationFromCoordinates(LATITUDE, LONGITUDE)
        }
    }

    override fun requestLocationUpdates(callback: (Result<Location>) -> Unit) {
        Log.d(TAG, "requestLocationUpdates with '${Thread.currentThread().name}' thread")

        mainScope.launch {
            try {
                if (!isGpsConnected()) {
                    callback.invoke(Result.failure(LocationProviderDenied()))
                    return@launch
                }
                if (!isNetworkConnected()) {
                    callback.invoke(Result.failure(NetworkProviderDenied()))
                    return@launch
                }
            } catch (e: Exception) {
                callback.invoke(Result.failure(IllegalStateException().initCause(e)))
                return@launch
            }

            try {
                locationListener.locationUpdatesCallback = callback

                locationManager?.let {
                    it.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0,
                        0f,
                        locationListener,
                    )
                    it.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0,
                        0f,
                        locationListener,
                    )
                }

                handler.postDelayed({
                    removeLocationUpdates()
                    callback.invoke(Result.failure(ProviderNotResponding()))
                }, 10000L)
            } catch (e: Exception) {
                callback.invoke(Result.failure(IllegalStateException().initCause(e)))
                return@launch
            }
        }
    }

    private suspend fun isNetworkConnected(): Boolean = withContext(ioDispatcher) {
        Log.d(TAG, "isNetworkConnected")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager!!.getNetworkCapabilities(connectivityManager!!.activeNetwork)

            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return@withContext true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return@withContext true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return@withContext true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return@withContext true
            }
        }
        false
    }

    private suspend fun isGpsConnected(): Boolean = withContext(ioDispatcher) {
        Log.d(TAG, "isGpsConnected")
        locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun removeLocationUpdates() {
        Log.d(TAG, "removeLocationUpdates")
        locationManager?.removeUpdates(locationListener.apply {
            locationUpdatesCallback = null
        })
        handler.removeCallbacksAndMessages(null)
    }

    private fun provideLocationFromCoordinates(lat: Double?, long: Double?): Location {
        Log.d(TAG, "provideLocationFromCoordinates")
        return Location(latitude = lat, longitude = long)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        Log.d(TAG, "onRequestPermissionsResult")
        return when (requestCode) {
            ACCESS_FINE_LOCATION_REQUEST_CODE -> {
                val callback = permissionCallback
                permissionCallback = null
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    callback!!.invoke(Result.success(Unit))
                } else {
                    if (shouldShowRequestPermissionRationale(
                            activityPluginBinding!!.activity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) { //TODO returns false when clicked outside the dialog
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

    override fun dispose() {
        Log.d(TAG, "dispose")

        removeLocationUpdates()
    }
}