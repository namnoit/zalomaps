package com.namnoit.zalomaps;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.namnoit.zalomaps.data.PlaceModel;
import com.namnoit.zalomaps.data.PlacesListManager;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(ADMINISTRATION_SELECTED, chipAdministration.isChecked());
        savedInstanceState.putBoolean(EDUCATION_SELECTED, chipEducation.isChecked());
        savedInstanceState.putBoolean(ENTERTAINMENT_SELECTED, chipEntertainment.isChecked());
        savedInstanceState.putBoolean(FOOD_SELECTED, chipFood.isChecked());
        savedInstanceState.putBoolean(GASOLINE_SELECTED, chipGasoline.isChecked());
        savedInstanceState.putBoolean(RELIGION_SELECTED, chipReligion.isChecked());
        savedInstanceState.putBoolean(VEHICLE_SELECTED, chipVehicleRepair.isChecked());
        savedInstanceState.putBoolean(OTHER_SELECTED, chipOther.isChecked());
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
        chipAdministration.setChecked(savedInstanceState.getBoolean(ADMINISTRATION_SELECTED));
        chipEducation.setChecked(savedInstanceState.getBoolean(EDUCATION_SELECTED));
        chipEntertainment.setChecked(savedInstanceState.getBoolean(ENTERTAINMENT_SELECTED));
        chipFood.setChecked(savedInstanceState.getBoolean(FOOD_SELECTED));
        chipGasoline.setChecked(savedInstanceState.getBoolean(GASOLINE_SELECTED));
        chipReligion.setChecked(savedInstanceState.getBoolean(RELIGION_SELECTED));
        chipVehicleRepair.setChecked(savedInstanceState.getBoolean(VEHICLE_SELECTED));
        chipOther.setChecked(savedInstanceState.getBoolean(OTHER_SELECTED));
    }
}
