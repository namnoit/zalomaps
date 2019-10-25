package com.namnoit.zalomaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.namnoit.zalomaps.data.PlaceModel;
import com.namnoit.zalomaps.data.PlacesListManager;

import java.util.Objects;


public class ListActivity extends AppCompatActivity implements SwipeController.OnSwipedListener {
    public static final String BROADCAST_SELECT = "select";
    public static final String BROADCAST_START_SELECTING = "start_selecting";
    public static final String BROADCAST_FINISH_SELECTING = "finish_selecting";
    private PlacesListManager listManager;
    private PlacesListAdapter adapter;
    private ActionMode mActionMode;
    private FloatingActionButton fab_map;
    private ChipGroup chipGroup;
    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.menu_context, menu);
            fab_map.hide();
            chipGroup.setVisibility(View.GONE);
            chipGroup.setEnabled(false);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.select_all_context_menu){
                actionMode.setTitle(Integer.toString(listManager.selectAll()));
                adapter.notifyDataSetChanged();
            }
            else {
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
            chipGroup.setVisibility(View.VISIBLE);
            chipGroup.setEnabled(true);
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
                                    getIntExtra(PlacesListAdapter.KEY_SELECTED_COUNT,0)));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(Color.parseColor("#b0bec5"));
        }
        Objects.requireNonNull(getSupportActionBar()).setElevation(0f);
        listManager = PlacesListManager.getInstance(getApplicationContext());
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new PlacesListAdapter(this);
        recyclerView.setAdapter(adapter);

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
        chipGroup = findViewById(R.id.chips_filter_map);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    private Chip.OnCheckedChangeListener chipCheckedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int choice;
            switch (buttonView.getId()){
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
            for (PlaceModel place: listManager.getPlacesList()){
                if (place.getType() == choice){
                    place.setChosen(isChecked);
                }
            }
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();
        adapter.notifyDataSetChanged();
    }

    private void deleteSelectedPlaces(){
        new MaterialAlertDialogBuilder(ListActivity.this,R.style.MaterialDialogStyle)
                .setTitle(R.string.title_delete)
                .setMessage(R.string.delete_selected_places)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listManager.deleteSelectedPlaces();
                        adapter.notifyDataSetChanged();
                        mActionMode.finish();
                        mActionMode = null;
                    }
                })
                .setNegativeButton(R.string.cancel,null)
                .show();



    }

    @Override
    public void onSwipe(RecyclerView.ViewHolder viewHolder) {
        listManager.delete(listManager.getPlacesList().get(viewHolder.getAdapterPosition()));
        adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
    }
}
