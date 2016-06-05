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
public class EventUnbookAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private final String TAG = this.getClass().getSimpleName();
    private final Calendar mCalendar;
    private final EventUnbookIface eventUnbookImpl;
    private final Event unbookEvent;
    private final Context context;
    private final ProgressDialog progressDialog;

    public EventUnbookAsyncTask(
              Context context
            , GoogleAccountCredential credential
            , EventUnbookIface eventUnbookImpl
            , Event unbookEvent
    ) {
        this.eventUnbookImpl = eventUnbookImpl;
        this.unbookEvent = unbookEvent;
        this.context=context;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(R.string.progress_dialogue_title);
        progressDialog.setMessage(context.getString(R.string.event_book_progress_content));

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        mCalendar = new Calendar.Builder(transport, jsonFactory, credential).build();
    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {

            mCalendar.events().delete(context.getString(R.string.event_primary_email), this.unbookEvent.getId()).execute();
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Webservice exception: "+e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        progressDialog.hide();
        if (isSuccess == null || !isSuccess) {
            eventUnbookImpl.onFail(context.getString(R.string.event_unbook_error_title), context.getString(R.string.event_unbook_error_content));
        }else if(isSuccess){
            eventUnbookImpl.onEventUnbookSuccess();
        }
    }

}
