import 'package:pigeon/pigeon.dart';

@HostApi()
abstract class SimpleGeolocationApi {

  Location getLastLocation();

  // @async
  // Location requestLocationUpdates(Location Function()? callback);

  void removeLocationUpdates();
}

@FlutterApi()
abstract class SimpleGeolocationFlutterApi {
  void onLocationUpdates(Location location);
}

class Location {

  double? latitude;
  double? longitude;

  Location({this.latitude, this.longitude});
}