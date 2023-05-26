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
  final _simpleGeolocationProviderPlugin = GeolocationServiceImpl();
  // final _simpleGeolocationProviderFlutterPlugin = SimpleGeolocationFlutterApi();
  bool _cancelJob = false;

  @override
  void initState() {
    super.initState();
    init();
  }

  Future<void> init() async {
    Location location;
    try {
      print("logger -- before requestLocationUpdates");
      // _simpleGeolocationProviderPlugin.onLocationUpdates(); //TODO there is no callback? wtf? throw via EventLoop?!
      print("logger -- before _delayJob");
      await _delayJob();
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

  Future<bool> _delayJob() async {
    return await _job() ?? await _delayJob();
  }

  Future<bool?> _job() async {
    await Future.delayed(const Duration(milliseconds: 500));
    if (!_cancelJob) {
      return null;
    } else {
      _cancelJob = !_cancelJob;
      return true;
    }
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
