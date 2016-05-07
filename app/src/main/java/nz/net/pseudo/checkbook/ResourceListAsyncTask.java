package nz.net.pseudo.checkbook;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.model.CalendarResources;

/**
 * Created by i on 24/04/16.
 * Get a list of resources(rooms etc)
 */
public class ResourceListAsyncTask extends AsyncTask<Void, Void, CalendarResources> {
    private final String TAG = this.getClass().getSimpleName();
    private Directory mService;
    private ResourceListIface resourceListImpl;
    private ProgressDialog progressDialog;
    private Context context;

    public ResourceListAsyncTask(Context context, GoogleAccountCredential credential, ResourceListIface resourceListImpl) {
        this.resourceListImpl=resourceListImpl;
        this.context=context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(R.string.progress_dialogue_title);
        progressDialog.setMessage(context.getString(R.string.resources_loading));


        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        mService = new Directory.Builder(transport, jsonFactory, credential).build();
    }

    @Override
    protected CalendarResources doInBackground(Void... params) {
        try {
            return mService.resources().calendars().list("my_customer").execute();
        } catch (Exception e) {
            Log.e(TAG, "Webservice exception: "+e.getMessage());
            return null;
        }
    }


    @Override
    protected void onPreExecute() {
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(CalendarResources calendarResources) {
        progressDialog.hide();
        if (calendarResources == null) {
            resourceListImpl.onFail(this.context.getString(R.string.resource_loading_error_title), this.context.getString(R.string.resource_loading_error_content));
        } else {
            resourceListImpl.onResourceListSuccess(calendarResources);
        }
    }

}
