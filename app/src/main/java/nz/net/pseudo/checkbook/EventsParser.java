package nz.net.pseudo.checkbook;

import android.content.Context;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by i on 30/04/16.
 */
public class EventsParser {

    private final int ONE_MIN_IN_MS = 60000;
    private final int FIFTEEN_MIN_IN_MS = 15*ONE_MIN_IN_MS;
    private final int TWO_HOURS_IN_MS = 120*ONE_MIN_IN_MS;

    Context context;

    public EventsParser(Context context) {
        this.context=context;
    }

    public Events parseEvents(Events rawEvents){

        List<Event> fillEvents = new ArrayList<>();

        for(int i = 0; i < rawEvents.getItems().size(); i++) {
            Event currentEvent = rawEvents.getItems().get(i);
            fillEvents.add(currentEvent);

            if(i+1 < rawEvents.getItems().size()) {
                Event nextEvent = rawEvents.getItems().get(i + 1);
                fillEvents.addAll(fillGaps(currentEvent, nextEvent));
            }
        }

        //must be at least one entry
        if(rawEvents.getItems().size()>0) {
            //Overshoot the last event by a couple of hours and populate with Book events
            Event lastEvent = rawEvents.getItems().get(rawEvents.getItems().size() - 1);

            Event overshootEvent = new Event();
            overshootEvent.setSummary(context.getString(R.string.book_event_title));

            EventDateTime overshootEventStartDateTime = new EventDateTime();
            overshootEventStartDateTime.setDateTime(AppUtils.roundFifteenMinutes(new DateTime(lastEvent.getEnd().getDateTime().getValue() + TWO_HOURS_IN_MS)));
            overshootEvent.setStart(overshootEventStartDateTime);

            EventDateTime overshootEventEndDateTime = new EventDateTime();
            overshootEventEndDateTime.setDateTime(new DateTime(overshootEventStartDateTime.getDateTime().getValue() + FIFTEEN_MIN_IN_MS));
            overshootEvent.setEnd(overshootEventEndDateTime);

            fillEvents.addAll(fillGaps(lastEvent, overshootEvent));
        }


        rawEvents.setItems(fillEvents);

        return rawEvents;
    }

    private List<Event> fillGaps(Event currentEvent, Event nextEvent){
        List<Event> bookEvents = new ArrayList<>();
        Long durationStartDate = currentEvent.getEnd().getDateTime().getValue();
        Long durationEndDate = nextEvent.getStart().getDateTime().getValue();

        while(durationStartDate<durationEndDate){

            Event bookEvent = new Event();
            bookEvent.setSummary(context.getString(R.string.book_event_title));
            bookEvent.setStart(new EventDateTime().setDateTime(new DateTime(durationStartDate)));

            //Set the end datetime 15min after the last event (including "Book" events) and round it up if it is not 1/4 hour
            DateTime bookEventEndDate = AppUtils.roundFifteenMinutes(new DateTime(durationStartDate+FIFTEEN_MIN_IN_MS));
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
}
