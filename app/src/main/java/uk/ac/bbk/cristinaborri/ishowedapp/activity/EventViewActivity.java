package uk.ac.bbk.cristinaborri.ishowedapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

import uk.ac.bbk.cristinaborri.ishowedapp.MainActivity;
import uk.ac.bbk.cristinaborri.ishowedapp.R;
import uk.ac.bbk.cristinaborri.ishowedapp.model.Event;
import uk.ac.bbk.cristinaborri.ishowedapp.model.EventDAO;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by cristinaborri.
 *
 */

public class EventViewActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "IShowedApp.EventView";
    private static final Strategy STRATEGY = Strategy.P2P_STAR;

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    private EventDAO eventData;
    private Event event;
    private String currentEndpointId;

    // Values, prefixed to avoid conflicts
    private static final int ISH_GPS_MODE = 1;
    private static final int ISH_SEARCH_MODE = 2;
    private static final int ISH_CONNECTED_MODE = 3;
    private static final float ISH_RANGE = 100.0f; // in meters
    private static final long ISH_LOCATION_REQUEST_INTERVAL = 1000; // in milliseconds

    private ConnectionsClient mConnectionsClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private Button attendanceButton;
    private TextView distanceText;
    private TextView reachLocationText;
    private TextView searchingServiceText;
    private Location eventLocation;
    private int mode;


    @Override
    protected void onStart() {
        super.onStart();

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }

    /** Returns true if the app was granted all the permissions. Otherwise, returns false. */
    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /** Handles user acceptance (or denial) of our permission request. */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Cannot run the application without all the required permissions", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);
        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle("Event");
            toolbar.setDisplayHomeAsUpEnabled(true);
            toolbar.setDisplayShowHomeEnabled(true);
        }

        long eventID = getIntent().getLongExtra(MainActivity.EXTRA_EVENT_ID, 0);
        eventData = new EventDAO(this);
        eventData.open();
        event = eventData.getEvent(eventID);
        TextView eventName = findViewById(R.id.view_event_name);
        eventName.setText(event != null ? event.getName() : null);
        TextView eventDate = findViewById(R.id.view_event_date);
        eventDate.setText(event != null ? new SimpleDateFormat(MainActivity.DATE_FORMAT, Locale.UK).format(event.getDate()) : null);
        TextView eventDetails = findViewById(R.id.view_event_details);
        eventDetails.setText(event != null ? event.getDetails() : null);
        TextView eventLocationText = findViewById(R.id.view_event_location);
        eventLocationText.setText(event != null ? event.getLocationName() : null);
        TextView eventAddress = findViewById(R.id.view_event_address);
        eventAddress.setText(event != null ? event.getLocationAddress() : null);
        eventData.close();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        eventLocation = new Location("");
        eventLocation.setLatitude(event.getLocationCoordinates().latitude);
        eventLocation.setLongitude(event.getLocationCoordinates().longitude);

        mConnectionsClient = Nearby.getConnectionsClient(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(ISH_LOCATION_REQUEST_INTERVAL);

        attendanceButton = findViewById(R.id.record_attendance);
        distanceText = findViewById(R.id.distance);
        reachLocationText = findViewById(R.id.reach_location);
        searchingServiceText = findViewById(R.id.searching_service);

        attendanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentEndpointId != null) {
                    mConnectionsClient.sendPayload(currentEndpointId, Payload.fromBytes(event.getAttendeeUniqueCode().getBytes(UTF_8)));
                    Log.i(TAG, "payload sent");
                }
            }
        });

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
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
        // Start showing the distance
        showDistance();
    }

    // Callbacks for receiving payloads
    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
                    Log.i(TAG, "received");
                    // remove event
                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate update) { }
            };

    // Callbacks for connections to other devices
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                    Log.i(TAG, "connection: accepting connection, id: "+endpointId);
                    // Automatically accept the connection on both sides.
                    if (currentEndpointId != null) {
                        mConnectionsClient.disconnectFromEndpoint(currentEndpointId);
                        currentEndpointId = null;
                    }
                    mConnectionsClient.acceptConnection(endpointId, mPayloadCallback);
                }

                @Override
                public void onConnectionResult(@NonNull String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {
                        Log.i(TAG, "connection: connection successful, id:" + endpointId);
                        currentEndpointId = endpointId;
                        showAttendanceButton();
                    } else {
                        Log.i(TAG, "connection: connection failed");
                    }
                }

                @Override
                public void onDisconnected(@NonNull String endpointId) {
                    Log.i(TAG, "connection: disconnected");
                    if (currentEndpointId.equals(endpointId)) {
                        currentEndpointId = null;
                    }
                    showSearchService();
                    stopDiscovery();
                    startDiscovery();
                }
            };

    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(@NonNull String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo)
                {
                    // An endpoint was found!
                    mConnectionsClient.requestConnection(
                            discoveredEndpointInfo.getEndpointName(),
                            endpointId,
                            mConnectionLifecycleCallback)
                            .addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unusedResult) {
                                            Log.i(TAG, "connection: connecting endpoint");
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i(TAG, "connection: endpoint connection error: " + e.getMessage());
                                            stopDiscovery();
                                            startDiscovery();
                                        }
                                    });
                }

                @Override
                public void onEndpointLost(@NonNull String endpointId) {
                    Log.i(TAG, "connection: endpoint lost");
                }
            };

    private void startDiscovery() {
        mConnectionsClient.startDiscovery(
                event.getName(),
                mEndpointDiscoveryCallback,
                new DiscoveryOptions(STRATEGY))
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                // We're discovering!
                                Log.i(TAG, "connection: discovery started");

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // We were unable to start discovering.
                                Log.i(TAG, "connection: discovery failed: " + e.getMessage());
                            }
                        });
    }

    private void stopDiscovery() {
        if (currentEndpointId != null) {
            mConnectionsClient.disconnectFromEndpoint(currentEndpointId);
        }
        mConnectionsClient.stopDiscovery();
        Log.i(TAG, "connection: discovery stopped");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_show, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case android.R.id.home:
                stopDiscovery();
                i = new Intent(EventViewActivity.this, MainActivity.class);
                startActivity(i);
                return true;
            case R.id.delete_event_menu_item:
                showDeleteDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(EventViewActivity.this);
        alertBuilder.setMessage("Do you want to delete the event?");
        alertBuilder.setCancelable(true);

        alertBuilder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        eventData.open();
                        eventData.removeEvent(event);
                        eventData.close();
                        stopDiscovery();
                        addDeleteToast();
                        Intent i = new Intent(EventViewActivity.this, MainActivity.class);
                        startActivity(i);
                    }
                });

        alertBuilder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog removeAlert = alertBuilder.create();
        removeAlert.show();
    }

    private void addDeleteToast() {
        Toast t = Toast.makeText(
                EventViewActivity.this, "Event "+ event.getName() + " has been removed successfully!",
                Toast.LENGTH_SHORT
        );
        t.show();
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we
     * just add a marker near Africa.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions().position(event.getLocationCoordinates()).title("Marker"));
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen

        map.moveCamera(CameraUpdateFactory.newLatLngBounds(event.getLocationViewPort(), width, height, padding));
    }
    @Override
    protected void onResume() {
        super.onResume();
        stopLocationUpdates();
        startLocationUpdates();
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        Log.i(TAG, "location: stop updating");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        Log.i(TAG, "location: start updating");
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }
    private void locationUpdated()
    {
        if (mCurrentLocation != null) {

            float distance = mCurrentLocation.distanceTo(eventLocation);


            Log.i(TAG, "location: curr lat: "+ mCurrentLocation.getLatitude());
            Log.i(TAG, "location: curr long: " + mCurrentLocation.getLongitude());
            Log.i(TAG, "location: ev lat: " + eventLocation.getLatitude());
            Log.i(TAG, "location: ev long: " + eventLocation.getLongitude());


            if (distance < ISH_RANGE) {
                if (mode == ISH_GPS_MODE) {
                    startDiscovery();
                    showSearchService();
                }
            } else {
                if (mode == ISH_SEARCH_MODE) {
                    stopDiscovery();
                    showDistance();
                }
            }

            if (mode == ISH_GPS_MODE) {
                String distanceString = String.valueOf(Math.round(distance)) + " meters";
                distanceText.setText(distanceString);
            }

            Log.i(TAG, "location: distance: "+distance);
        }
        Log.i(TAG, "location: updated");
    }

    private void showDistance()
    {
        attendanceButton.setVisibility(View.GONE);
        searchingServiceText.setVisibility(View.GONE);
        distanceText.setVisibility(View.VISIBLE);
        reachLocationText.setVisibility(View.VISIBLE);
        mode = ISH_GPS_MODE;
    }

    private void showSearchService()
    {
        attendanceButton.setVisibility(View.GONE);
        searchingServiceText.setVisibility(View.VISIBLE);
        distanceText.setVisibility(View.GONE);
        reachLocationText.setVisibility(View.GONE);
        mode = ISH_SEARCH_MODE;
    }

    private void showAttendanceButton()
    {
        attendanceButton.setVisibility(View.VISIBLE);
        searchingServiceText.setVisibility(View.GONE);
        distanceText.setVisibility(View.GONE);
        reachLocationText.setVisibility(View.GONE);
        mode = ISH_CONNECTED_MODE;
    }
}