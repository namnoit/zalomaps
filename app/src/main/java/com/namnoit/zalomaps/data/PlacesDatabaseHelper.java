package com.namnoit.zalomaps.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class PlacesDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "placesDatabase";
    private static int DATABASE_VERSION = 1;
    private static final String TABLE_PLACES = "places";
    // Post Table Columns
    private static final String COLUMN_ID = BaseColumns._ID;
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_NOTE = "note";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_ADDRESS = "address";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " +
            TABLE_PLACES + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT DEFAULT 0," +
            COLUMN_TYPE + INT_TYPE + COMMA_SEP +
            COLUMN_LATITUDE + REAL_TYPE + COMMA_SEP +
            COLUMN_LONGITUDE + REAL_TYPE + COMMA_SEP +
            COLUMN_NOTE + TEXT_TYPE + COMMA_SEP +
            COLUMN_TIME + INT_TYPE + COMMA_SEP +
            COLUMN_ADDRESS + TEXT_TYPE + ")";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_PLACES;

    private static PlacesDatabaseHelper instance;

    static synchronized PlacesDatabaseHelper getInstance(Context context){
        if (instance==null){
            instance = new PlacesDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private PlacesDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        DATABASE_VERSION++;
        onCreate(sqLiteDatabase);
    }

    ArrayList<PlaceModel> getAllPlaces(){
        ArrayList<PlaceModel> places = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLACES,
                new String[]{COLUMN_ID, COLUMN_TYPE, COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_NOTE,
                        COLUMN_TIME, COLUMN_ADDRESS},
                null,
                null,
                null,
                null,
                COLUMN_TIME + " DESC");
        if (cursor.moveToFirst()) {
            do {
                PlaceModel place = new PlaceModel(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE)),
                        cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_NOTE)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_TIME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)));
                places.add(place);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return places;
    }

    PlaceModel getLatestPlace(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLACES,
                new String[]{
                        COLUMN_ID,
                        COLUMN_TYPE,
                        COLUMN_NOTE,
                        COLUMN_LATITUDE,
                        COLUMN_LONGITUDE,
                        COLUMN_TIME,
                        COLUMN_ADDRESS
                },
                null,
                null,
                null,
                null,
                null);
        PlaceModel place = null;
        if (cursor.moveToLast()) {
            place = new PlaceModel(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NOTE)),
                    cursor.getLong(cursor.getColumnIndex(COLUMN_TIME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)));
        }
        cursor.close();
        db.close();
        return place;
    }

    void insertPlace(String note, int type, double lat, double lng, long time, String address){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE, note);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_LATITUDE, lat);
        values.put(COLUMN_LONGITUDE, lng);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_ADDRESS, address);
        db.insert(TABLE_PLACES,null,values);
        db.close();
    }

    void updatePlace(PlaceModel place){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE, place.getType());
        values.put(COLUMN_LATITUDE, place.getLatitude());
        values.put(COLUMN_LONGITUDE, place.getLongitude());
        values.put(COLUMN_NOTE,place.getNote());
        values.put(COLUMN_TIME,place.getTime());
        values.put(COLUMN_ADDRESS,place.getAddress());
        db.update(TABLE_PLACES,
                values,
                COLUMN_ID + " = ?",
                new String[] {Integer.toString(place.getId())});
        db.close();
    }

    void delete(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLACES, COLUMN_ID + "=?", new String[]{Integer.toString(id)});
        db.close();
    }
}
