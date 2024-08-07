import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:async';
import 'package:flutter_geolocation_provider/pigeon.dart';
import 'package:flutter_geolocation_provider/geolocation_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
  ]);
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
  bool isLoading = false;
  final _simpleGeolocationProviderPlugin = GeolocationService();

  @override
  dispose() {
    _simpleGeolocationProviderPlugin.dispose();
    super.dispose();
  }

  Future<void> getLocation(BuildContext context) async {
    setState(() {
      _latitude = 'Unknown';
      _longitude = 'Unknown';
      isLoading = true;
    });

    Location location;
    try {
      await _simpleGeolocationProviderPlugin.requestLocationPermission();
      await _simpleGeolocationProviderPlugin.requestLocationUpdates();
      location = await _simpleGeolocationProviderPlugin.getLastLocation();
    } catch (e) {
      if (e is PlatformException) {
        if (e.message != null) {
          if (!context.mounted) return;

          if (e.message!.contains("LocationAccessDenied")) {
            await _showInfoDialog(context, "Location access required", "Application needs geolocation access for work");
          } else if (e.message!.contains("LocationAccessPermanentlyDenied")) {
            await _showInfoDialog(context, "Location access denied", "You can provide access via settings");
          } else if (e.message!.contains("LocationProviderDenied")) {
            await _showInfoDialog(context, "GPS provider unavailable", "Please check your gps service");
          } else if (e.message!.contains("NetworkProviderDenied")) {
            await _showInfoDialog(context, "Network provider unavailable", "Please check your network connection");
          } else if (e.message!.contains("ProviderNotResponding")) {
            await _showInfoDialog(context, "Provider does not answer", "Make sure you have gps services enabled and an internet connection and try again");
          }
        }
      }
      location = Location();
    }

    if (!context.mounted) return;

    setState(() {
      _latitude = location.latitude?.toString() ?? 'no result';
      _longitude = location.longitude?.toString() ?? 'no result';
      isLoading = false;
    });
  }

  Future<void> _showInfoDialog(BuildContext context, String title, String content) async {
    return await showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: Text(title),
            content: Text(content),
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
              child: !isLoading ? MaterialButton(
                color: Colors.grey.shade800,
                onPressed: () => getLocation(context),
                child: const Text('Get location'),
              ) : const CircularProgressIndicator(),
            )
          ],
        ),
      ),
    );
  }
}
