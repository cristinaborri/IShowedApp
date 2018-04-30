package uk.ac.bbk.cristinaborri.ishowedapp.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationListener;
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

public class EventViewActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        LocationListener
{
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
    private GeofencingClient mGeofencingClient;


    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "Event Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters
    private ConnectionsClient mConnectionsClient;

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


    @Override
    protected void onStop() {
        mConnectionsClient.stopAllEndpoints();

        super.onStop();
    }

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
        TextView eventLocation = findViewById(R.id.view_event_location);
        eventLocation.setText(event != null ? event.getLocationName() : null);
        TextView eventAddress = findViewById(R.id.view_event_address);
        eventAddress.setText(event != null ? event.getLocationAddress() : null);
        eventData.close();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        mConnectionsClient = Nearby.getConnectionsClient(this);

        Button attendanceButton = findViewById(R.id.record_attendance);

        attendanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mConnectionsClient.sendPayload(event.getName(), Payload.fromBytes(event.getAttendeeUniqueCode().getBytes(UTF_8)));
                Log.i(TAG, "sent id:" + event.getName());
            }
        });

        startDiscovery();
    }

    // Callbacks for receiving payloads
    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
                    Log.i(TAG, "sent");
                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate update) { }

            };

    // Callbacks for connections to other devices
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                    Log.i(TAG, "connection: accepting connection");
                    // Automatically accept the connection on both sides.
                    mConnectionsClient.acceptConnection(endpointId, mPayloadCallback);
                }

                @Override
                public void onConnectionResult(@NonNull String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {
                        Log.i(TAG, "connection: connection successful, id:" + endpointId);
                    } else {
                        Log.i(TAG, "connection: connection failed");
                    }
                }

                @Override
                public void onDisconnected(@NonNull String endpointId) {
                    Log.i(TAG, "connection: disconnected");
                    mConnectionsClient.stopDiscovery();
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
                                Log.i(TAG, "connection: discovery started, service_id: "+event.getName());

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
                mConnectionsClient.stopDiscovery();
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
                        mConnectionsClient.stopDiscovery();
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

//    private PendingIntent getGeofencePendingIntent() {
//        // Reuse the PendingIntent if we already have it.
//        if (mGeofencePendingIntent != null) {
//            return mGeofencePendingIntent;
//        }
//        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
//        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
//        // calling addGeofences() and removeGeofences().
//        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
//                FLAG_UPDATE_CURRENT);
//        return mGeofencePendingIntent;
//    }

    @Override
    public void onLocationChanged(Location location) {

    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLastKnownLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // Get last known location
    private void getLastKnownLocation() {
//        Log.d(TAG, "getLastKnownLocation()");
//        if ( checkPermission() ) {
//            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//            if ( lastLocation != null ) {
//                Log.i(TAG, "LasKnown location. " +
//                        "Long: " + lastLocation.getLongitude() +
//                        " | Lat: " + lastLocation.getLatitude());
//                writeLastLocation();
//                startLocationUpdates();
//            } else {
//                Log.w(TAG, "No location retrieved yet");
//                startLocationUpdates();
//            }
//        }
//        else askPermission();
    }
}