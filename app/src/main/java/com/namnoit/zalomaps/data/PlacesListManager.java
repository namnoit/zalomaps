package com.namnoit.zalomaps.data;

import android.content.Context;

import java.util.ArrayList;

public class PlacesListManager {
    private ArrayList<PlaceModel> places;
    private static PlacesListManager instance;
    private PlacesDatabaseHelper db;
    private PlacesListManager(Context context){
        db = PlacesDatabaseHelper.getInstance(context);
        places = db.getAllPlaces();
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

    public void insertPlace(String note, int type, double lat, double lng, long time){
        db.insertPlace(note,type,lat,lng,time);
        PlaceModel place = db.getLatestPlace();
        places.add(place);
    }

    public void delete(String markerId){
        for (int i = 0; i < places.size(); i++){
            if (places.get(i).getMarkerId() != null && places.get(i).getMarkerId().equals(markerId)){
                db.delete(places.get(i).getId());
                places.remove(i);
                break;
            }
        }
    }

    public PlaceModel getPlaceByMarkerId(String markerId){
        for (PlaceModel place: places){
            if (place.getMarkerId() != null && place.getMarkerId().equals(markerId)){
                return place;
            }
        }
        return null;
    }

    public void updatePlace(PlaceModel place){
        db.updatePlace(place);
    }

    public int size(){
        return places.size();
    }
}
