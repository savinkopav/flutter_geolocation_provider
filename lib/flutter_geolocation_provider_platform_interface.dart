import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_geolocation_provider_method_channel.dart';

abstract class FlutterGeolocationProviderPlatform extends PlatformInterface {
  /// Constructs a FlutterGeolocationProviderPlatform.
  FlutterGeolocationProviderPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterGeolocationProviderPlatform _instance = MethodChannelFlutterGeolocationProvider();

  /// The default instance of [FlutterGeolocationProviderPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterGeolocationProvider].
  static FlutterGeolocationProviderPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterGeolocationProviderPlatform] when
  /// they register themselves.
  static set instance(FlutterGeolocationProviderPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
