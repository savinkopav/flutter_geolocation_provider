import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_geolocation_provider/flutter_geolocation_provider.dart';
import 'package:flutter_geolocation_provider/flutter_geolocation_provider_platform_interface.dart';
import 'package:flutter_geolocation_provider/flutter_geolocation_provider_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterGeolocationProviderPlatform
    with MockPlatformInterfaceMixin
    implements FlutterGeolocationProviderPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterGeolocationProviderPlatform initialPlatform = FlutterGeolocationProviderPlatform.instance;

  test('$MethodChannelFlutterGeolocationProvider is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterGeolocationProvider>());
  });

  test('getPlatformVersion', () async {
    FlutterGeolocationProvider flutterGeolocationProviderPlugin = FlutterGeolocationProvider();
    MockFlutterGeolocationProviderPlatform fakePlatform = MockFlutterGeolocationProviderPlatform();
    FlutterGeolocationProviderPlatform.instance = fakePlatform;

    expect(await flutterGeolocationProviderPlugin.getPlatformVersion(), '42');
  });
}
