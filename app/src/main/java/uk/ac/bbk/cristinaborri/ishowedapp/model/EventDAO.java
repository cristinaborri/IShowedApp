package uk.ac.bbk.cristinaborri.ishowedapp.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Cristina Borri
 * This class will provide the operations that will allow to save update and load the events
 */
public class EventDAO extends IsaDao {
    private static final String[] allColumns = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_LOCATION_LONG,
            DatabaseHelper.COLUMN_LOCATION_LAT,
            DatabaseHelper.COLUMN_LOCATION_ADDRESS,
            DatabaseHelper.COLUMN_LOCATION_NAME,
            DatabaseHelper.COLUMN_MAP_NE_LONG,
            DatabaseHelper.COLUMN_MAP_NE_LAT,
            DatabaseHelper.COLUMN_MAP_SW_LONG,
            DatabaseHelper.COLUMN_MAP_SW_LAT,
            DatabaseHelper.COLUMN_EVENT_DATE,
            DatabaseHelper.COLUMN_EVENT_NAME,
            DatabaseHelper.COLUMN_EVENT_DETAILS,
            DatabaseHelper.COLUMN_UNIQUE_CODE
    };

    public EventDAO(Context context){
        dbHandler = new DatabaseHelper(context);
    }

    public void addEvent(Event event){
        ContentValues values = prepareEventContentValues(event);
        long insertId = database.insert(DatabaseHelper.TABLE_EVENT,null,values);
        event.setId(insertId);
    }

    // Getting single Event
    public Event getEvent(long id) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_EVENT,
                allColumns,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null,
                null
        );
        if (cursor != null) {
            cursor.moveToFirst();

            Event e = this.eventFromCursor(cursor);
            cursor.close();
            return e;
        }
        return new Event();
    }

    public List<Event> getAllEvents() {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_EVENT,
                allColumns,null,
                null,
                null,
                null,
                null
        );

        List<Event> events = new ArrayList<>();
        if(cursor.getCount() > 0){
            while(cursor.moveToNext()){
                events.add(this.eventFromCursor(cursor));
            }
        }
        // return All Events
        return events;
    }

    // Updating Event
    public void updateEvent(Event event) {
        ContentValues values = prepareEventContentValues(event);
        // updating row
        database.update(DatabaseHelper.TABLE_EVENT, values,
                DatabaseHelper.COLUMN_ID + "=?",new String[] { String.valueOf(event.getId())});
    }

    // Deleting Event
    public void removeEvent(Event event) {
        database.delete(DatabaseHelper.TABLE_EVENT, DatabaseHelper.COLUMN_ID + "=" + event.getId(), null);
    }

    public static LatLng latLngFromCoordinates(double longitude, double latitude) {
        return new LatLng(latitude,longitude);
    }

    @NonNull
    public static LatLngBounds viewPortFromCoordinates(double neLong, double neLat, double swLong, double swLat) {
        return new LatLngBounds(new LatLng(swLat,swLong), new LatLng(neLat,neLong));
    }

    @NonNull
    private ContentValues prepareEventContentValues(Event event) {
        ContentValues values  = new ContentValues();
        if(event.getLocationCoordinates() == null) {
            event.setLocationCoordinates(new LatLng(0,0));
        }
        if(event.getDate() == null) {
            event.setDate(new Date());
        }
        if(event.getLocationViewPort() == null) {
            event.setLocationViewPort(
                    new LatLngBounds(new LatLng(0,0), new LatLng(0,0))
            );
        }
        values.put(DatabaseHelper.COLUMN_LOCATION_LONG, event.getLocationCoordinates().longitude);
        values.put(DatabaseHelper.COLUMN_LOCATION_LAT, event.getLocationCoordinates().latitude);
        values.put(DatabaseHelper.COLUMN_LOCATION_ADDRESS, event.getLocationAddress());
        values.put(DatabaseHelper.COLUMN_LOCATION_NAME, event.getLocationName());
        values.put(DatabaseHelper.COLUMN_MAP_NE_LONG, event.getLocationViewPort().northeast.longitude);
        values.put(DatabaseHelper.COLUMN_MAP_NE_LAT, event.getLocationViewPort().northeast.latitude);
        values.put(DatabaseHelper.COLUMN_MAP_SW_LONG, event.getLocationViewPort().southwest.longitude);
        values.put(DatabaseHelper.COLUMN_MAP_SW_LAT, event.getLocationViewPort().southwest.latitude);
        values.put(DatabaseHelper.COLUMN_EVENT_DATE, event.getDate().getTime());
        values.put(DatabaseHelper.COLUMN_EVENT_NAME, event.getName());
        values.put(DatabaseHelper.COLUMN_EVENT_DETAILS, event.getDetails());
        values.put(DatabaseHelper.COLUMN_UNIQUE_CODE, event.getAttendeeUniqueCode());
        return values;
    }

    private Event eventFromCursor(Cursor cursor) {
        Event event = new Event();
        event.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)));
        event.setLocationCoordinates(latLngFromCoordinates(
                cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION_LONG)),
                cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION_LAT))
        ));
        event.setLocationViewPort(viewPortFromCoordinates(
                cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_MAP_NE_LONG)),
                cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_MAP_NE_LAT)),
                cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_MAP_SW_LONG)),
                cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_MAP_SW_LAT))
        ));
        event.setLocationAddress(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION_ADDRESS)));
        event.setLocationName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION_NAME)));

        event.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_DATE))));
        event.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_NAME)));
        event.setDetails(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_DETAILS)));

        return event;
    }
 }
