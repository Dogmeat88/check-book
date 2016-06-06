package nz.net.pseudo.checkbook.resource;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.admin.directory.model.CalendarResource;
import com.google.api.services.admin.directory.model.CalendarResources;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.FreeBusyRequest;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.FreeBusyResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nz.net.pseudo.checkbook.AppUtils;
import nz.net.pseudo.checkbook.R;

/**
 * Created by i on 24/04/16.
 * Get a list of events for a specified resource
 */
public class ResourceAvailabilityAsyncTask extends AsyncTask<Void, Void, FreeBusyResponse> {
    private final String TAG = this.getClass().getSimpleName();
    private final Calendar mCalendar;
    private final ResourceAvailabilityIface resourceAvailabilityImpl;
    private final CalendarResources calendarResources;
    private final Context context;

    public ResourceAvailabilityAsyncTask(Context context, GoogleAccountCredential credential, ResourceAvailabilityIface resourceAvailabilityImpl, CalendarResources calendarResources) {
        this.resourceAvailabilityImpl=resourceAvailabilityImpl;
        this.calendarResources=calendarResources;
        this.context=context;

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        mCalendar = new Calendar.Builder(transport, jsonFactory, credential).build();

    }

    @Override
    protected FreeBusyResponse doInBackground(Void... params) {
        try {
            List<FreeBusyRequestItem> requestItemList  = new ArrayList<>();
            for (CalendarResource calendarResource :calendarResources.getItems()) {
                FreeBusyRequestItem requestItem = new FreeBusyRequestItem();
                requestItem.setId(calendarResource.getResourceEmail());
                requestItemList.add(requestItem);
            }

            FreeBusyRequest freeBusyRequest = new FreeBusyRequest();
            freeBusyRequest.setItems(requestItemList);

            Date startDateLimit = new Date();

            Date endDateLimit = new Date();
            endDateLimit.setTime(endDateLimit.getTime()+AppUtils.FIFTEEN_MIN_IN_MS);

            freeBusyRequest.setTimeMin(new DateTime(startDateLimit));
            freeBusyRequest.setTimeMax(new DateTime(endDateLimit));

            return mCalendar.freebusy().query(freeBusyRequest).execute();


        } catch (Exception e) {
            Log.e(TAG, "Webservice exception: "+e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(FreeBusyResponse freeBusyResponse) {
        if (freeBusyResponse == null) {
            resourceAvailabilityImpl.onFail(this.context.getString(R.string.resource_availability_loading_error_title), this.context.getString(R.string.resource_availability_loading_error_content));
        } else {
            resourceAvailabilityImpl.onResourceAvailabilitySuccess(freeBusyResponse);
        }
    }

}
