package com.savinkopav.flutter_geolocation_provider_example

import com.savinkopav.flutter_geolocation_provider.SimpleGeolocationApi
import com.savinkopav.flutter_geolocation_provider.SimpleGeolocationImpl
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity: FlutterActivity() {

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        SimpleGeolocationApi.setUp(flutterEngine.dartExecutor.binaryMessenger, SimpleGeolocationImpl(this))
    }
}
