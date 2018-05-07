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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;

import java.text.SimpleDateFormat;
import java.util.Locale;

import uk.ac.bbk.cristinaborri.ishowedapp.AttendanceService;
import uk.ac.bbk.cristinaborri.ishowedapp.LocationService;
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
    public static final String TAG = "IShowedApp.EventView";
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

    // Values, prefixed to avoid conflicts
    public static final int ISH_GPS_MODE = 1;
    public static final int ISH_SEARCH_MODE = 2;
    public static final int ISH_CONNECTED_MODE = 3;

    private Button attendanceButton;
    private TextView distanceText;
    private TextView reachLocationText;
    private TextView searchingServiceText;
    private Location eventLocation;
    private int mode;
    private LocationService locationService;
    private AttendanceService attendanceService;


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

        attendanceButton = findViewById(R.id.record_attendance);
        distanceText = findViewById(R.id.distance);
        reachLocationText = findViewById(R.id.reach_location);
        searchingServiceText = findViewById(R.id.searching_service);

        attendanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attendanceService.registerAttendance(event.getAttendeeUniqueCode());
            }
        });

        // Start showing the distance
        showDistance();

        attendanceService = new AttendanceService(this, event.getName());
        locationService = new LocationService(this);
    }

    public AttendanceService getAttendanceService() {
        return attendanceService;
    }

    public Location getEventLocation() {
        return eventLocation;
    }

    public int getMode() {
        return mode;
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
                attendanceService.stopDiscovery();
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
                        attendanceService.stopDiscovery();
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

    public void registrationConfirmed()
    {
        eventData.open();
        eventData.removeEvent(event);
        eventData.close();
        attendanceService.stopDiscovery();
        Toast t = Toast.makeText(
                EventViewActivity.this, "The attendance for the Event "+ event.getName() + " has been recorded successfully!",
                Toast.LENGTH_SHORT
        );
        t.show();
        Intent i = new Intent(EventViewActivity.this, MainActivity.class);
        startActivity(i);
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * We will center the map on the event and add a marker
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
        locationService.stopLocationUpdates();
        locationService.startLocationUpdates();
    }
    @Override
    protected void onPause() {
        super.onPause();
        locationService.stopLocationUpdates();
    }

    public void showDistance()
    {
        attendanceButton.setVisibility(View.GONE);
        searchingServiceText.setVisibility(View.GONE);
        distanceText.setVisibility(View.VISIBLE);
        reachLocationText.setVisibility(View.VISIBLE);
        mode = ISH_GPS_MODE;
    }

    public void showSearchService()
    {
        attendanceButton.setVisibility(View.GONE);
        searchingServiceText.setVisibility(View.VISIBLE);
        distanceText.setVisibility(View.GONE);
        reachLocationText.setVisibility(View.GONE);
        mode = ISH_SEARCH_MODE;
    }

    public void showAttendanceButton()
    {
        attendanceButton.setVisibility(View.VISIBLE);
        searchingServiceText.setVisibility(View.GONE);
        distanceText.setVisibility(View.GONE);
        reachLocationText.setVisibility(View.GONE);
        mode = ISH_CONNECTED_MODE;
    }

    public void refreshDistance(float distance)
    {
        String distanceString = String.valueOf(Math.round(distance)) + " meters";
        distanceText.setText(distanceString);
    }
}