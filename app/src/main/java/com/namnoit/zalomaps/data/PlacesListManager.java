package com.namnoit.zalomaps.data;

import android.content.Context;

import java.util.ArrayList;

public class PlacesListManager {
    private ArrayList<PlaceModel> places;
    private ArrayList<PlaceModel> selectedPlaces;
    private static PlacesListManager instance;
    private PlacesDatabaseHelper db;

    private PlacesListManager(Context context){
        db = PlacesDatabaseHelper.getInstance(context);
        places = db.getAllPlaces();
        selectedPlaces = new ArrayList<>();
    }

    public static synchronized PlacesListManager getInstance(Context context){
        if (instance == null){
            instance = new PlacesListManager(context);
        }
        return instance;
    }

    public ArrayList<PlaceModel> getPlacesList(){
        return places;
    }

    public void insertPlace(String note, int type, double lat, double lng, long time, String address){
        db.insertPlace(note,type,lat,lng,time,address);
        PlaceModel place = db.getLatestPlace();
        places.add(0,place);
    }

    public void delete(int id){
        for (int i = 0; i < places.size(); i++){
            if (places.get(i).getId() == id){
                db.delete(places.get(i).getId());
                places.remove(i);
                break;
            }
        }
    }

    public void delete(PlaceModel place){
        int id = place.getId();
        if (places.remove(place)){
            db.delete(id);
        }
    }

    public void updatePlace(PlaceModel place){
        db.updatePlace(place);
    }

    public int size(){
        return places.size();
    }

    public int getSelectedCount(){
        return selectedPlaces.size();
    }

    public void select(PlaceModel place){
        selectedPlaces.add(place);
    }

    public void removeSelection(PlaceModel place){
        selectedPlaces.remove(place);
    }

    public void removeAllSelection(){
        selectedPlaces.clear();
    }

    public void deleteSelectedPlaces(){
        for (PlaceModel place : selectedPlaces) {
            db.delete(place.getId());
            places.remove(place);
        }
        selectedPlaces.clear();
    }

    public int selectAll(){
        selectedPlaces = new ArrayList<>(places);
        return selectedPlaces.size();
    }

    public boolean isSelected(PlaceModel place){
        return selectedPlaces.contains(place);
    }
}
