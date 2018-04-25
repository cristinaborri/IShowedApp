package uk.ac.bbk.cristinaborri.ishowedapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.ac.bbk.cristinaborri.ishowedapp.MainActivity;
import uk.ac.bbk.cristinaborri.ishowedapp.R;
import uk.ac.bbk.cristinaborri.ishowedapp.model.Event;
import uk.ac.bbk.cristinaborri.ishowedapp.model.EventDAO;

/**
 * Created by Cristina Borri
 * This class is the activity called to update or create an event
 */

public class EventAddActivity extends AppCompatActivity {

    private EditText codeEditText;
    private Event event;
    private EventDAO eventData;
    private String mode;
    private long eventID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_add);

        event = new Event();
        codeEditText = findViewById(R.id.unique_code);

        eventData = new EventDAO(this);
        eventData.open();

        String toolbarTitle = "Add Event";

        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle(toolbarTitle);
            toolbar.setDisplayHomeAsUpEnabled(true);
            toolbar.setDisplayShowHomeEnabled(true);
        }
    }

    private void saveEvent() {
//        event.setName(nameEditText.getText().toString());
//        setEventDateFromEditText(event);
//        event.setDetails(detailsEditText.getText().toString());
//        if(mode.equals("Add")) {
//            eventData.addEvent(event);
//            eventData.close();
//            addSuccessToast();
//            Intent i = new Intent(EventAddUpdateActivity.this, MainActivity.class);
//            startActivity(i);
//        } else {
//            eventData.updateEvent(event);
//            eventData.close();
//            addSuccessToast();
//            Intent i = new Intent(EventAddUpdateActivity.this, EventViewActivity.class);
//            i.putExtra(MainActivity.EXTRA_EVENT_ID, eventID);
//            startActivity(i);
//        }
    }

    private void addSuccessToast() {
        String action = "added";
        if(mode.equals("Update")) {
            action = "updated";
        }
        Toast t = Toast.makeText(
                EventAddActivity.this, "Event "+ event.getName() + " has been " + action + " successfully!",
                Toast.LENGTH_SHORT
        );
        t.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                eventData.close();
                onBackPressed();
                return true;
            case R.id.save_event:
                saveEvent();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}