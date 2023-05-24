import 'package:flutter/material.dart';
import 'dart:async';
import 'package:flutter/services.dart';
import 'package:flutter_geolocation_provider/flutter_geolocation_provider.dart';
import 'package:flutter_geolocation_provider/pigeon.dart';

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
  String _platformVersion = "unknown";
  final _simpleGeolocationProviderPlugin = SimpleGeolocationApi();
  final _simpleGeolocationProviderPlugin2 = FlutterGeolocationProvider();

  @override
  void initState() {
    super.initState();
    init();
  }

  Future<void> init() async {
    Location location;
    String? version;
    try {
      location = await _simpleGeolocationProviderPlugin.getLastLocation();
      version = await _simpleGeolocationProviderPlugin2.getPlatformVersion();
    } on PlatformException {
      location = Location();
    }

    if (!mounted) return;

    setState(() {
      _latitude = location.latitude?.toString() ?? "1";
      _longitude = location.longitude?.toString() ?? "2";
      _platformVersion = version ?? "Unknown";
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
              Text('version: $_platformVersion'),
            ],
          ),
        ),
      ),
    );
  }
}
