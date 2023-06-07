import 'package:pigeon/pigeon.dart';

@HostApi()
abstract class SimpleGeolocationApi {

  @async
  void requestLocationPermission();

  @async
  Location getLastLocation();

  @async
  Location requestLocationUpdates();

}


class Location {

  double? latitude;
  double? longitude;

  Location({this.latitude, this.longitude});
}