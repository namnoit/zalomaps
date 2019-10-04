package com.namnoit.zalomaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.namnoit.zalomaps.data.PlaceModel;
import com.namnoit.zalomaps.data.PlacesListManager;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    private PlacesListManager listManager;
    private PlacesListAdapter adapter;
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
//        recyclerView.setPadding(0,0,0,64);
        adapter = new PlacesListAdapter(listManager.getPlacesList());
        recyclerView.setAdapter(adapter);
        FloatingActionButton fab_map = findViewById(R.id.fab_map);
        fab_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        Chip chipFood = findViewById(R.id.chip_list_food_drink);
        Chip chipEntertainment = findViewById(R.id.chip_list_entertainment);
        Chip chipEducation = findViewById(R.id.chip_list_education);
        Chip chipAdministration = findViewById(R.id.chip_list_administration);
        Chip chipGasoline = findViewById(R.id.chip_list_gasoline);
        Chip chipReligion = findViewById(R.id.chip_list_religion);
        Chip chipVehicleRepair = findViewById(R.id.chip_list_vehicle_repair);
        Chip chipOther = findViewById(R.id.chip_list_other);
        Intent intent = getIntent();
        boolean[] choices = intent.getBooleanArrayExtra("choices");
        if (choices!=null){
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
}
