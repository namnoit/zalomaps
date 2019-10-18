# zalomaps

## Pre-requisites
This project require an up-to-date version of the Android build tools and the Android support repository.

## Getting started
First, clone this repository. Then import to Android Studio and remember to add Google Maps API key to `google_maps_api.xml`.

### How to get API key
To use the Maps SDK and Places SDK for Android you must have at least one API key.
To get an API key:
- Visit the [Google Cloud Platform Console](https://cloud.google.com/console/google/maps-apis/overview)
- Select the project for which you want to add an API key and make sure you enabled Maps SDK and Places SDK for Android.
- Click the menu button  and select APIs & Services > Credentials.
- On the Credentials page, click Create credentials > API key. 
The API key created dialog displays your newly created API key.
- Click Close. 
The new API key is listed on the Credentials page under API keys. 
- Restrict the API key (with package name and SHA-1 certificate fingerprint of your project).

Note: Places SDK use the same key with Maps SDK.\
For more detail, you can visit [Get an API key](https://developers.google.com/maps/documentation/android-sdk/get-api-key)

## Core components in this project
### GoogleMap
### f

## Usage of Google Maps SDK

To use map, embed fragment with `name="com.google.android.gms.maps.SupportMapFragment"` to your layout source file:
```sh
<fragment xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:id="@+id/map"
    tools:context=".MapActivity"
    android:name="com.google.android.gms.maps.SupportMapFragment" />
```
In activity Java file, implements OnMapReadyCallback, override onMapReady() and call MapFragment.getMapAsync() in onCreate() of activity.
<img src="https://imgur.com/a/xAHhEIe">

### Marker

Marker is used to show bookmark in map. 
To add marker to map, call addMarker() on GoogleMap object, and pass a MarkerOption which define some infomation of the marker, such as **position** (LatLng), icon, title, snippet,...
Each marker has a unique identifier, and can store a reference to a place model that save in list, so I save markers to a list for manage easilier.\
Markers is clickable like views.
