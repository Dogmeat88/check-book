package nz.net.pseudo.checkbook;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.CalendarResource;
import com.google.api.services.admin.directory.model.CalendarResources;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nz.net.pseudo.checkbook.databinding.ActivityMainBinding;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks
        , ResourceListIface, EventsListIface
        , SwipeRefreshLayout.OnRefreshListener
        , SearchView.OnQueryTextListener
        , EventBookIface
        , EventUnbookIface{
    private final String TAG = this.getClass().getSimpleName();

    private ActivityMainBinding mBinding;
    private GoogleAccountCredential credential;
    private ResourceListAdapter resourceListAdapter;
    private EventsListAdapter eventsListAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private CalendarResource selectedResource;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME_KEY = "accountNameKey";
    private static final String FRAG_SETTINGS_KEY = "fragSettingsKey";
    public static final String QUICKBOOK_TITLE_KEY = "quickBoolTitleKey";

    private static final String[] SCOPES = {
          DirectoryScopes.ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY
        , CalendarScopes.CALENDAR
        , CalendarScopes.CALENDAR_READONLY
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());

        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mBinding.navDrawerLayout,         /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description */
                R.string.navigation_drawer_close  /* "close drawer" description */
        );
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();
        mBinding.navDrawerLayout.addDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });


        CalendarResources calendarResources = new CalendarResources();
        calendarResources.setItems(new ArrayList<CalendarResource>());
        resourceListAdapter = new ResourceListAdapter(calendarResources);

        mBinding.resourceSearchview.setOnQueryTextListener(this);
        mBinding.resourceList.setAdapter(resourceListAdapter);
        mBinding.resourceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBinding.navDrawerLayout.closeDrawer(GravityCompat.START);
                selectedResource = ((CalendarResource)parent.getItemAtPosition(position));
                mBinding.setResource(selectedResource);
                onRefresh();
            }
        });

        Events events = new Events();
        events.setItems(new ArrayList<Event>());
        eventsListAdapter = new EventsListAdapter(this, events, credential);
        mBinding.resourceEventsList.setAdapter(eventsListAdapter);

        mBinding.resourceEventsRefreshLayout.setOnRefreshListener(this);

        mBinding.resourceEventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                final Event event = ((Event)parent.getItemAtPosition(position)).clone();

                if(event.getCreator()==null) {

                    final EditText edittext = (EditText) LayoutInflater.from(MainActivity.this).inflate(R.layout.event_book_confirm_edittext, null).findViewById(R.id.event_book_confirm_edittext);
                    edittext.setHint(getPreferences(Context.MODE_PRIVATE).getString(QUICKBOOK_TITLE_KEY, getString(R.string.event_book_confirm_edittext_hint)));

                    new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.event_book_confirm_title))
                        .setMessage(getString(R.string.event_book_confirm_content))
                        .setView(edittext)
                        .setCancelable(true)
                        .setPositiveButton(
                                getString(R.string.okay_btn),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Log.d(TAG, (edittext.getText().toString()));
                                        event.setSummary("".equals(edittext.getText().toString()) ? edittext.getHint().toString() : edittext.getText().toString());
                                        new EventBookAsyncTask(
                                                MainActivity.this
                                                , MainActivity.this.credential
                                                , MainActivity.this
                                                , MainActivity.this.selectedResource.getResourceEmail()
                                                , event
                                                , position
                                        ).execute();
                                    }
                                }
                        )
                        .setNegativeButton(
                                getString(R.string.cancel_btn),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                }
                    ).show();

                }else if(event.getCreator().getEmail().equals(credential.getSelectedAccountName())){
                    Log.d(TAG, "User's event");

                    if(event.getRecurringEventId()==null) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getString(R.string.event_unbook_confirm_title))
                                .setMessage(getString(R.string.event_unbook_confirm_content))
                                .setCancelable(true)
                                .setPositiveButton(
                                        getString(R.string.okay_btn),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                new EventUnbookAsyncTask(
                                                        MainActivity.this
                                                        , MainActivity.this.credential
                                                        , MainActivity.this
                                                        , event
                                                ).execute();
                                            }
                                        }
                                )
                                .setNegativeButton(
                                        getString(R.string.cancel_btn),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        }
                                ).show();
                    }else{
                        Toast.makeText(MainActivity.this, R.string.event_unbook_reoccurring_toast, Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });

        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if(item.getItemId()==R.id.action_settings){
            new SettingsDialogueFragment().show(getSupportFragmentManager(), FRAG_SETTINGS_KEY);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.nav_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void init() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Log.e(TAG,"No network connection available.");
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.connection_problem_title))
                    .setMessage(getString(R.string.connection_problem__content))
                    .setCancelable(true)
                    .setPositiveButton(
                            getString(R.string.retry_btn),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    init();
                                }
                            })
                    .setNegativeButton(
                            getString(R.string.quit_btn),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            }).show();
        } else {
            resourcesAsyncTask();
        }
    }

    @Override
    public void resourcesAsyncTask(){
        //Check resource cache first
        CalendarResources resources = null;
        try {
            resources = AppUtils.retrieveResources(getPreferences(Context.MODE_PRIVATE));
        } catch (IOException e) {
            Log.w(TAG, "Failed to parse resource json... fetching again");
        }
        if(resources==null) {
            new ResourceListAsyncTask(this, credential, this).execute();
        }else{
            loadResources(resources);
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME_KEY, null);
            if (accountName != null) {
                credential.setSelectedAccountName(accountName);
                init();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                   Log.e(TAG,
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    init();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                        editor.putString(PREF_ACCOUNT_NAME_KEY, accountName);
                        editor.apply();
                        credential.setSelectedAccountName(accountName);
                        init();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    init();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void onResourceListSuccess(CalendarResources resources) {
        AppUtils.persistResources(getPreferences(Context.MODE_PRIVATE), resources);
        loadResources(resources);
    }

    private void loadResources(CalendarResources resources){
        resourceListAdapter.setCalendarResources(resources);
        mBinding.resourceSearchview.setQuery(getPreferences(Context.MODE_PRIVATE).getString(getString(R.string.resource_filter_id),""),true);
    }

    @Override
    public void onEventsListSuccess(Events events) {
        Log.d(TAG, "Got events");
        EventsParser eventsParser = new EventsParser(this);
        eventsListAdapter.setEvents(eventsParser.parseEvents(events, selectedResource));
        mBinding.resourceEventsRefreshLayout.setRefreshing(false);
    }


    @Override
    public void onFail(String title, String content) {
        mBinding.resourceEventsRefreshLayout.setRefreshing(false);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setCancelable(true)
                .setNegativeButton(
                        getString(R.string.okay_btn),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();
    }

    @Override
    public void onRefresh() {
        if(selectedResource!=null) {
            mBinding.resourceEventsRefreshLayout.setRefreshing(true);
            new EventsListAsyncTask(
                    MainActivity.this
                    , credential
                    , MainActivity.this
                    , selectedResource.getResourceEmail()
            ).execute();

        }else{
           mBinding.resourceEventsRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        onQueryTextChange(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String filterText) {
        resourceListAdapter.getFilter().filter(filterText);
        getPreferences(Context.MODE_PRIVATE).edit().putString(getString(R.string.resource_filter_id), filterText).commit();
        return true;
    }

    @Override
    public void onEventBookSuccess(Event bookEvent,int position) {
        Log.d(TAG, "Book Success");
        eventsListAdapter.getEvents().getItems().set(position, bookEvent);
        eventsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onEventUnbookSuccess() {
        Log.d(TAG, "Unbook Success");
        onRefresh();
    }

}
