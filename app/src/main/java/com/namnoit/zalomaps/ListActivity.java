package com.namnoit.zalomaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.CompoundButton;

import com.google.android.material.chip.Chip;
import com.namnoit.zalomaps.data.PlaceModel;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    private ArrayList<PlaceModel> list;
    private PlacesListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        list = new ArrayList<>();
        list.add(new PlaceModel(1,
                PlaceModel.TYPE_ADMINISTRATION,
                10,
                10,
                "Example",
                1122));
        list.add(new PlaceModel(2,
                PlaceModel.TYPE_ENTERTAINMENT,
                10,
                10,
                "Example 2",
                11212));
        list.add(new PlaceModel(3,
                PlaceModel.TYPE_OTHER,
                10,
                10,
                "Example 3",
                11212));
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new PlacesListAdapter(list);
        recyclerView.setAdapter(adapter);
        Chip chipFood = findViewById(R.id.chip_list_food_drink);
        Chip chipEntertainment = findViewById(R.id.chip_list_entertainment);
        Chip chipEducation = findViewById(R.id.chip_list_education);
        Chip chipAdministration = findViewById(R.id.chip_list_administration);
        Chip chipGasoline = findViewById(R.id.chip_list_gasoline);
        Chip chipReligion = findViewById(R.id.chip_list_religion);
        Chip chipVehicleRepair = findViewById(R.id.chip_list_vehicle_repair);
        Chip chipOther = findViewById(R.id.chip_list_other);
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
            for (PlaceModel place: list){
                if (place.getType() == choice){
                    place.setChoosen(isChecked);
                }
            }
            adapter.notifyDataSetChanged();
        }
    };
}
