package uk.ac.bbk.cristinaborri.ishowedapp.ListAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import uk.ac.bbk.cristinaborri.ishowedapp.MainActivity;
import uk.ac.bbk.cristinaborri.ishowedapp.R;
import uk.ac.bbk.cristinaborri.ishowedapp.model.Event;

/**
 * Created by Cristina Borri
 * This class is an adapter used by the event list page to display the event
 */
public class EventsListItemAdapter extends ArrayAdapter<Event> {
    public EventsListItemAdapter(Context context, List<Event> events) {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        Event event = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                R.layout.list_item_event, parent, false
            );
        }
        // Lookup view for event name
        TextView eventName = convertView.findViewById(R.id.listEventName);
        eventName.setText(event != null ? event.getName() : null);
        TextView eventDate = convertView.findViewById(R.id.listEventDate);
        eventDate.setText(event != null ? new SimpleDateFormat(MainActivity.DATE_FORMAT, Locale.UK).format(event.getDate()) : null);
        // Return the completed view to render on screen
        return convertView;
    }
}