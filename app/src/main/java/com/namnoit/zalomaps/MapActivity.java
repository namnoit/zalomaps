package com.namnoit.zalomaps;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.lang.ref.WeakReference;
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
    private static final int PERMISSION_REQUEST_CODE = 1;
    public static String[] APP_PERMISSION = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private GoogleMap map;
    private PlacesListManager listManager;
    private FloatingActionButton fab;
    private ActionBar actionBar;
    private View parentView;
    private ArrayList<Marker> markers;
    private BottomSheetBehavior sheetBehavior;
    private boolean isMain = true;
    private FusedLocationProviderClient fusedLocationClient;

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
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                addPlace(Objects.requireNonNull(place.getLatLng()));
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
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
        if (item.getItemId() == android.R.id.home) {
            if (isMain) {
                finish();
            } else {
                for (Marker marker : markers) {
                    marker.setVisible(true);
                }
                actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location == null) {
                                    sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                    Snackbar.make(parentView, R.string.alert_no_location, Snackbar.LENGTH_SHORT).show();
                                    return;
                                }
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
                                map.animateCamera(cameraUpdate, 1000, null);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                Snackbar.make(parentView, R.string.alert_no_location, Snackbar.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        listManager = PlacesListManager.getInstance(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        if (!isMain) {
            for (Marker marker : markers) {
                marker.setVisible(true);
            }
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
            isMain = true;
            actionBar.setTitle(R.string.search_here);
        } else {
            super.onBackPressed();
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> listPermissionNeeded = new ArrayList<>();
            for (String permission : APP_PERMISSION) {
                if (ContextCompat.checkSelfPermission(this, permission) !=
                        PackageManager.PERMISSION_GRANTED)
                    listPermissionNeeded.add(permission);
            }
            if (!listPermissionNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionNeeded.toArray(new String[0]),
                        PERMISSION_REQUEST_CODE);
                return;
            }
        }
        // Zoom to my location
        map.setMyLocationEnabled(true);
        Intent intent = getIntent();
        int id = intent.getIntExtra(PlacesListAdapter.KEY_ID, -1);
        if (id != -1) return;
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    Snackbar.make(parentView, R.string.alert_no_location, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
                map.animateCamera(cameraUpdate, 1000, null);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        checkPermissions();
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setOnMapClickListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnMyLocationClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnPoiClickListener(this);
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
                    .snippet(place.getDescription()));
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
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            fab.hide();
        } else {
            actionBar.show();
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            fab.show();
        }
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
        addPlace(latLng);
    }


    @Override
    public void onMyLocationClick(@NonNull Location location) {
        addPlace(new LatLng(location.getLatitude(), location.getLongitude()));
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
            textNotes.setText(place.getDescription());
            switch (oldType) {
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
                            place.setDescription(Objects.requireNonNull(textNotes.getText()).toString());
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

                            InsertionTask insertionTask = new InsertionTask(
                                    MapActivity.this,
                                    type,
                                    notes,
                                    latLng,
                                    finalAddress);
                            insertionTask.execute();
                        }
                    }
                })
                .show();
    }

    void addMarker(LatLng latLng, int type, String notes) {
        Marker marker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(vectorToBitmap(PlaceModel.getDrawableResource(type)))
                .title(PlaceModel.getTypeInString(type))
                .snippet(notes));
        marker.showInfoWindow();
        marker.setTag(listManager.getPlacesList().get(0));
        markers.add(marker);
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
        String title;
        switch (v.getId()) {
            case R.id.button_administration_sheet:
                type = PlaceModel.TYPE_ADMINISTRATION;
                title = getResources().getString(R.string.administration);
                break;
            case R.id.button_education_sheet:
                type = PlaceModel.TYPE_EDUCATION;
                title = getResources().getString(R.string.education);
                break;
            case R.id.button_entertainment_sheet:
                type = PlaceModel.TYPE_ENTERTAINMENT;
                title = getResources().getString(R.string.entertainment);
                break;
            case R.id.button_food_sheet:
                type = PlaceModel.TYPE_FOOD_DRINK;
                title = getResources().getString(R.string.food_drink);
                break;
            case R.id.button_gasoline_sheet:
                type = PlaceModel.TYPE_GASOLINE;
                title = getResources().getString(R.string.gasoline);
                break;
            case R.id.button_religion_sheet:
                type = PlaceModel.TYPE_RELIGION;
                title = getResources().getString(R.string.religion);
                break;
            case R.id.button_vehicle_sheet:
                type = PlaceModel.TYPE_VEHICLE_REPAIR;
                title = getResources().getString(R.string.vehicle_repair);
                break;
            default:
                type = PlaceModel.TYPE_OTHER;
                title = getResources().getString(R.string.other);
                break;
        }
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int markerCount = 0;
        for (Marker marker : markers) {
            PlaceModel place = (PlaceModel) marker.getTag();
            if ((place != null ? place.getType() : -1) != type) {
                marker.setVisible(false);
            } else {
                marker.setVisible(true);
                builder.include(marker.getPosition());
                markerCount++;
            }
        }
        title += " (" + markerCount + ")";
        actionBar.setTitle(title);
        isMain = false;
        actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        if (markerCount == 0) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            Snackbar.make(parentView, R.string.alert_no_place_of_type, Snackbar.LENGTH_SHORT).show();
            return;
        }
        final int width = getResources().getDisplayMetrics().widthPixels;
        final int height = getResources().getDisplayMetrics().heightPixels;
        final int minMetric = Math.min(width, height);
        final int padding = (int) (minMetric * 0.25);
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    builder.include(latLng);
                }
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), padding);
                map.animateCamera(cameraUpdate, 1000, null);
            }
        });
    }

    private static class InsertionTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<MapActivity> activityReference;
        private int type;
        private String notes, address;
        private LatLng latLng;

        InsertionTask(MapActivity context, int type, String notes, LatLng latLng, String address) {
            activityReference = new WeakReference<>(context);
            this.type = type;
            this.notes = notes;
            this.latLng = latLng;
            this.address = address;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            MapActivity activity = activityReference.get();
            PlacesListManager.getInstance(activity).insertPlace(notes,
                    type,
                    latLng.latitude,
                    latLng.longitude,
                    address);
            return null;
        }

        @Override
        protected void onPreExecute() {
            activityReference.get().findViewById(R.id.progress_bar_map).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            MapActivity activity = activityReference.get();
            activity.findViewById(R.id.progress_bar_map).setVisibility(View.GONE);
            activity.addMarker(latLng, type, notes);
        }
    }
}
