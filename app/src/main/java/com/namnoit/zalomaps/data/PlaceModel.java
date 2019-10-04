package com.namnoit.zalomaps.data;

import android.app.Application;
import android.content.Context;

import com.namnoit.zalomaps.R;

public class PlaceModel {
    public static final int TYPE_FOOD_DRINK = 1;
    public static final int TYPE_ENTERTAINMENT = 2;
    public static final int TYPE_EDUCATION = 3;
    public static final int TYPE_VEHICLE_REPAIR = 4;
    public static final int TYPE_RELIGION = 5;
    public static final int TYPE_ADMINISTRATION = 6;
    public static final int TYPE_GASOLINE = 7;
    public static final int TYPE_OTHER = 8;

    private int id;
    private int type;
    private String note;
    private long time;
    private double latitude, longitude;
    private boolean chosen;
    private String markerId;

    public PlaceModel(int id, int type, double latitude, double longitude, String note, long time){
        this.id = id;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.note = note;
        this.time = time;
        chosen = true;
    }

    public static String getTypeInString(int type){
        switch (type){
            case TYPE_FOOD_DRINK:
                return "Food and drink";
            case TYPE_ENTERTAINMENT:
                return "Entertainment";
            case TYPE_EDUCATION:
                return "Education";
            case TYPE_VEHICLE_REPAIR:
                return "Vehicle repair";
            case TYPE_RELIGION:
                return "Religion";
            case TYPE_ADMINISTRATION:
                return "Administration";
            case TYPE_GASOLINE:
                return "Gaslone";
            default:
                return "Other";
        }
    }

    public static int getDrawableResource(int type){
        switch (type){
            case TYPE_FOOD_DRINK:
                return R.drawable.ic_marker_food;
            case TYPE_ENTERTAINMENT:
                return R.drawable.ic_marker_entertainment;
            case TYPE_EDUCATION:
                return R.drawable.ic_marker_education;
            case TYPE_VEHICLE_REPAIR:
                return R.drawable.ic_marker_car_repair;
            case TYPE_RELIGION:
                return R.drawable.ic_marker_religion;
            case TYPE_ADMINISTRATION:
                return R.drawable.ic_marker_administration;
            case TYPE_GASOLINE:
                return R.drawable.ic_marker_gasoline;
            default:
                return R.drawable.ic_marker_other;
        }
    }

    public static int getTypeByIdDialog(int id){
        switch (id){
            case R.id.chip_food_drink:
                return PlaceModel.TYPE_FOOD_DRINK;
            case R.id.chip_entertainment:
                return PlaceModel.TYPE_ENTERTAINMENT;
            case R.id.chip_education:
                return PlaceModel.TYPE_EDUCATION;
            case R.id.chip_vehicle_repair:
                return PlaceModel.TYPE_VEHICLE_REPAIR;
            case R.id.chip_religion:
                return PlaceModel.TYPE_RELIGION;
            case R.id.chip_administration:
                return PlaceModel.TYPE_ADMINISTRATION;
            case R.id.chip_gasoline:
                return PlaceModel.TYPE_GASOLINE;
            default:
                return PlaceModel.TYPE_OTHER;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longitude;
    }

    public void setLongtitude(double longtitude) {
        this.longitude = longtitude;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setChosen(boolean chosen) {
        this.chosen = chosen;
    }

    public boolean isChosen() {
        return chosen;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }

    public String getMarkerId(){
        return markerId;
    }
}
