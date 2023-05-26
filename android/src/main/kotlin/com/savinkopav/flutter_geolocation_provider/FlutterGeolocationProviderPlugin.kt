package com.savinkopav.flutter_geolocation_provider

import android.app.Activity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

/** FlutterGeolocationProviderPlugin */
class FlutterGeolocationProviderPlugin: FlutterPlugin, ActivityAware {

  private var activity: Activity? = null
  private var simpleGeolocationApi: SimpleGeolocationImpl? = null
  private var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding? = null //TODO: last upd

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    this.flutterPluginBinding = flutterPluginBinding
    simpleGeolocationApi = SimpleGeolocationImpl()
    SimpleGeolocationApi.setUp(flutterPluginBinding.binaryMessenger, simpleGeolocationApi)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    this.flutterPluginBinding = null
    SimpleGeolocationApi.setUp(binding.binaryMessenger, null)
    simpleGeolocationApi = null
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
    flutterPluginBinding?.let {
      simpleGeolocationApi?.onActivityAttach(it, activity!!)
    }
  }

  override fun onDetachedFromActivity() {
    simpleGeolocationApi?.onActivityDetach()
    activity = null
  }

  override fun onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity()
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(binding)
  }
}
