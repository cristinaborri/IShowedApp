package uk.ac.bbk.cristinaborri.ishowedapp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import uk.ac.bbk.cristinaborri.ishowedapp.model.DatabaseHelper;
import uk.ac.bbk.cristinaborri.ishowedapp.model.Event;
import uk.ac.bbk.cristinaborri.ishowedapp.model.EventDAO;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class EventOperationsTest {
    private static final Date DATE_EVENT_1 = new Date(1544400000000L);
    private final LatLng LOCATION_COORDINATES_1 = EventDAO.latLngFromCoordinates(
            -80.1055376, 26.2754584
    );
    private final LatLngBounds WP_EVENT_1 = EventDAO.viewPortFromCoordinates(
            -80.1056376, 26.2755584,
            -80.1054376, 26.2752584
    );
    private static final String NAME_EVENT_1 = "My event 1";
    private static final String DETAILS_EVENT_1 = "details 1";


    private static final Date DATE_EVENT_2 = new Date(1539907200000L);
    private final LatLng LOCATION_COORDINATES_2 = EventDAO.latLngFromCoordinates(
            -80.0769872, 26.3181345
    );
    private final LatLngBounds WP_EVENT_2 = EventDAO.viewPortFromCoordinates(
            -80.1056371, 26.2755582,
            -80.1054371, 26.2752582
    );
    private static final String NAME_EVENT_2 = "My event 2";
    private static final String DETAILS_EVENT_2 = "details 2";
    private Context appContext;
    private Event event1;
    private Event event2;
    private EventDAO eventData;

    @Before
    public void setUp() throws Exception {
        // Context of the app under test.
        appContext = InstrumentationRegistry.getTargetContext();
        appContext.deleteDatabase(DatabaseHelper.DATABASE_NAME);
        event1 = new Event();
        event1.setDate(DATE_EVENT_1);
        event1.setLocationCoordinates(LOCATION_COORDINATES_1);
        event1.setLocationViewPort(WP_EVENT_1);
        event1.setName(NAME_EVENT_1);
        event1.setDetails(DETAILS_EVENT_1);
        event2 = new Event();
        event2.setDate(DATE_EVENT_2);
        event2.setLocationCoordinates(LOCATION_COORDINATES_2);
        event2.setLocationViewPort(WP_EVENT_2);
        event2.setName(NAME_EVENT_2);
        event2.setDetails(DETAILS_EVENT_2);
        eventData = new EventDAO(appContext);
        eventData.open();
    }

    @After
    public void tearDown() throws Exception {
        eventData.close();
    }

    @Test
    public void useAppContext() throws Exception {
        assertEquals("uk.ac.bbk.cristinaborri.ishowedapp", appContext.getPackageName());
    }

    @Test
    public void addAndGetEvent() throws Exception {
        eventData.addEvent(event1);
        Event foundEvent = eventData.getEvent(event1.getId());
        validateEvent1(foundEvent);
    }

    @Test
    public void getEventsList() throws Exception {
        eventData.addEvent(event1);
        eventData.addEvent(event2);
        List<Event> events = eventData.getAllEvents();
        assertEquals(2, events.size());
        validateEvent1(events.get(0));
        validateEvent2(events.get(1));
    }

    @Test
    public void deleteEvent() throws Exception {
        eventData.addEvent(event1);
        eventData.addEvent(event2);
        List<Event> startingEvents = eventData.getAllEvents();
        assertEquals(2, startingEvents.size());
        eventData.removeEvent(startingEvents.get(0));
        List<Event> events = eventData.getAllEvents();
        assertEquals(1, events.size());
        validateEvent2(events.get(0));
    }

    @Test
    public void updateEvent() throws Exception {
        eventData.addEvent(event1);
        Event event = eventData.getEvent(event1.getId());
        validateEvent1(event);
        event.setDate(DATE_EVENT_2);
        event.setLocationCoordinates(LOCATION_COORDINATES_2);
        event.setLocationViewPort(WP_EVENT_2);
        event.setName(NAME_EVENT_2);
        event.setDetails(DETAILS_EVENT_2);
        eventData.updateEvent(event);
        validateEvent2(event);
    }

    private void validateEvent1(Event event) {
        assertEquals(NAME_EVENT_1, event.getName());
        assertEquals(DATE_EVENT_1, event.getDate());
        assertEquals(LOCATION_COORDINATES_1.latitude, event.getLocationCoordinates().latitude, 0);
        assertEquals(LOCATION_COORDINATES_1.longitude, event.getLocationCoordinates().longitude, 0);
        assertEquals(WP_EVENT_1.northeast.latitude, event.getLocationViewPort().northeast.latitude, 0);
        assertEquals(WP_EVENT_1.northeast.longitude, event.getLocationViewPort().northeast.longitude, 0);
        assertEquals(WP_EVENT_1.southwest.latitude, event.getLocationViewPort().southwest.latitude, 0);
        assertEquals(WP_EVENT_1.southwest.longitude, event.getLocationViewPort().southwest.longitude, 0);
        assertEquals(DETAILS_EVENT_1, event.getDetails());
    }

    private void validateEvent2(Event event) {
        assertEquals(NAME_EVENT_2, event.getName());
        assertEquals(DATE_EVENT_2, event.getDate());
        assertEquals(LOCATION_COORDINATES_2.latitude, event.getLocationCoordinates().latitude, 0);
        assertEquals(LOCATION_COORDINATES_2.longitude, event.getLocationCoordinates().longitude, 0);
        assertEquals(WP_EVENT_2.northeast.latitude, event.getLocationViewPort().northeast.latitude, 0);
        assertEquals(WP_EVENT_2.northeast.longitude, event.getLocationViewPort().northeast.longitude, 0);
        assertEquals(WP_EVENT_2.southwest.latitude, event.getLocationViewPort().southwest.latitude, 0);
        assertEquals(WP_EVENT_2.southwest.longitude, event.getLocationViewPort().southwest.longitude, 0);
        assertEquals(DETAILS_EVENT_2, event.getDetails());
    }
}