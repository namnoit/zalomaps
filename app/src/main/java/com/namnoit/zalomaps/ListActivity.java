package com.namnoit.zalomaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.namnoit.zalomaps.data.PlaceModel;
import com.namnoit.zalomaps.data.PlacesListManager;


public class ListActivity extends AppCompatActivity {
    public static final String BROADCAST_START_SELECTING = "start_selecting";
    private static final String ADMINISTRATION_SELECTED = "administration_selected";
    private static final String EDUCATION_SELECTED = "education_selected";
    private static final String ENTERTAINMENT_SELECTED = "entertainment_selected";
    private static final String FOOD_SELECTED = "food_selected";
    private static final String GASOLINE_SELECTED = "gasoline_selected";
    private static final String RELIGION_SELECTED = "religion_selected";
    private static final String VEHICLE_SELECTED = "vehicle_selected";
    private static final String OTHER_SELECTED = "other_selected";
    private PlacesListManager listManager;
    private PlacesListAdapter adapter;
    private Chip chipFood, chipEntertainment, chipEducation, chipAdministration, chipGasoline,
            chipReligion, chipVehicleRepair, chipOther;
    private ActionMode mActionMode;
    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.menu_context, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mActionMode = null;
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case BROADCAST_START_SELECTING:
                        mActionMode = startActionMode(actionModeCallback);
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        listManager = PlacesListManager.getInstance(getApplicationContext());
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new PlacesListAdapter(listManager.getPlacesList(), getApplicationContext());
        recyclerView.setAdapter(adapter);
        FloatingActionButton fab_map = findViewById(R.id.fab_map);
        fab_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        chipFood = findViewById(R.id.chip_list_food_drink);
        chipEntertainment = findViewById(R.id.chip_list_entertainment);
        chipEducation = findViewById(R.id.chip_list_education);
        chipAdministration = findViewById(R.id.chip_list_administration);
        chipGasoline = findViewById(R.id.chip_list_gasoline);
        chipReligion = findViewById(R.id.chip_list_religion);
        chipVehicleRepair = findViewById(R.id.chip_list_vehicle_repair);
        chipOther = findViewById(R.id.chip_list_other);

        Intent intent = getIntent();
        boolean[] choices = intent.getBooleanArrayExtra("choices");
        if (choices != null) {
        }
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
                new IntentFilter(BROADCAST_START_SELECTING));
    }

    private Chip.OnCheckedChangeListener chipCheckedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int choice;
            switch (buttonView.getId()){
                case R.id.chip_list_administration:
                    choice = PlaceModel.TYPE_ADMINISTRATION;
                    break;
                case R.id.chip_list_education:
                    choice = PlaceModel.TYPE_EDUCATION;
                    break;
                case R.id.chip_list_entertainment:
                    choice = PlaceModel.TYPE_ENTERTAINMENT;
                    break;
                case R.id.chip_list_food_drink:
                    choice = PlaceModel.TYPE_FOOD_DRINK;
                    break;
                case R.id.chip_list_gasoline:
                    choice = PlaceModel.TYPE_GASOLINE;
                    break;
                case R.id.chip_list_religion:
                    choice = PlaceModel.TYPE_RELIGION;
                    break;
                case R.id.chip_list_vehicle_repair:
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


}
