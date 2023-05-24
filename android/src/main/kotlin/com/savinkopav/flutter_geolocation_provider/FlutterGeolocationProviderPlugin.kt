package com.savinkopav.flutter_geolocation_provider

import io.flutter.embedding.engine.plugins.FlutterPlugin

/** FlutterGeolocationProviderPlugin */
class FlutterGeolocationProviderPlugin: FlutterPlugin {

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    SimpleGeolocationApi.setUp(flutterPluginBinding.binaryMessenger, SimpleGeolocationImpl(flutterPluginBinding.applicationContext))
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    SimpleGeolocationApi.setUp(binding.binaryMessenger, null)
  }
}
