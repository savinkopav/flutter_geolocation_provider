
import 'flutter_geolocation_provider_platform_interface.dart';

class FlutterGeolocationProvider {
  Future<String?> getPlatformVersion() {
    return FlutterGeolocationProviderPlatform.instance.getPlatformVersion();
  }
}
