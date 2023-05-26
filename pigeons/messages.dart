import 'package:pigeon/pigeon.dart';

@HostApi()
abstract class SimpleGeolocationApi {

  @async
  void requestLocationPermission();

  Location getLastLocation();

  @async
  Location requestLocationUpdates();

  void removeLocationUpdates();
}


class Location {

  double? latitude;
  double? longitude;

  Location({this.latitude, this.longitude});
}