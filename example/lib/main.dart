import 'package:flutter/material.dart';
import 'dart:async';
import 'package:flutter/services.dart';
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
    init();
  }

  Future<void> init() async {
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
    } catch(_) { // if we don't have ACCESS_FINE_LOCATION permission for example
      location = Location();
    }

    if (!mounted) return;

    setState(() {
      _latitude = location.latitude?.toString() ?? "1";
      _longitude = location.longitude?.toString() ?? "2";
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
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
            ],
          ),
        ),
      ),
    );
  }
}
