package uk.ac.bbk.cristinaborri.ishowedapp.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.io.DataOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import uk.ac.bbk.cristinaborri.ishowedapp.MainActivity;
import uk.ac.bbk.cristinaborri.ishowedapp.R;
import uk.ac.bbk.cristinaborri.ishowedapp.model.Event;
import uk.ac.bbk.cristinaborri.ishowedapp.model.EventDAO;

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

    private EventDAO eventData;
    private Event event;
    private GeofencingClient mGeofencingClient;

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pDnsSdServiceInfo mService;

    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "Event Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters


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

        Button attendanceButton = findViewById(R.id.record_attendance);

        attendanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findServices();
            }
        });
    }

    public void findServices() {
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (mManager != null) {
            mChannel = mManager.initialize(this, getMainLooper(), null);
            if (mChannel == null) {
                //Failure to set up connection
                Toast t = Toast.makeText(
                        EventViewActivity.this, "Failed to set up connection with wifi p2p service",
                        Toast.LENGTH_SHORT
                );
                t.show();
            }
            mManager.setDnsSdResponseListeners(mChannel,
                    new WifiP2pManager.DnsSdServiceResponseListener() {
                        @Override
                        public void onDnsSdServiceAvailable(String instanceName,
                                                            String registrationType, WifiP2pDevice device) {
                            Log.v("s", "A");
                            Log.v("s", "Service Found: "+instanceName+":"+registrationType);

                        }
                    },
                    new WifiP2pManager.DnsSdTxtRecordListener() {
                        @Override
                        public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record,
                                                              WifiP2pDevice device) {
                            Log.v("s", "B");
                            Log.v("s", device.deviceName);
                            WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = device.deviceAddress;
                            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                                @Override
                                public void onSuccess() {
                                    //success logic
                                    int e =  1;
                                }

                                @Override
                                public void onFailure(int reason) {
                                    //failure logic
                                }
                            });

                            //new SocketServerTask().execute(device);
                        }
                    });

            WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
            mManager.addServiceRequest(mChannel, serviceRequest,
                    new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.v("s", "Added service discovery request");
                        }

                        @Override
                        public void onFailure(int error) {
                            Log.v("s", "Failed adding service discovery request: "+error);
                        }
                    });

            mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.v("s", "Service discovery initiated");
                }

                @Override
                public void onFailure(int arg0) {
                    Log.v("s", "Service discovery failed");
                }
            });

        } else {
            Toast t = Toast.makeText(
                    EventViewActivity.this, "WIFI p2p unavailable",
                    Toast.LENGTH_SHORT
            );
            t.show();
        }
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

    private class SocketServerTask extends AsyncTask<WifiP2pDevice, Void, Void> {
        private WifiP2pDevice device;
        private boolean success;

        @Override
        protected Void doInBackground(WifiP2pDevice... params) {
            device = params[0];
            Log.v("s", device.deviceName);// Create a new Socket instance and connect to host
            Socket socket = null;
            try {
                socket = new Socket(device.deviceAddress, 0);
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(event.getAttendeeUniqueCode());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
//            if (success) {
//                Toast.makeText(PlayListTestActivity.this, "Connection Done", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(PlayListTestActivity.this, "Unable to connect", Toast.LENGTH_SHORT).show();
//            }
        }
    }

}