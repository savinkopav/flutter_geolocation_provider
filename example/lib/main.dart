import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:async';
import 'package:flutter_geolocation_provider/pigeon.dart';
import 'package:flutter_geolocation_provider/geolocation_service.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _latitude = 'Unknown';
  String _longitude = 'Unknown';
  final _simpleGeolocationProviderPlugin = GeolocationService();
  // bool _cancelJob = false;

  @override
  void initState() {
    super.initState();
  }

  Future<void> getLocation() async {
    setState(() {
      _latitude = 'Unknown';
      _longitude = 'Unknown';
    });

    Location location;
    try {
      print("logger -- before requestLocationPermission");
      await _simpleGeolocationProviderPlugin.requestLocationPermission();
      print("logger -- before requestLocationUpdates");
      await _simpleGeolocationProviderPlugin.requestLocationUpdates(); //TODO there is no callback? wtf? throw via EventLoop?!
      print("logger -- before removeLocationUpdates");
      await _simpleGeolocationProviderPlugin.removeLocationUpdates();
      print("logger -- before getLastLocation");
      location = await _simpleGeolocationProviderPlugin.getLastLocation();
    } catch(e, s) { // if we don't have ACCESS_FINE_LOCATION permission for example
      if (e is PlatformException) {
        print("logger -- e is PlatformException with message: ${e.message}");
        if (e.message!.contains("LocationAccessDenied")) {
          print("logger -- LocationAccessDenied");
          await showDialog(context: context, builder: (context) {
            return AlertDialog(
              title: const Text("LocationAccessDenied"),
              content: const Text("For usages application's functionality you should provice location permission"),
              actions: [
                    TextButton(
                      onPressed: () => Navigator.of(context).pop(),
                      child: const Text('cancel'),
                    ),
                  ],
            );
          });
        }
      }
      location = Location();
    }

    if (!mounted) return;

    setState(() {
      _latitude = location.latitude?.toString() ?? 'Unknown';
      _longitude = location.longitude?.toString() ?? 'Unknown';
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.dark,
        primaryColorDark: Colors.lightGreen[800],
        fontFamily: 'Georgia',
        textTheme: const TextTheme(
          displayLarge: TextStyle(fontSize: 18.0, fontWeight: FontWeight.bold),
          titleLarge: TextStyle(fontSize: 18.0),
          bodyMedium: TextStyle(fontSize: 14.0, fontFamily: 'Hind'),
        ),
      ),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text('latitude: $_latitude'),
              Text('longitude: $_longitude'),
              TextButton(
                onPressed: getLocation,
                child: const Text('Get location'),
              )
            ],
          ),
        ),
      ),
    );
  }
}
