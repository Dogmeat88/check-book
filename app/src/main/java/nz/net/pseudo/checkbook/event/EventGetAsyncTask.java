package nz.net.pseudo.checkbook.event;

import android.app.ProgressDialog;
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

import nz.net.pseudo.checkbook.R;

/**
 * Created by i on 24/04/16.
 * QuickBook an event
 */
public class EventGetAsyncTask extends AsyncTask<Void, Void, Event> {
    private final String TAG = this.getClass().getSimpleName();
    private Calendar mCalendar;
    private EventGetIface eventGetImpl;
    private String eventId;
    private Context context;
    private ProgressDialog progressDialog;


    public EventGetAsyncTask(
              Context context
            , GoogleAccountCredential credential
            , EventGetIface eventGetImpl
            , String eventId
    ) {
        this.eventGetImpl=eventGetImpl;
        this.eventId=eventId;
        this.context=context;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(R.string.progress_dialogue_title);
        progressDialog.setMessage(context.getString(R.string.event_get_progress_content));

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        mCalendar = new Calendar.Builder(transport, jsonFactory, credential).build();
    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
    }

    @Override
    protected Event doInBackground(Void... params) {
        try {

            //Get the event
            return mCalendar.events().get(context.getString(R.string.event_primary_email),this.eventId).execute();

        } catch (Exception e) {
            Log.e(TAG, "Webservice exception: "+e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Event event) {
        progressDialog.hide();
        if(event!=null && event.getCreator()!=null){
            eventGetImpl.onEventGetSuccess(event);
        } else {
            eventGetImpl.onFail(context.getString(R.string.event_get_error_title), context.getString(R.string.event_get_error_content));
        }
    }

}
