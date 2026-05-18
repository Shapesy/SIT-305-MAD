package com.example.lostandfound;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lostandfound.db";
    private static final int DATABASE_VERSION = 1;

    static final String TABLE_ITEMS  = "items";
    static final String COL_ID       = "_id";
    static final String COL_TYPE     = "post_type";
    static final String COL_NAME     = "name";
    static final String COL_PHONE    = "phone";
    static final String COL_DESC     = "description";
    static final String COL_DATE     = "date";
    static final String COL_LOCATION = "location";
    static final String COL_LAT      = "latitude";
    static final String COL_LNG      = "longitude";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ITEMS + " (" +
                COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TYPE     + " TEXT, " +
                COL_NAME     + " TEXT, " +
                COL_PHONE    + " TEXT, " +
                COL_DESC     + " TEXT, " +
                COL_DATE     + " TEXT, " +
                COL_LOCATION + " TEXT, " +
                COL_LAT      + " REAL, " +
                COL_LNG      + " REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    public long insertItem(String postType, String name, String phone,
                           String description, String date, String location,
                           double latitude, double longitude) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_TYPE, postType);
        v.put(COL_NAME, name);
        v.put(COL_PHONE, phone);
        v.put(COL_DESC, description);
        v.put(COL_DATE, date);
        v.put(COL_LOCATION, location);
        v.put(COL_LAT, latitude);
        v.put(COL_LNG, longitude);
        long id = db.insert(TABLE_ITEMS, null, v);
        db.close();
        return id;
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);
        if (c.moveToFirst()) {
            do {
                items.add(new Item(
                        c.getInt(c.getColumnIndexOrThrow(COL_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_TYPE)),
                        c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                        c.getString(c.getColumnIndexOrThrow(COL_PHONE)),
                        c.getString(c.getColumnIndexOrThrow(COL_DESC)),
                        c.getString(c.getColumnIndexOrThrow(COL_DATE)),
                        c.getString(c.getColumnIndexOrThrow(COL_LOCATION)),
                        c.getDouble(c.getColumnIndexOrThrow(COL_LAT)),
                        c.getDouble(c.getColumnIndexOrThrow(COL_LNG))
                ));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return items;
    }

    public boolean deleteItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_ITEMS, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }
}
