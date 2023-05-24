package com.savinkopav.flutter_geolocation_provider

import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log

class SimpleGeolocationImpl(context: Context) : SimpleGeolocationApi {

    private val gpsLocationListener = LocationListener {
        removeListeners()
    }
    private val networkLocationListener = LocationListener {
        removeListeners()
    }
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

    companion object {
        private const val TAG = "SimpleGeolocationImpl"
    }

    override fun getLastLocation(): Location {
        requestLocationUpdates()

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
                    return provideLocationFromCoordinates(53.0, 27.0)
                }
            }
        } ?: run {
            Log.d(TAG, "locationManager = null")
            return provideLocationFromCoordinates(53.0, 27.0)
        }
    }

    override fun requestLocationUpdate(callback: (Result<Location>) -> Unit) {

    }

    private fun provideLocationFromCoordinates(lat: Double?, long: Double?) : Location {
        return Location().apply {
            latitude = lat
            longitude = long
        }
    }

    private fun requestLocationUpdates() {
        locationManager?.let {
            it.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, gpsLocationListener)
            it.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0f, networkLocationListener)
        }
    }

    private fun removeListeners() {
        locationManager?.removeUpdates(gpsLocationListener)
        locationManager?.removeUpdates(networkLocationListener)
    }
}