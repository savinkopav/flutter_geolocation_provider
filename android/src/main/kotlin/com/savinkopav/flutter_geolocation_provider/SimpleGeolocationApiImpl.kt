package com.savinkopav.flutter_geolocation_provider

import android.content.Context
import android.location.LocationManager

class SimpleGeolocationImpl(private val context: Context) : SimpleGeolocationApi {

    override fun getLastLocation(): Location {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        locationManager?.let {
            it.getLastKnownLocation(
                LocationManager.GPS_PROVIDER
            )?.let { gpsLocationResult ->
                return provideLocationFromCoordinates(gpsLocationResult.latitude, gpsLocationResult.longitude)
            } ?: run {
                it.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER
                )?.let { networkLocationResult ->
                    return provideLocationFromCoordinates(networkLocationResult.latitude, networkLocationResult.longitude)
                } ?: run {
                    return provideLocationFromCoordinates(null, null)
                }
            }
        } ?: run {
            return provideLocationFromCoordinates(null, null)
        }
    }

    private fun provideLocationFromCoordinates(lat: Double?, long: Double?) : Location {
        return Location().apply {
            latitude = lat
            longitude = long
        }
    }
}