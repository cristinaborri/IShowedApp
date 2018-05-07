package uk.ac.bbk.cristinaborri.ishowedapp;

import android.annotation.SuppressLint;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import uk.ac.bbk.cristinaborri.ishowedapp.activity.EventViewActivity;

/**
 * Created by Cristina Borri
 * This class will provide the code to manage the geolocation
 * using Fused Location Provider
 */
public class LocationService {
    private static final String TAG = EventViewActivity.TAG;

    private EventViewActivity activity;
    private AttendanceService attendanceService;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;

    private static final float ISH_RANGE = 100.0f; // in meters
    private static final long ISH_LOCATION_REQUEST_INTERVAL = 1000; // in milliseconds

    @SuppressLint("MissingPermission")
    public LocationService(EventViewActivity targetActivity) {
        activity = targetActivity;

        attendanceService = activity.getAttendanceService();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(ISH_LOCATION_REQUEST_INTERVAL);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            locationUpdated();
                            mCurrentLocation = location;
                        }
                    }
                });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    locationUpdated();
                    mCurrentLocation = location;
                }
            }
        };
    }

    // Starts updating the location
    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        Log.i(TAG, "location: start updating");
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
        attendanceService.restartDiscovery();
    }

    // Stops updating the location
    public void stopLocationUpdates() {
        Log.i(TAG, "location: stop updating");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        attendanceService.stopDiscovery();
    }

    // Function called every time a new location is received
    private void locationUpdated()
    {
        Location eventLocation = activity.getEventLocation();
        if (mCurrentLocation != null) {

            float distance = mCurrentLocation.distanceTo(eventLocation);


            Log.i(TAG, "location: curr lat: "+ mCurrentLocation.getLatitude());
            Log.i(TAG, "location: curr long: " + mCurrentLocation.getLongitude());
            Log.i(TAG, "location: ev lat: " + eventLocation.getLatitude());
            Log.i(TAG, "location: ev long: " + eventLocation.getLongitude());


            if (distance < ISH_RANGE) {
                if (activity.getMode() == EventViewActivity.ISH_GPS_MODE) {
                    attendanceService.restartDiscovery();
                    activity.showSearchService();
                }
            } else {
                if (activity.getMode() == EventViewActivity.ISH_SEARCH_MODE) {
                    attendanceService.stopDiscovery();
                    activity.showDistance();
                }
            }

            if (activity.getMode() == EventViewActivity.ISH_GPS_MODE) {
                activity.refreshDistance(distance);
            }

            Log.i(TAG, "location: distance: "+distance);
        }
        Log.i(TAG, "location: updated");
    }
}
