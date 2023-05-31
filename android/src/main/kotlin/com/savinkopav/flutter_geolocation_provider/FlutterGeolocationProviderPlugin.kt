package com.savinkopav.flutter_geolocation_provider

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

/** FlutterGeolocationProviderPlugin */
class FlutterGeolocationProviderPlugin: FlutterPlugin, ActivityAware {

  private var activityPluginBinding: ActivityPluginBinding? = null
  private var simpleGeolocationApi: SimpleGeolocationImpl? = null
  private var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding? = null

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
    activityPluginBinding = binding
    flutterPluginBinding?.let {
      simpleGeolocationApi?.onActivityAttach(it, activityPluginBinding!!)
    }
  }

  override fun onDetachedFromActivity() {
    simpleGeolocationApi?.onActivityDetach()
    activityPluginBinding = null
  }

  override fun onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity()
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(binding)
  }
}
