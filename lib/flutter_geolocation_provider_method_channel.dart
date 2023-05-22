import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_geolocation_provider_platform_interface.dart';

/// An implementation of [FlutterGeolocationProviderPlatform] that uses method channels.
class MethodChannelFlutterGeolocationProvider extends FlutterGeolocationProviderPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_geolocation_provider');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
