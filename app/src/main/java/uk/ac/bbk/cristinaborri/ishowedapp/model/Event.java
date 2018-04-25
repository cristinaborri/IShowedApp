package uk.ac.bbk.cristinaborri.ishowedapp.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONException;
import org.json.JSONObject;

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

    public String jsonSerialize()
    {
        JSONObject json = new JSONObject();
        if(this.getLocationCoordinates() == null) {
            this.setLocationCoordinates(new LatLng(0,0));
        }
        if(this.getDate() == null) {
            this.setDate(new Date());
        }
        if(this.getLocationViewPort() == null) {
            this.setLocationViewPort(
                    new LatLngBounds(new LatLng(0,0), new LatLng(0,0))
            );
        }
        try {
            json.put(DatabaseHelper.COLUMN_LOCATION_LONG, this.getLocationCoordinates().longitude);
            json.put(DatabaseHelper.COLUMN_LOCATION_LAT, this.getLocationCoordinates().latitude);
            json.put(DatabaseHelper.COLUMN_LOCATION_ADDRESS, this.getLocationAddress());
            json.put(DatabaseHelper.COLUMN_LOCATION_NAME, this.getLocationName());
            json.put(DatabaseHelper.COLUMN_MAP_NE_LONG, this.getLocationViewPort().northeast.longitude);
            json.put(DatabaseHelper.COLUMN_MAP_NE_LAT, this.getLocationViewPort().northeast.latitude);
            json.put(DatabaseHelper.COLUMN_MAP_SW_LONG, this.getLocationViewPort().southwest.longitude);
            json.put(DatabaseHelper.COLUMN_MAP_SW_LAT, this.getLocationViewPort().southwest.latitude);
            json.put(DatabaseHelper.COLUMN_EVENT_DATE, this.getDate().getTime());
            json.put(DatabaseHelper.COLUMN_EVENT_NAME, this.getName());
            json.put(DatabaseHelper.COLUMN_EVENT_DETAILS, this.getDetails());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
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
