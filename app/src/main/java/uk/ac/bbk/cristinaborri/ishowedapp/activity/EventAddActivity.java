package uk.ac.bbk.cristinaborri.ishowedapp.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import uk.ac.bbk.cristinaborri.ishowedapp.MainActivity;
import uk.ac.bbk.cristinaborri.ishowedapp.R;
import uk.ac.bbk.cristinaborri.ishowedapp.model.Event;
import uk.ac.bbk.cristinaborri.ishowedapp.model.EventDAO;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

/**
 * Created by Cristina Borri
 * This class is the activity called to update or create an event
 */

public class EventAddActivity extends AppCompatActivity {

    private EditText codeEditText;
    private Event event;
    private EventDAO eventData;

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

        Button clipboardButton = findViewById(R.id.paste_from_clipboard);
        clipboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                // If it does contain data and  the clipboard contains plain text.
                assert clipboard != null;
                if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                    codeEditText.setText(item.getText().toString());
                }
            }
        });
    }

    private void saveEvent() {
        String code = codeEditText.getText().toString();
        if (event.buildFromCode(code)){
            eventData.addEvent(event);
            eventData.close();
            addSuccessToast();
            Intent i = new Intent(EventAddActivity.this, MainActivity.class);
            startActivity(i);
        } else {
            Toast t = Toast.makeText(
                    EventAddActivity.this, "Invalid Code",
                    Toast.LENGTH_SHORT
            );
            t.show();
        }
    }

    private void addSuccessToast() {
        Toast t = Toast.makeText(
                EventAddActivity.this, "Event "+ event.getName() + " has been added successfully!",
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