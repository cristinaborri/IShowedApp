package uk.ac.bbk.cristinaborri.ishowedapp.model;

import android.util.Base64;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.Date;

/**
 * Created by Cristina Borri
 * This class represents the event and it's persisted using EventDAO
 */

public class Event {
    /**
     * This is the (database) identifier of the event
     */
    private long id;
    /**
     * These are the coordinates of the event locationCoordinates
     */
    private LatLng locationCoordinates;
    /**
     * This is the name of the event locationCoordinates
     */
    private String locationName;
    /**
     * This is the address of the event locationCoordinates
     */
    private String locationAddress;
    /**
     * These are the bounds for the location viewport (Map)
     */
    private LatLngBounds locationViewPort;
    /**
     * This is the date of the event
     */
    private Date date;
    /**
     * This is the name of the event
     */
    private String name;
    /**
     * These are the details of the event
     */
    private String details;

    /**
     * This is the attendee code to send back to confirm attendancy
     */
    private String attendeeUniqueCode;

    /**
     * This constructor will create en empty event
     */
    public Event() {
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LatLng getLocationCoordinates() {
        return locationCoordinates;
    }

    public void setLocationCoordinates(LatLng locationCoordinates) {
        this.locationCoordinates = locationCoordinates;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public LatLngBounds getLocationViewPort() {
        return locationViewPort;
    }

    public void setLocationViewPort(LatLngBounds locationViewPort) {
        this.locationViewPort = locationViewPort;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getAttendeeUniqueCode() {
        return attendeeUniqueCode;
    }

    public void setAttendeeUniqueCode(String attendeeUniqueCode) {
        this.attendeeUniqueCode = attendeeUniqueCode;
    }

    public boolean buildFromCode(String code)
    {
        try {
            String jsonString = new String(Base64.decode(code, Base64.NO_WRAP), Charset.defaultCharset());
            JSONObject jsonObject = new JSONObject(jsonString);
            String attendeeUid = jsonObject.getString("attendee_uid");
            setAttendeeUniqueCode(attendeeUid);
            JSONObject jsonEvent = new JSONObject(jsonObject.getString("event"));
            setName(jsonEvent.getString(DatabaseHelper.COLUMN_EVENT_NAME));
            setDetails(jsonEvent.getString(DatabaseHelper.COLUMN_EVENT_DETAILS));
            setLocationName(jsonEvent.getString(DatabaseHelper.COLUMN_LOCATION_NAME));
            setLocationAddress(jsonEvent.getString(DatabaseHelper.COLUMN_LOCATION_ADDRESS));
            setLocationCoordinates(EventDAO.latLngFromCoordinates(
                    jsonEvent.getDouble(DatabaseHelper.COLUMN_LOCATION_LONG),
                    jsonEvent.getDouble(DatabaseHelper.COLUMN_LOCATION_LAT)
            ));
            setLocationViewPort(EventDAO.viewPortFromCoordinates(
                    jsonEvent.getDouble(DatabaseHelper.COLUMN_MAP_NE_LONG),
                    jsonEvent.getDouble(DatabaseHelper.COLUMN_MAP_NE_LAT),
                    jsonEvent.getDouble(DatabaseHelper.COLUMN_MAP_SW_LONG),
                    jsonEvent.getDouble(DatabaseHelper.COLUMN_MAP_SW_LAT)
            ));
            setDate(new Date(jsonEvent.getLong(DatabaseHelper.COLUMN_EVENT_DATE)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", locationName='" + locationName + '\'' +
                ", date='" + date + '\'' +
                ", name='" + name + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
