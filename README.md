# zalomaps


### Screenshots
<img src=https://i.imgur.com/DSNcnH5.png width=200><img src=https://i.imgur.com/undxxSW.png width=200>
<img src=https://i.imgur.com/bluwwWd.png width=200>
<img src=https://i.imgur.com/LBfmJrt.png width=200>
<img src=https://i.imgur.com/ZGKeZM1.png width=200>

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

### SQLite Database

SQLiteOpenHelper is used for storing saved places as SQLite database which are bookmarked by user. Saved fields include *latitude, longitude, tag, notes, anddress,...*, in which tag, notes is entered by user, latitude, longitude is get from Maps API, and address is get from Location API.\
The following methods are used for interacting with database:
- *insertPlace()* - To insert new marked place to database.
- *updatePlace()* - To update exist place in database.
- *delete()* - To remove a place from database.
- *getAllPlaces()* - Return all of saved places in database.
- *getLatestPlace()* - Return latest saved place in database.

### Android Location API

To use Location API, app must request location permission.\
In Manifest:
```sh
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```
Request runtime permission:
```sh
if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
    mLocationPermissionGranted = true;
} else {
    ActivityCompat.requestPermissions(this,
            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
}
```
#### Access my location

To access last known location
```sh
LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
```
Now you can get location by call *getLatitude()* and *getLongitude()* on location instance.

#### Retrieve address
Location API is used for retrieving address from LatLng input. You can get address by pass latitude and longitude to Geocoder object, and get address from it:
```sh
Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
List<Address> addresses;
String address;
try {
    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
    address = addresses.get(0).getAddressLine(0);
} catch (IOException e) {
    e.printStackTrace();
    address = "Not found";
}
```
Result:\
<img src=https://i.imgur.com/03EuFXI.png height="100">

### Material Components
Material components is used for building a modern UI.\
In the dependencies section of app-level **build.gradle** file, add a dependency:
```sh
implementation 'com.google.android.material:material:1.1.0-beta01'
```

#### Chips
<img src=https://i.imgur.com/b5j5kVd.png height="50">
<img src=https://i.imgur.com/8OxiWCz.png height="150">

#### Material TextField
<img src=https://i.imgur.com/mxufhss.png height="200">

#### Floating Action Button
<img src=https://i.imgur.com/uo5g5ZM.png height="150">

#### Bottom sheet
<img src=https://i.imgur.com/J0Z2FvS.png height="300">

## Usage of Google Maps SDK
In the dependencies section of app-level **build.gradle** file, add a dependency to the Maps SDK for Android:
```sh
implementation 'com.google.android.gms:play-services-maps:17.0.0'
```
### Add a map

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
In activity Java file, implements OnMapReadyCallback, override onMapReady() and call MapFragment.getMapAsync() in onCreate() of activity.\
Result:\
<img src=https://imgur.com/d9nRlCm.png width="200">

### Configure initial state

The Maps API allows you to configure the initial state of the map to suit your application's needs. You can specify the following:
- The camera position, including: location, zoom, bearing and tilt.
- The map type: Normal, Hybrid, Satellite, Terrain, None.
- Whether the zoom buttons and/or compass appear on screen.
- The gestures a user can use to manipulate the camera.
- Whether lite mode is enabled or not. A lite mode map is a bitmap image of a map that supports a subset of the functionality supplied by the full API.

You can configure the initial state of the map either via XML, if you have added the map to your activity's layout file:
```sh
map:mapType="normal"
```
or programmatically, if you have added the map that way:
```sh
GoogleMapOptions options = new GoogleMapOptions();
options.mapType(GoogleMap.MAP_TYPE_SATELLITE);
MapFragment mapFragment MapFragment.newInstance(GoogleMapOptions options);
```
### Marker

Marker is used to show bookmark in map. 
To add marker to map, call addMarker() on GoogleMap object, and pass a MarkerOption which define some infomation of the marker, such as **position** (LatLng), icon, title, snippet,...\
Each marker has a unique identifier, and can store a reference to a place model that save in list, so I save markers to a list for manage easilier.\
Markers is clickable like views.\
<img src=https://i.imgur.com/sy1xlLu.png width="200">

### Info Windows
Info Windows show title and snippet of markers: 
```sh
Marker hcmc = mMap.addMarker(new MarkerOptions()
                          .position(HCMC)
                          .title("Ho Chi Minh City"));
```
Where HCMC is a LatLng object.\
To show Info Windows, call *showInfoWindow()* on Marker instance.\
<img src=https://i.imgur.com/TXInKk0.png width="200">

## Usage Places SDK
- In the dependencies section of app-level **build.gradle** file, add a dependency to the Places SDK for Android:
```sh
implementation 'com.google.android.libraries.places:places:2.0.0'
```
- Initialize the Places API client:
```sh
// Initialize the SDK
Places.initialize(getApplicationContext(), apiKey);

// Create a new Places client instance
PlacesClient placesClient = Places.createClient(this);
```
### Places AutoComplete
App use autocomplete intent to perform search places feature to save bookmark. This will return a Place object which contains some infomation to use such as address, position, id,...\
Create an autocomplete intent:
```sh
int AUTOCOMPLETE_REQUEST_CODE = 1;
List<Place.Field> fields =
        Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
// Start the autocomplete intent.
Intent intent = new Autocomplete.IntentBuilder(
        AutocompleteActivityMode.OVERLAY, fields)
        .build(this);
startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
```
Override the onActivityResult callback:
```sh
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
        if (resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            // Get LatLng by call place.getLatLng()
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Log.i(TAG, status.getStatusMessage());
        }
    }
}
```

### 
