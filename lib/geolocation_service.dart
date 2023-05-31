import 'package:flutter_geolocation_provider/pigeon.dart';

class GeolocationService {

  static final GeolocationService _instance = GeolocationService._();
  factory GeolocationService() {
    return _instance;
  }

  late SimpleGeolocationApi _simpleGeolocationApi;

  GeolocationService._() {
    _simpleGeolocationApi = SimpleGeolocationApi();
  }

  Future<Location> getLastLocation() {
    return _simpleGeolocationApi.getLastLocation();
  }

  Future<Location> requestLocationUpdates() {
    return _simpleGeolocationApi.requestLocationUpdates();
  }

  Future<void> requestLocationPermission() {
    return _simpleGeolocationApi.requestLocationPermission();
  }

}