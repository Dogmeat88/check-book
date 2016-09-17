package nz.net.pseudo.cb.event;

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

import java.util.Date;

import nz.net.pseudo.cb.AppUtils;
import nz.net.pseudo.cb.R;

/**
 * Created by i on 24/04/16.
 * Get a list of events for a specified resource
 */
public class EventsListAsyncTask extends AsyncTask<Void, Void, Events> {
    private final String TAG = this.getClass().getSimpleName();
    private final Calendar mCalendar;
    private final EventsListIface eventsListImpl;
    private final String calendarResourceId;
    private final Context context;

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

            Date startDateLimit = new Date();
            startDateLimit.setTime(startDateLimit.getTime()-AppUtils.THIRTY_MIN_IN_MS);

            Date endDateLimit = new Date();
            endDateLimit.setTime(endDateLimit.getTime()+AppUtils.TWELVE_HOURS_IN_MS);

            return mCalendar
                    .events()
                    .list(calendarResourceId)
                    .setTimeMin(new DateTime(startDateLimit))
                    .setTimeMax(new DateTime(endDateLimit))
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
