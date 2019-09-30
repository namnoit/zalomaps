package com.namnoit.zalomaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.android.material.chip.ChipGroup;
import com.namnoit.zalomaps.data.PlaceModel;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        final ArrayList<PlaceModel> list = new ArrayList<>();
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

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        final PlacesListAdapter adapter = new PlacesListAdapter(list);
        recyclerView.setAdapter(adapter);
        ChipGroup chips = findViewById(R.id.chips_filter);
        chips.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                int choice;
                switch (checkedId){
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
                        place.setChoosen(!place.isChoosen());
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}
