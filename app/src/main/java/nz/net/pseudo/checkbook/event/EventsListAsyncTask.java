package nz.net.pseudo.checkbook.event;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;

import nz.net.pseudo.checkbook.R;

/**
 * Created by i on 24/04/16.
 * Get a list of events for a specified resource
 */
public class EventsListAsyncTask extends AsyncTask<Void, Void, Events> {
    private final String TAG = this.getClass().getSimpleName();
    private Calendar mCalendar;
    private EventsListIface eventsListImpl;
    private String calendarResourceId;
    private Context context;

    public EventsListAsyncTask(Context context, GoogleAccountCredential credential, EventsListIface eventsListImpl, String calendarResourceId) {
        this.eventsListImpl=eventsListImpl;
        this.calendarResourceId=calendarResourceId;
        this.context=context;


        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        mCalendar = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential).build();
    }

    @Override
    protected Events doInBackground(Void... params) {
        try {
            java.util.Calendar startDateLimit = java.util.Calendar.getInstance();
            startDateLimit.add(java.util.Calendar.MINUTE, -15);

            java.util.Calendar endDateLimit = java.util.Calendar.getInstance();
            endDateLimit.add(java.util.Calendar.HOUR, +12);

            return mCalendar
                    .events()
                    .list(calendarResourceId)
                    .setTimeMin(new DateTime(startDateLimit.getTime()))
                    .setTimeMax(new DateTime(endDateLimit.getTime()))
                    .setSingleEvents(true)
                    .setOrderBy("startTime")
                    .execute();

        } catch (Exception e) {
            Log.e(TAG, "Webservice exception: "+e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Events events) {
        if (events == null) {
            eventsListImpl.onFail(this.context.getString(R.string.events_loading_error_title), this.context.getString(R.string.events_loading_error_content));
        } else {
            eventsListImpl.onEventsListSuccess(events);
        }
    }

}
