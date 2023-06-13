package com.savinkopav.flutter_geolocation_provider

class LocationAccessDenied: Exception("LocationAccessDenied")
class LocationAccessPermanentlyDenied: Exception("LocationAccessPermanentlyDenied")
class LocationProviderDenied: Exception("LocationProviderDenied")
class NetworkProviderDenied: Exception("NetworkProviderDenied")
class ProviderNotResponding: Exception("ProviderNotResponding")