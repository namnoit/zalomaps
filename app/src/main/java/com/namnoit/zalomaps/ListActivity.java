package com.namnoit.zalomaps;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.namnoit.zalomaps.data.PlaceModel;
import com.namnoit.zalomaps.data.PlacesListManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


public class ListActivity extends AppCompatActivity implements SwipeController.OnSwipedListener {
    public static final String BROADCAST_SELECT = "select";
    public static final String BROADCAST_START_SELECTING = "start_selecting";
    public static final String BROADCAST_FINISH_SELECTING = "finish_selecting";
    private static final int PERMISSION_REQUEST_CODE = 1;
    ArrayList<Chip> chips;
    View scroll;
    private PlacesListManager listManager;
    private PlacesListAdapter adapter;
    private ActionMode mActionMode;
    private ExtendedFloatingActionButton fab_map;
    private Location myLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.menu_context, menu);
            fab_map.hide();
            for (Chip chip : chips) {
                chip.setEnabled(false);
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;

        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.select_all_context_menu) {
                actionMode.setTitle(Integer.toString(listManager.selectAll()));
                adapter.notifyDataSetChanged();
            } else {
                deleteSelectedPlaces();
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            listManager.removeAllSelection();
            adapter.notifyDataSetChanged();
            mActionMode = null;
            fab_map.show();
            for (Chip chip : chips) {
                chip.setEnabled(true);
            }
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case BROADCAST_START_SELECTING:
                        mActionMode = startActionMode(actionModeCallback);
                    case BROADCAST_SELECT:
                        if (mActionMode != null) {
                            mActionMode.setTitle(Integer.toString(intent.
                                    getIntExtra(PlacesListAdapter.KEY_SELECTED_COUNT, 0)));
                        }
                        break;
                    case BROADCAST_FINISH_SELECTING:
                        mActionMode.finish();
                        mActionMode = null;
                        break;
                }
            }
        }
    };
    private Chip.OnCheckedChangeListener chipCheckedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int choice;
            switch (buttonView.getId()) {
                case R.id.chip_map_administration:
                    choice = PlaceModel.TYPE_ADMINISTRATION;
                    break;
                case R.id.chip_map_education:
                    choice = PlaceModel.TYPE_EDUCATION;
                    break;
                case R.id.chip_map_entertainment:
                    choice = PlaceModel.TYPE_ENTERTAINMENT;
                    break;
                case R.id.chip_map_food_drink:
                    choice = PlaceModel.TYPE_FOOD_DRINK;
                    break;
                case R.id.chip_map_gasoline:
                    choice = PlaceModel.TYPE_GASOLINE;
                    break;
                case R.id.chip_map_religion:
                    choice = PlaceModel.TYPE_RELIGION;
                    break;
                case R.id.chip_map_vehicle_repair:
                    choice = PlaceModel.TYPE_VEHICLE_REPAIR;
                    break;
                default:
                    choice = PlaceModel.TYPE_OTHER;
                    break;
            }
            for (PlaceModel place : listManager.getPlacesList()) {
                if (place.getType() == choice) {
                    place.setChosen(isChecked);
                }
            }
            adapter.notifyDataSetChanged();
        }
    };

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
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    myLocation = location;
                    PlacesLoaderTask loaderTask = new PlacesLoaderTask(ListActivity.this);
                    loaderTask.execute();
                }
            });
        } else {
            new MaterialAlertDialogBuilder(this, R.style.MaterialDialogStyle)
                    .setTitle(R.string.title_permission_denied)
                    .setMessage(R.string.message_permission_denied)
                    .setCancelable(false)
                    .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(uri);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(Color.parseColor("#b0bec5"));
        }
        Objects.requireNonNull(getSupportActionBar()).setElevation(0f);
        checkPermissions();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    MapActivity.APP_PERMISSION,
                    PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    myLocation = location;
                    PlacesLoaderTask loaderTask = new PlacesLoaderTask(ListActivity.this);
                    loaderTask.execute();
                }
            });
        }
    }

    public void updateDistance() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                myLocation = location;
                if (myLocation != null) {
                    listManager.updateDistances(myLocation);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null && listManager != null) {
            for (Chip chip : chips) {
                if (!chip.isChecked()) {
                    int choice;
                    switch (chip.getId()) {
                        case R.id.chip_map_administration:
                            choice = PlaceModel.TYPE_ADMINISTRATION;
                            break;
                        case R.id.chip_map_education:
                            choice = PlaceModel.TYPE_EDUCATION;
                            break;
                        case R.id.chip_map_entertainment:
                            choice = PlaceModel.TYPE_ENTERTAINMENT;
                            break;
                        case R.id.chip_map_food_drink:
                            choice = PlaceModel.TYPE_FOOD_DRINK;
                            break;
                        case R.id.chip_map_gasoline:
                            choice = PlaceModel.TYPE_GASOLINE;
                            break;
                        case R.id.chip_map_religion:
                            choice = PlaceModel.TYPE_RELIGION;
                            break;
                        case R.id.chip_map_vehicle_repair:
                            choice = PlaceModel.TYPE_VEHICLE_REPAIR;
                            break;
                        default:
                            choice = PlaceModel.TYPE_OTHER;
                            break;
                    }
                    for (PlaceModel place : listManager.getPlacesList()) {
                        if (place.getType() == choice && place.isChosen()) {
                            place.setChosen(false);
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver,
                new IntentFilter(BROADCAST_SELECT));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver,
                new IntentFilter(BROADCAST_START_SELECTING));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver,
                new IntentFilter(BROADCAST_FINISH_SELECTING));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return true;
    }

    private void deleteSelectedPlaces() {
        new MaterialAlertDialogBuilder(ListActivity.this, R.style.MaterialDialogStyle)
                .setTitle(R.string.title_delete)
                .setMessage(R.string.delete_selected_places)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SelectedPlacesDeleteTask deletionTask =
                                new SelectedPlacesDeleteTask(ListActivity.this);
                        deletionTask.execute();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onSwipe(RecyclerView.ViewHolder viewHolder) {
        DeleteTask deleteTask = new DeleteTask(this, viewHolder.getAdapterPosition());
        deleteTask.execute();
    }

    void doAfterDelete() {
        adapter.notifyDataSetChanged();
        mActionMode.finish();
        mActionMode = null;
    }

    void doAfterDeleteOne(int position) {
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, listManager.size());
    }

    void setUp(PlacesListManager placesListManager) {
        listManager = placesListManager;
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        scroll = findViewById(R.id.scroll_view_map);
        adapter = new PlacesListAdapter(this);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmpty();
            }
        });
        recyclerView.setAdapter(adapter);
        checkEmpty();
        SwipeController swipeController = new SwipeController(this, this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        fab_map = findViewById(R.id.fab_map);
        fab_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        final SwipeRefreshLayout swipe = findViewById(R.id.swipe_refresh);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateDistance();
                swipe.setRefreshing(false);
            }
        });
        Chip chipFood = findViewById(R.id.chip_map_food_drink);
        Chip chipEntertainment = findViewById(R.id.chip_map_entertainment);
        Chip chipEducation = findViewById(R.id.chip_map_education);
        Chip chipAdministration = findViewById(R.id.chip_map_administration);
        Chip chipGasoline = findViewById(R.id.chip_map_gasoline);
        Chip chipReligion = findViewById(R.id.chip_map_religion);
        Chip chipVehicleRepair = findViewById(R.id.chip_map_vehicle_repair);
        Chip chipOther = findViewById(R.id.chip_map_other);
        chipFood.setOnCheckedChangeListener(chipCheckedListener);
        chipEntertainment.setOnCheckedChangeListener(chipCheckedListener);
        chipAdministration.setOnCheckedChangeListener(chipCheckedListener);
        chipEducation.setOnCheckedChangeListener(chipCheckedListener);
        chipGasoline.setOnCheckedChangeListener(chipCheckedListener);
        chipReligion.setOnCheckedChangeListener(chipCheckedListener);
        chipVehicleRepair.setOnCheckedChangeListener(chipCheckedListener);
        chipOther.setOnCheckedChangeListener(chipCheckedListener);
        chips = new ArrayList<>();
        chips.add(chipFood);
        chips.add(chipEntertainment);
        chips.add(chipEducation);
        chips.add(chipAdministration);
        chips.add(chipGasoline);
        chips.add(chipReligion);
        chips.add(chipVehicleRepair);
        chips.add(chipOther);
        // Make a showcase to show user how to use this app
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "showcase");
        sequence.setConfig(config);
        sequence.addSequenceItem(fab_map, getString(R.string.showcase_fab_map), getString(R.string.got_it));
        sequence.addSequenceItem(chipFood,
                getString(R.string.showcase_chip), getString(R.string.got_it));
        sequence.start();
    }

    private void checkEmpty() {
        final TextView emptyText = findViewById(R.id.text_empty_message);
        if (adapter.getItemCount() == 0) {
            scroll.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            scroll.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    private static class PlacesLoaderTask extends AsyncTask<Void, Void, PlacesListManager> {
        private WeakReference<ListActivity> activityReference;

        PlacesLoaderTask(ListActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected PlacesListManager doInBackground(Void... voids) {
            PlacesListManager listManager = PlacesListManager.getInstance(activityReference.get());
            listManager.updateDistances(activityReference.get().myLocation);
            return listManager;
        }

        @Override
        protected void onPreExecute() {
            activityReference.get().findViewById(R.id.layout_list).setVisibility(View.GONE);
            activityReference.get().findViewById(R.id.text_empty_message).setVisibility(View.GONE);
            activityReference.get().findViewById(R.id.progress_bar_list).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(PlacesListManager placesListManager) {
            ListActivity activity = activityReference.get();
            activity.findViewById(R.id.progress_bar_list).setVisibility(View.GONE);
            activity.findViewById(R.id.layout_list).setVisibility(View.VISIBLE);
            activity.setUp(placesListManager);
        }
    }

    private static class SelectedPlacesDeleteTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<ListActivity> activityReference;

        SelectedPlacesDeleteTask(ListActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            PlacesListManager.getInstance(activityReference.get()).deleteSelectedPlaces();
            return null;
        }

        @Override
        protected void onPreExecute() {
            activityReference.get().findViewById(R.id.layout_list).setEnabled(false);
            activityReference.get().findViewById(R.id.progress_bar_list).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ListActivity activity = activityReference.get();
            activity.findViewById(R.id.progress_bar_list).setVisibility(View.GONE);
            activity.findViewById(R.id.layout_list).setEnabled(true);
            activity.doAfterDelete();
        }
    }

    private static class DeleteTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<ListActivity> activityReference;
        private int position;

        DeleteTask(ListActivity context, int position) {
            this.position = position;
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            PlacesListManager.getInstance(activityReference.get()).delete(
                    PlacesListManager.getInstance(activityReference.get())
                            .getPlacesList().get(position));
            return null;
        }

        @Override
        protected void onPreExecute() {
            activityReference.get().findViewById(R.id.layout_list).setEnabled(false);
            activityReference.get().findViewById(R.id.progress_bar_list).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ListActivity activity = activityReference.get();
            activity.findViewById(R.id.progress_bar_list).setVisibility(View.GONE);
            activity.findViewById(R.id.layout_list).setEnabled(true);
            activity.doAfterDeleteOne(position);
        }
    }
}
