package uk.ac.bbk.cristinaborri.ishowedapp.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "isa.db";
    private static final int DATABASE_VERSION = 1;

    // no access modifier = Package private
    static final String TABLE_EVENT = "event";
    static final String TABLE_ATTENDEE = "attendee";


    static final String COLUMN_ID = "id";
    static final String COLUMN_LOCATION_LAT = "location_latitude";
    static final String COLUMN_LOCATION_LONG = "location_longitude";
    static final String COLUMN_LOCATION_NAME = "location_name";
    static final String COLUMN_LOCATION_ADDRESS = "location_address";
    static final String COLUMN_MAP_SW_LAT = "map_sw_lat";
    static final String COLUMN_MAP_SW_LONG = "map_sw_long";
    static final String COLUMN_MAP_NE_LAT = "map_ne_lat";
    static final String COLUMN_MAP_NE_LONG = "map_ne_long";
    static final String COLUMN_EVENT_DATE = "date";
    static final String COLUMN_EVENT_NAME = "name";
    static final String COLUMN_EVENT_DETAILS = "details";

    static final String COLUMN_ATTENDEE_NAME = "name";
    static final String COLUMN_ATTENDEE_EMAIL = "email";
    static final String COLUMN_ATTENDEE_EVENT_ID= "event_id";

    private static final String TABLE_EVENT_CREATE = "CREATE TABLE " + TABLE_EVENT + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_LOCATION_LONG + " DOUBLE, " +
            COLUMN_LOCATION_LAT + " DOUBLE, " +
            COLUMN_LOCATION_NAME + " TEXT, " +
            COLUMN_LOCATION_ADDRESS + " TEXT, " +
            COLUMN_MAP_SW_LAT + " DOUBLE, " +
            COLUMN_MAP_SW_LONG + " DOUBLE, " +
            COLUMN_MAP_NE_LAT + " DOUBLE, " +
            COLUMN_MAP_NE_LONG + " DOUBLE, " +
            COLUMN_EVENT_DATE + " LONG, " +
            COLUMN_EVENT_NAME + " TEXT, " +
            COLUMN_EVENT_DETAILS + " TEXT " +
            ")";

    private static final String TABLE_ATTENDEE_CREATE = "CREATE TABLE " + TABLE_ATTENDEE + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ATTENDEE_NAME + " TEXT, " +
            COLUMN_ATTENDEE_EMAIL + " TEXT, " +
            COLUMN_ATTENDEE_EVENT_ID + " INT, " +
            "FOREIGN KEY(" + COLUMN_ATTENDEE_EVENT_ID + ") REFERENCES " + TABLE_EVENT + "(id) " +
            ")";
    DatabaseHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_EVENT_CREATE);
        db.execSQL(TABLE_ATTENDEE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_ATTENDEE);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_EVENT);
        db.execSQL(TABLE_EVENT_CREATE);
        db.execSQL(TABLE_ATTENDEE_CREATE);
    }
}
