package nz.net.pseudo.checkbook.event;

import android.app.Activity;
import android.content.Context;

import com.google.api.client.util.DateTime;
import com.google.api.services.admin.directory.model.CalendarResource;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nz.net.pseudo.checkbook.AppUtils;
import nz.net.pseudo.checkbook.R;

/**
 * Created by i on 30/04/16.
 */
public class EventsParser {

    private final int ONE_MIN_IN_MS = 60000;
    private int quickBookDurationMs;
    private final int QUICK_BOOK_BUFFER_MS = 60*ONE_MIN_IN_MS;


    private Context context;
    private CalendarResource selectedResource;

    public EventsParser(Context context) {
        this.context=context;
    }

    public Events parseEvents(Events rawEvents, CalendarResource selectedResource){

        this.quickBookDurationMs = ((Activity)context).getPreferences(Context.MODE_PRIVATE).getInt(AppUtils.QUICKBOOK_DURATION_KEY, context.getResources().getInteger(R.integer.settings_event_duration_15min));
        this.selectedResource=selectedResource;

        List<Event> fillEvents = new ArrayList<>();


        //Past event is 15min ago till now
        Date now = new Date();
        now.setTime(now.getTime() - quickBookDurationMs);

        DateTime pastDateTime = AppUtils.roundFifteenMinutes(new DateTime(now.getTime()));
        EventDateTime pastEventDateTime= new EventDateTime();
        pastEventDateTime.setDateTime(pastDateTime);
        Event pastEvent = new Event();
        pastEvent.setEnd(pastEventDateTime);

        //must be at least one entry
        if(rawEvents.getItems().size()>0) {

            //fill events leading up to the first event
            if(rawEvents.getItems().get(0).getStart().getDateTime().getValue()>pastEvent.getEnd().getDateTime().getValue()){
                fillEvents.addAll(fillGaps(pastEvent, rawEvents.getItems().get(0)));
            }

            //Loop through the events and fill in the gaps with quick book events
            for(int i = 0; i < rawEvents.getItems().size(); i++) {
                Event currentEvent = rawEvents.getItems().get(i);
                fillEvents.add(currentEvent);

                if(i+1 < rawEvents.getItems().size()) {
                    Event nextEvent = rawEvents.getItems().get(i + 1);
                    fillEvents.addAll(fillGaps(currentEvent, nextEvent));
                }
            }

            //Overshoot the last event by a couple of hours and populate with Book events
            Event lastEvent = rawEvents.getItems().get(rawEvents.getItems().size() - 1);

            Event overshootEvent = createCoreQuickBookEvent();

            EventDateTime overshootEventStartDateTime = new EventDateTime();
            overshootEventStartDateTime.setDateTime(AppUtils.roundFifteenMinutes(new DateTime(lastEvent.getEnd().getDateTime().getValue() + QUICK_BOOK_BUFFER_MS)));
            overshootEvent.setStart(overshootEventStartDateTime);

            EventDateTime overshootEventEndDateTime = new EventDateTime();
            overshootEventEndDateTime.setDateTime(new DateTime(overshootEventStartDateTime.getDateTime().getValue() + quickBookDurationMs));
            overshootEvent.setEnd(overshootEventEndDateTime);

            fillEvents.addAll(fillGaps(lastEvent, overshootEvent));
        } else {

            //If there are no events, then add a few book events after the current time
            now.setTime(now.getTime() + QUICK_BOOK_BUFFER_MS);
            DateTime futureStartDateTime = AppUtils.roundFifteenMinutes(new DateTime(now.getTime()));
            EventDateTime futureStartEventDateTime = new EventDateTime();
            futureStartEventDateTime.setDateTime(futureStartDateTime);

            Event futureEvent = new Event();
            futureEvent.setStart(futureStartEventDateTime);

            fillEvents.addAll(fillGaps(pastEvent, futureEvent));

        }


        rawEvents.setItems(fillEvents);

        return rawEvents;
    }

    private List<Event> fillGaps(Event currentEvent, Event nextEvent){
        List<Event> bookEvents = new ArrayList<>();
        Long durationStartDate = currentEvent.getEnd().getDateTime().getValue();
        Long durationEndDate = nextEvent.getStart().getDateTime().getValue();

        while(durationStartDate<durationEndDate){

            Event bookEvent = createCoreQuickBookEvent();

            bookEvent.setStart(new EventDateTime().setDateTime(new DateTime(durationStartDate)));

            //Set the end datetime 15min after the last event (including "Book" events) and round it up if it is not 1/4 hour
            DateTime bookEventEndDate = AppUtils.roundFifteenMinutes(new DateTime(durationStartDate + quickBookDurationMs));
            if(bookEventEndDate.getValue()>durationEndDate){
                //If the end of the new event is after the start of the next event, then make it the same to avoid overlap
                bookEventEndDate = new DateTime(durationEndDate);
            }

            bookEvent.setEnd(new EventDateTime().setDateTime(bookEventEndDate));

            bookEvents.add(bookEvent);
            //Update the duration start date to then end on the new "Book" Event
            durationStartDate = bookEventEndDate.getValue();
        }

        return bookEvents;
    }

    private Event createCoreQuickBookEvent(){

        Event coreBookEvent = new Event();
        coreBookEvent.setSummary(context.getString(R.string.book_event_title));

        //Add the resource to event to allow us to book it if required
        List<EventAttendee> attendeesList = new ArrayList<>();
        attendeesList.add(new EventAttendee().setEmail(this.selectedResource.getResourceEmail()).setResource(true));
        coreBookEvent.setAttendees(attendeesList);

        //Remove reminders
        coreBookEvent.setReminders(new Event.Reminders());
        coreBookEvent.getReminders().setUseDefault(false);

        //Change colour to green
        coreBookEvent.setColorId("10");

        return coreBookEvent;

    }
}
