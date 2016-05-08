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

/**
 * Created by i on 24/04/16.
 * QuickBook an event
 */
public class EventUnbookAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private final String TAG = this.getClass().getSimpleName();
    private Calendar mCalendar;
    private EventUnbookIface eventUnbookImpl;
    private Event unbookEvent;
    private Context context;


    public EventUnbookAsyncTask(
              Context context
            , GoogleAccountCredential credential
            , EventUnbookIface eventUnbookImpl
            , Event unbookEvent
    ) {
        this.eventUnbookImpl = eventUnbookImpl;
        this.unbookEvent = unbookEvent;
        this.context=context;

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        mCalendar = new Calendar.Builder(transport, jsonFactory, credential).build();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {

            mCalendar.events().delete("primary", this.unbookEvent.getId()).execute();
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Webservice exception: "+e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        if (isSuccess == null || !isSuccess) {
            eventUnbookImpl.onFail(context.getString(R.string.event_unbook_error_title), context.getString(R.string.event_unbook_error_content));
        }else if(isSuccess){
            eventUnbookImpl.onEventUnbookSuccess();
        }
    }

}
