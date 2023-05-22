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
  Location _platformVersion = Location();
  final _flutterGeolocationProviderPlugin = SimpleGeolocationApi();

  @override
  void initState() {
    super.initState();
    init();
  }

  Future<void> init() async {
    try {
      _platformVersion = await _flutterGeolocationProviderPlugin.getLastLocation();
    } on PlatformException {
      _platformVersion = Location();
    }

    if (!mounted) return;

    setState(() {

    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            Text('latitude: ${_platformVersion.latitude}\n'),
            Text('longitude: ${_platformVersion.longitude}\n'),
          ],
        ),
      ),
    );
  }
}
