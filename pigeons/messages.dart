import 'package:pigeon/pigeon.dart';

@HostApi()
abstract class SimpleGeolocationApi {
  Location getLastLocation();

  @async
  Location requestLocationUpdate();
}

class Location {

  double? latitude;
  double? longitude;

  Location({this.latitude, this.longitude});
}