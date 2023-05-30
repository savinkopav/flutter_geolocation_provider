import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:async';
import 'package:flutter_geolocation_provider/pigeon.dart';
import 'package:flutter_geolocation_provider/geolocation_service.dart';

void main() {
  runApp(const Main());
}

class Main extends StatelessWidget {
  const Main({super.key});

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
      home: const MyApp(),
    );
  }
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

  @override
  void initState() {
    super.initState();
  }

  Future<void> getLocation(BuildContext context) async {
    setState(() {
      _latitude = 'Unknown';
      _longitude = 'Unknown';
    });

    Location location;
    try {
      print("logger -- before requestLocationPermission");
      await _simpleGeolocationProviderPlugin.requestLocationPermission();
      print("logger -- before requestLocationUpdates");
      await _simpleGeolocationProviderPlugin.requestLocationUpdates();
      print("logger -- before removeLocationUpdates");
      await _simpleGeolocationProviderPlugin.removeLocationUpdates();
      print("logger -- before getLastLocation");
      location = await _simpleGeolocationProviderPlugin.getLastLocation();
    } catch (e) {
      if (e is PlatformException) {
        if (e.message != null && e.message!.contains("LocationAccessDenied")) {
          await _showInfoDialog(context, "Location access required", "Application needs geolocation access to work");
        }
      }
      location = Location();
    }

    if (!mounted) return;

    setState(() {
      _latitude = location.latitude?.toString() ?? '1';
      _longitude = location.longitude?.toString() ?? '2';
    });
  }

  Future<void> _showInfoDialog(BuildContext context, String title, String content) async {
    return await showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: const Text("Need location access"),
            content: const Text("Application needs geolocation permission for work"),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(),
                child: const Text('Close'),
              ),
            ],
          );
        });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text('latitude: $_latitude'),
            Text('longitude: $_longitude'),
            Padding(
              padding: const EdgeInsets.all(5.0),
              child: MaterialButton(
                color: Colors.grey.shade800,
                onPressed: () => getLocation(context),
                child: const Text('Get location'),
              ),
            )
          ],
        ),
      ),
    );
  }
}
