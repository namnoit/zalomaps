package com.namnoit.zalomaps;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.namnoit.zalomaps.data.PlaceModel;
import com.namnoit.zalomaps.data.PlacesListManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnPoiClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMarkerClickListener,
        View.OnClickListener {
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private GoogleMap map;
    private PlacesListManager listManager;
    private FloatingActionButton fab;
    private ActionBar actionBar;
    private View parentView;
    private ArrayList<Marker> markers;
    private BottomSheetBehavior sheetBehavior;
    private boolean isMain = true;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        boolean permissionsAccepted = true;
        for (int grantResult : grantResults) {
            if (grantResult < 0) {
                permissionsAccepted = false;
            }
        }
        if (permissionsAccepted) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                addPlace(Objects.requireNonNull(place.getLatLng()));
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                if (status.getStatusMessage() != null) {
                    Snackbar.make(parentView, status.getStatusMessage(), BaseTransientBottomBar.LENGTH_SHORT);
                }
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.home){
            if (isMain) {
                finish();
            }
            else {
                for (Marker marker : markers){
                    marker.setVisible(true);
                }
                isMain = true;
                actionBar.setTitle(R.string.search_here);
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(this);
        Toolbar toolbar = findViewById(R.id.toolbar_map);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        parentView = findViewById(R.id.layout_main_coor);
        ConstraintLayout layoutBottomSheet = findViewById(R.id.bottom_sheet);
        ImageButton administrationButton = layoutBottomSheet.findViewById(R.id.button_administration_sheet);
        ImageButton educationButton = layoutBottomSheet.findViewById(R.id.button_education_sheet);
        ImageButton entertainment = layoutBottomSheet.findViewById(R.id.button_entertainment_sheet);
        ImageButton foodButton = layoutBottomSheet.findViewById(R.id.button_food_sheet);
        ImageButton gasolineButton = layoutBottomSheet.findViewById(R.id.button_gasoline_sheet);
        ImageButton religionButton = layoutBottomSheet.findViewById(R.id.button_religion_sheet);
        ImageButton vehicleButton = layoutBottomSheet.findViewById(R.id.button_vehicle_sheet);
        ImageButton otherButton = layoutBottomSheet.findViewById(R.id.button_other_sheet);
        administrationButton.setOnClickListener(this);
        educationButton.setOnClickListener(this);
        entertainment.setOnClickListener(this);
        foodButton.setOnClickListener(this);
        gasolineButton.setOnClickListener(this);
        religionButton.setOnClickListener(this);
        vehicleButton.setOnClickListener(this);
        otherButton.setOnClickListener(this);


        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        final List<Place.Field> fields =
                Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY, fields)
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setCountry("VN")
                        .build(MapActivity.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

            }
        });
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        listManager = PlacesListManager.getInstance(getApplicationContext());

    }

    public void toggleBottomSheet() {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
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
            } else {
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
            map.setPadding(0, getResources().getDimensionPixelSize(resourceId), 0, 0);
        }
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setOnMapClickListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnMyLocationClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnPoiClickListener(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            View mapView = mapFragment.getView();
            assert mapView != null;
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            rlp.setMargins(0, 180, 180, 0);
        }
        showAllMarker();
    }


    private void showAllMarker() {
        Intent intent = getIntent();
        int id = intent.getIntExtra(PlacesListAdapter.KEY_ID, -1);
        markers = new ArrayList<>();

        for (PlaceModel place : listManager.getPlacesList()) {
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(place.getLatitude(), place.getLongitude()))
                    .icon(vectorToBitmap(PlaceModel.getDrawableResource(place.getType())))
                    .title(PlaceModel.getTypeInString(place.getType()))
                    .snippet(place.getNote()));
            marker.setTag(place);
            markers.add(marker);
            if (id == place.getId()) {
                map.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                marker.showInfoWindow();
            }
        }
    }

    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {
        addPlace(pointOfInterest.latLng);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (actionBar.isShowing()) {
            actionBar.hide();
            fab.hide();
        } else {
            actionBar.show();
            fab.show();
        }
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
        addPlace(latLng);
    }


    @Override
    public void onMyLocationClick(@NonNull Location location) {
        addPlace(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    /*
     * Return hue value (0 - 360) base on number of items and index of this item
     * to set color for markers with different types
     */
    private int getHueValue(int numberOfItem, int index) {
        return index == 0 ? 0 : index * 360 / numberOfItem;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        final PlaceModel place = (PlaceModel) marker.getTag();
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_place, null);
        final ChipGroup chipGroup = view.findViewById(R.id.chip_group_add_place);
        final TextInputEditText textNotes = view.findViewById(R.id.text_notes_add_place);
        final TextInputEditText textAddress = view.findViewById(R.id.text_address);
        if (place != null) {
            textAddress.setText(place.getAddress());
            final int oldType = place.getType();
            textNotes.setText(place.getNote());
            switch (place.getType()) {
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
                            if (newType != oldType) {
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
                            listManager.delete(place);
                            marker.remove();
                        }
                    })
                    .show();
        }
        return true;
    }

    private void addPlace(final LatLng latLng) {
        final View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_place, null);
        final ChipGroup chipGroup = view.findViewById(R.id.chip_group_add_place);
        TextInputEditText textAddress = view.findViewById(R.id.text_address);
        Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
        List<Address> addresses;
        String address;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
            address = getResources().getString(R.string.address_not_found);
        }
        textAddress.setText(address);
        final String finalAddress = address;
        new MaterialAlertDialogBuilder(this, R.style.MaterialDialogStyle)
                .setTitle(R.string.add_new_place)
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
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
                                    System.currentTimeMillis(),
                                    finalAddress);
                            Marker marker = map.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .icon(vectorToBitmap(PlaceModel.getDrawableResource(type)))
                                    .title(PlaceModel.getTypeInString(type))
                                    .snippet(notes));
                            marker.showInfoWindow();
                            marker.setTag(listManager.getPlacesList().get(0));
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

    @Override
    public void onClick(View v) {
        int type;
        switch (v.getId()) {
            case R.id.button_administration_sheet:
                type = PlaceModel.TYPE_ADMINISTRATION;
                actionBar.setTitle(getResources().getString(R.string.administration));
                break;
            case R.id.button_education_sheet:
                type = PlaceModel.TYPE_EDUCATION;
                actionBar.setTitle(getResources().getString(R.string.education));
                break;
            case R.id.button_entertainment_sheet:
                type = PlaceModel.TYPE_ENTERTAINMENT;
                actionBar.setTitle(getResources().getString(R.string.entertainment));
                break;
            case R.id.button_food_sheet:
                type = PlaceModel.TYPE_FOOD_DRINK;
                actionBar.setTitle(getResources().getString(R.string.food_drink));
                break;
            case R.id.button_gasoline_sheet:
                type = PlaceModel.TYPE_GASOLINE;
                actionBar.setTitle(getResources().getString(R.string.gasoline));
                break;
            case R.id.button_religion_sheet:
                type = PlaceModel.TYPE_RELIGION;
                actionBar.setTitle(getResources().getString(R.string.religion));
                break;
            case R.id.button_vehicle_sheet:
                type = PlaceModel.TYPE_VEHICLE_REPAIR;
                actionBar.setTitle(getResources().getString(R.string.vehicle_repair));
                break;
            default:
                type = PlaceModel.TYPE_OTHER;
                actionBar.setTitle(getResources().getString(R.string.other));
                break;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            PlaceModel place = (PlaceModel) marker.getTag();
            if ((place != null ? place.getType() : -1) != type) {
                marker.setVisible(false);
            } else {
                marker.setVisible(true);
                builder.include(marker.getPosition());
            }
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
            return;
        }
        Location location = locationManager != null ? locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER) : null;
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            builder.include(latLng);
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 300);
        map.animateCamera(cameraUpdate, 1000, null);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
        isMain = false;
    }
}
