package com.namnoit.zalomaps.data;

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
    private boolean choosen = true;

    public PlaceModel(int id, int type, double latitude, double longitude, String note, long time){
        this.id = id;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.note = note;
        this.time = time;
        choosen = true;
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

    public void setChoosen(boolean choosen) {
        this.choosen = choosen;
    }

    public boolean isChoosen() {
        return choosen;
    }
}
