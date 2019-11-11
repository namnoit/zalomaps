package com.namnoit.zalomaps.data;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.namnoit.zalomaps.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlacesListManager {
    private static PlacesListManager instance;
    private ArrayList<PlaceModel> places;
    private ArrayList<PlaceModel> selectedPlaces;
    private PlacesDatabaseHelper db;

    private PlacesListManager(Context context) {
        db = PlacesDatabaseHelper.getInstance(context);
        places = db.getAllPlaces();
        selectedPlaces = new ArrayList<>();
    }

    public static synchronized PlacesListManager getInstance(Context context) {
        if (instance == null) {
            instance = new PlacesListManager(context);
        }
        return instance;
    }

    public ArrayList<PlaceModel> getPlacesList() {
        return places;
    }

    public void insertPlace(String note, int type, double lat, double lng, String address) {
        db.insertPlace(note, type, lat, lng, address);
        PlaceModel place = db.getLatestPlace();
        places.add(0, place);
    }

    public void delete(PlaceModel place) {
        int id = place.getId();
        if (places.remove(place)) {
            db.delete(id);
        }
    }

    public void updatePlace(final PlaceModel place) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                db.updatePlace(place);
            }
        });
    }

    public void updateDistances(Location myLatLng, Context context) {
        if (myLatLng == null) return;
        for (PlaceModel place : places) {
            float[] result = new float[1];
            Location.distanceBetween(place.getLatitude(), place.getLongitude(), myLatLng.getLatitude(), myLatLng.getLongitude(), result);
            place.setDistance(result[0]);
            // Find address
            if (place.getAddress().equals(context.getString(R.string.address_not_found))) {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses;
                String address;
                try {
                    addresses = geocoder.getFromLocation(place.getLatitude(),
                            place.getLongitude(),
                            1);
                    address = addresses.get(0).getAddressLine(0);
                    if (!address.equals(context.getString(R.string.address_not_found))) {
                        place.setAddress(address);
                        updatePlace(place);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Collections.sort(places, new Comparator<PlaceModel>() {
            @Override
            public int compare(PlaceModel o1, PlaceModel o2) {
                return Double.compare(o1.getDistance(), o2.getDistance());
            }
        });
    }

    public int size() {
        return places.size();
    }

    public int getSelectedCount() {
        return selectedPlaces.size();
    }

    public void select(PlaceModel place) {
        selectedPlaces.add(place);
    }

    public void removeSelection(PlaceModel place) {
        selectedPlaces.remove(place);
    }

    public void removeAllSelection() {
        selectedPlaces.clear();
    }

    public void deleteSelectedPlaces() {
        for (PlaceModel place : selectedPlaces) {
            db.delete(place.getId());
            places.remove(place);
        }
        selectedPlaces.clear();
    }

    public int selectAll() {
        selectedPlaces = new ArrayList<>();
        for (PlaceModel place : places) {
            if (place.isChosen()) selectedPlaces.add(place);
        }
        return selectedPlaces.size();
    }

    public boolean isSelected(PlaceModel place) {
        return selectedPlaces.contains(place);
    }
}
