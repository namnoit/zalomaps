package com.namnoit.zalomaps;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.namnoit.zalomaps.data.PlacesDatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnPoiClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnInfoWindowClickListener {
    private GoogleMap map;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        boolean permissionsAcepted = true;
        for (int grantResult : grantResults) {
            if (grantResult < 0) {
                permissionsAcepted = false;
            }
        }
        if (permissionsAcepted) {
            map.setMyLocationEnabled(true);
        } else {
            new MaterialAlertDialogBuilder(this, R.style.MaterialDialogStyle)
                    .setTitle(R.string.title_permission_denied)
                    .setMessage(R.string.message_permission_denied)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    private static final int PERMISSION_REQUEST_CODE = 1;
    private String[] appPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ListActivity.class);
                startActivityForResult(intent,1);
            }
        });
        PlacesDatabaseHelper db = PlacesDatabaseHelper.getInstance(getApplicationContext());

    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> listPermissionNeeded = new ArrayList<>();
            for (String permission : appPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) !=
                        PackageManager.PERMISSION_GRANTED)
                    listPermissionNeeded.add(permission);
            }
            if (!listPermissionNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionNeeded.toArray(new String[0]),
                        PERMISSION_REQUEST_CODE);
            }
            else {
                // Zoom to my location
                map.setMyLocationEnabled(true);
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = locationManager != null ? locationManager
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER) : null;
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
                    map.animateCamera(cameraUpdate, 1000, null);
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        checkPermissions();
        // Set padding for status bar
        int resourceId = getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            map.setPadding(0,getResources().getDimensionPixelSize(resourceId),0,0);
        }
        map.getUiSettings().setTiltGesturesEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnMapClickListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnMyLocationClickListener(this);
        map.setOnInfoWindowClickListener(this);
        showAllMarker();
    }

    private void showAllMarker() {

    }

    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
        map.animateCamera(cameraUpdate,500,null);
        map.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(vectorToBitmap(R.drawable.ic_marker_car_repair))
                .title("Marker")
                .snippet("Population: 4,137,400"));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        map.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(60))
                .title("Marker")
                .snippet("Haha"));
    }


    @Override
    public void onMyLocationClick(@NonNull Location location) {
        map.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(),location.getLongitude()))
                .title("Marker")
                .snippet("Haha"))
                .setTag(0);

    }

    /*
     * Return hue value (0 - 360) base on number of items and index of this item
     * to set color for markers with different types
     */
    private int getHueValue(int numberOfItem, int index){
        return index == 0 ? 0 : index*360/numberOfItem;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_place,null);
        ChipGroup chipGroup = view.findViewById(R.id.chipGroup);
        new MaterialAlertDialogBuilder(this,R.style.MaterialDialogStyle).setTitle("Place")
                .setView(view)
                .setPositiveButton(R.string.ok,null)
                .show();
    }

    private BitmapDescriptor vectorToBitmap(@DrawableRes int id) {
        VectorDrawable vectorDrawable = (VectorDrawable) getDrawable(id);
        int h = Objects.requireNonNull(vectorDrawable).getIntrinsicHeight();
        int w = vectorDrawable.getIntrinsicWidth();
        vectorDrawable.setBounds(0, 0, w, h);
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }
}
