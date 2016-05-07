package nz.net.pseudo.checkbook;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 * Created by i on 24/04/16.
 * QuickBook an event
 */
public class EventBookAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private final String TAG = this.getClass().getSimpleName();
    private Calendar mCalendar;
    private EventBookIface eventBookImpl;
    private String calendarResourceId;
    private Event bookEvent;
    private Context context;


    public EventBookAsyncTask(
              Context context
            , GoogleAccountCredential credential
            , EventBookIface eventBookImpl
            , String calendarResourceId
            , Event bookEvent
    ) {
        this.eventBookImpl=eventBookImpl;
        this.calendarResourceId=calendarResourceId;
        this.bookEvent=bookEvent;
        this.context=context;

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        mCalendar = new Calendar.Builder(transport, jsonFactory, credential).build();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {

            //Lets double check the availability of the room time slot
            Events events = mCalendar
                    .events()
                    .list(calendarResourceId)
                    .setTimeMin(this.bookEvent.getStart().getDateTime())
                    .setTimeMax(this.bookEvent.getEnd().getDateTime())
                    .setSingleEvents(true)
                    .setOrderBy("startTime")
                    .execute();

            //if we are happy the timeslot is still free, then book it
            if(events.getItems().size()==0){
                mCalendar.events().insert("primary", this.bookEvent).execute();
            }else{
                //Someone must have snuck in and booked the room
                return false;
            }

            return true;

        } catch (Exception e) {
            Log.e(TAG, "Webservice exception: "+e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        if (isSuccess == null) {
            eventBookImpl.onFail(context.getString(R.string.event_booking_error_title), context.getString(R.string.event_booking_error_content));
        }else if(isSuccess){
            eventBookImpl.onEventBookSuccess();
        }else{
            eventBookImpl.onFail(context.getString(R.string.event_booking_conflict_title), context.getString(R.string.event_booking_conflict_content));
        }
    }

}
