import 'package:flutter_geolocation_provider/pigeon.dart';

abstract class GeolocationService extends SimpleGeolocationFlutterApi {

  static GeolocationService? _instance;

  static GeolocationService get instance {
    if (_instance != null) {
      return _instance!;
    }
    _instance = GeolocationServiceImpl();
    return _instance!;
  }

  Future<Location> getLastLocation();

  Future<void> removeLocationUpdates();
}

class GeolocationServiceImpl extends GeolocationService {

  final SimpleGeolocationApi _simpleGeolocationApi = SimpleGeolocationApi();

  GeolocationServiceImpl() {
    SimpleGeolocationFlutterApi.setup(this);
  }

  @override
  void onLocationUpdates(Location location) {

  }

  @override
  Future<Location> getLastLocation() {
    return _simpleGeolocationApi.getLastLocation();
  }

  @override
  Future<void> removeLocationUpdates() {
    return _simpleGeolocationApi.removeLocationUpdates();
  }

}