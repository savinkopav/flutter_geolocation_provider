import 'package:flutter/material.dart';
import 'dart:async';
import 'package:flutter/services.dart';
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
  final _simpleGeolocationProviderPlugin = SimpleGeolocationApi();

  @override
  void initState() {
    super.initState();
    init();
  }

  Future<void> init() async {
    Location location;
    try {
      location = await _simpleGeolocationProviderPlugin.getLastLocation();
    } on PlatformException {
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
