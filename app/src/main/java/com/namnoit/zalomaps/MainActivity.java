package com.namnoit.zalomaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.namnoit.zalomaps.data.PlacesDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

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
        for (int grantResult: grantResults){
            if (grantResult < 0){
                permissionsAcepted = false;
            }
        }
        if (permissionsAcepted){
            map.setMyLocationEnabled(true);
        }
        else {
            new MaterialAlertDialogBuilder(this,R.style.MaterialDialogStyle)
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
            Manifest.permission.ACCESS_FINE_LOCATION
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
        PlacesDatabaseHelper db = PlacesDatabaseHelper.getInstance(getApplicationContext());
        db.getAllPlaces();
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
                map.setMyLocationEnabled(true);
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
                .icon(BitmapDescriptorFactory.defaultMarker(0))
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
}
