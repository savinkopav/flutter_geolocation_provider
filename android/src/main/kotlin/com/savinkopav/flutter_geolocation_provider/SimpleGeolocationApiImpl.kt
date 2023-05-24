package com.savinkopav.flutter_geolocation_provider

import android.content.Context
import android.location.LocationManager
import android.util.Log

class SimpleGeolocationImpl(private val context: Context) : SimpleGeolocationApi {

    companion object {
        private const val TAG = "SimpleGeolocationImpl"
    }

    override fun getLastLocation(): Location {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        Log.d(TAG, "locationManager = $locationManager")
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

    private fun provideLocationFromCoordinates(lat: Double?, long: Double?) : Location {
        return Location().apply {
            latitude = lat
            longitude = long
        }
    }
}