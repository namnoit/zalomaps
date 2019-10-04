package com.namnoit.zalomaps;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;

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
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.namnoit.zalomaps.data.PlaceModel;
import com.namnoit.zalomaps.data.PlacesListManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnPoiClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMarkerClickListener {
    private GoogleMap map;
    private PlacesListManager listManager;
    private boolean[] choices = {
            true, // Administration
            true, // Education
            true, // Entertainment
            true, // Food and drink
            true, // Gasoline
            true, // Other
            true, // Religion
            true // Vehicle repair
    };
    private Toolbar toolbar;
    private ActionBar actionBar;

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
        toolbar = findViewById(R.id.toolbar_map);
        toolbar.setTitleTextColor(0);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBlack));
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
                intent.putExtra("choices",choices);
                startActivityForResult(intent,1);
            }
        });
        listManager = PlacesListManager.getInstance(getApplicationContext());

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
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.setOnMapClickListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnMyLocationClickListener(this);
        map.setOnMarkerClickListener(this);
        showAllMarker();
    }

    private void showAllMarker() {
        for (PlaceModel place: listManager.getPlacesList()){
            if (place.isChosen()){
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(place.getLatitude(),place.getLongtitude()))
                        .icon(vectorToBitmap(PlaceModel.getDrawableResource(place.getType())))
                        .title(PlaceModel.getTypeInString(place.getType()))
                        .snippet(place.getNote()));
                place.setMarkerId(marker.getId());
            }
        }
    }

    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {
        addPlace(pointOfInterest.latLng);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (actionBar.isShowing()){
            actionBar.hide();
        }
        else actionBar.show();
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
        addPlace(latLng);
    }


    @Override
    public void onMyLocationClick(@NonNull Location location) {
        addPlace(new LatLng(location.getLatitude(),location.getLongitude()));
    }

    /*
     * Return hue value (0 - 360) base on number of items and index of this item
     * to set color for markers with different types
     */
    private int getHueValue(int numberOfItem, int index){
        return index == 0 ? 0 : index*360/numberOfItem;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_place,null);
        final ChipGroup chipGroup = view.findViewById(R.id.chip_group_add_place);
        final TextInputEditText textNotes = view.findViewById(R.id.text_notes_add_place);
        final PlaceModel place = listManager.getPlaceByMarkerId(marker.getId());
        if (place!= null) {
            final int oldType = place.getType();
            textNotes.setText(place.getNote());
            switch (place.getType()){
                case PlaceModel.TYPE_FOOD_DRINK:
                    chipGroup.check(R.id.chip_food_drink);
                    break;
                case PlaceModel.TYPE_ENTERTAINMENT:
                    chipGroup.check(R.id.chip_entertainment);
                    break;
                case PlaceModel.TYPE_EDUCATION:
                    chipGroup.check(R.id.chip_education);
                    break;
                case PlaceModel.TYPE_VEHICLE_REPAIR:
                    chipGroup.check(R.id.chip_vehicle_repair);
                    break;
                case PlaceModel.TYPE_RELIGION:
                    chipGroup.check(R.id.chip_religion);
                    break;
                case PlaceModel.TYPE_ADMINISTRATION:
                    chipGroup.check(R.id.chip_administration);
                    break;
                case PlaceModel.TYPE_GASOLINE:
                    chipGroup.check(R.id.chip_gasoline);
                    break;
                default:
                    chipGroup.check(R.id.chip_other);
                    break;
            }
            new MaterialAlertDialogBuilder(this, R.style.MaterialDialogStyle)
                    .setTitle(PlaceModel.getTypeInString(oldType))
                    .setView(view)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int newType = PlaceModel.getTypeByIdDialog(chipGroup.getCheckedChipId());
                            if (newType != oldType){
                                marker.setIcon(vectorToBitmap(PlaceModel.getDrawableResource(newType)));
                                place.setType(newType);
                            }
                            place.setNote(Objects.requireNonNull(textNotes.getText()).toString());
                            listManager.updatePlace(place);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            listManager.delete(marker.getId());
                            marker.remove();
                        }
                    })
                    .show();
        }
        return true;
    }

    private void addPlace(final LatLng latLng){
        final View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_place,null);
        final ChipGroup chipGroup = view.findViewById(R.id.chip_group_add_place);
        new MaterialAlertDialogBuilder(this,R.style.MaterialDialogStyle)
                .setTitle(R.string.add_new_place)
                .setView(view)
                .setNegativeButton(R.string.cancel,null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int type = PlaceModel.getTypeByIdDialog(chipGroup.getCheckedChipId());
                        TextInputEditText textNotes = view.findViewById(R.id.text_notes_add_place);
                        if (textNotes != null && textNotes.getText() != null) {
                            String notes = textNotes.getText().toString();
                            listManager.insertPlace(notes,
                                    type,
                                    latLng.latitude,
                                    latLng.longitude,
                                    System.currentTimeMillis());
                            Marker marker = map.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .icon(vectorToBitmap(PlaceModel.getDrawableResource(type)))
                                    .title(PlaceModel.getTypeInString(type))
                                    .snippet(notes));
                            listManager.getPlacesList().get(listManager.size()-1).setMarkerId(marker.getId());
                        }
                    }
                })
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
