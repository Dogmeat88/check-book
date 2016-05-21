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
import android.os.CountDownTimer;
import android.os.RemoteException;
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

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
        , EventUnbookIface
        , BeaconConsumer
        , RangeNotifier
        , BeaconIface
        {
    private final String TAG = this.getClass().getSimpleName();

    private ActivityMainBinding mBinding;
    private GoogleAccountCredential credential;
    private ResourceListAdapter resourceListAdapter;
    private EventsListAdapter eventsListAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private CalendarResource selectedResource;

    private BeaconManager beaconManager;
    private Beacon currentBeacon;
    private boolean beaconOverride;
    private CountDownTimer beaconOverrideCountdownTimer;


    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS_FINE_LOCATION = 1003;

    private static final String PREF_ACCOUNT_NAME_KEY = "accountNameKey";
    private static final String FRAG_SETTINGS_KEY = "fragSettingsKey";
    private static final String FRAG_ABOUt_KEY = "fragAboutKey";


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

        beaconOverride = false;

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

        CalendarResources calendarResources = new CalendarResources();
        calendarResources.setItems(new ArrayList<CalendarResource>());
        resourceListAdapter = new ResourceListAdapter(calendarResources);

        mBinding.resourceSearchview.setOnQueryTextListener(this);
        mBinding.resourceList.setAdapter(resourceListAdapter);
        mBinding.resourceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBinding.navDrawerLayout.closeDrawer(GravityCompat.START);
                suspendBeacon();

                selectResource(((CalendarResource)parent.getItemAtPosition(position)));
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
                    edittext.setHint(getPreferences(Context.MODE_PRIVATE).getString(AppUtils.QUICKBOOK_TITLE_KEY, getString(R.string.event_book_confirm_edittext_hint)));

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

    private void selectResource(CalendarResource resource){
        selectedResource = resource;
        mBinding.setResource(selectedResource);
        onRefresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options, menu);
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
        } else if(item.getItemId()==R.id.action_about){
            new AboutDialogueFragment().show(getSupportFragmentManager(), FRAG_ABOUt_KEY);
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

    private void initBeacons(){

        beaconManager = BeaconManager.getInstanceForApplication(this);

        beaconManager.setForegroundScanPeriod(AppUtils.BEACON_POLL_DURATION_MS);
        // Detect the main identifier (UID) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
        // Detect the telemetry (TLM) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"));
        // Detect the URL frame:
        //beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v"));

        beaconManager.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setBeaconEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(beaconManager!=null) {
            beaconManager.bind(this);
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
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS_FINE_LOCATION)
    private void chooseAccount() {

        String[] perms = {Manifest.permission.GET_ACCOUNTS, Manifest.permission.ACCESS_FINE_LOCATION};

        if (EasyPermissions.hasPermissions(this, perms)) {
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
                    "This app needs to access your Google account (via Contacts) and you Location",
                    REQUEST_PERMISSION_GET_ACCOUNTS_FINE_LOCATION,
                    perms);
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

        //Now that we have a resource list we can init the beacon functionality
        if(getPreferences(Context.MODE_PRIVATE).getBoolean(AppUtils.BEACON_STATE_KEY, false)) {
            initBeacons();
        }
    }

    @Override
    public void eventsAsyncTask() {
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
        eventsAsyncTask();
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

    @Override
    public void onBeaconServiceConnect() {

        Region region = new Region("all-beacons-region", null, null, null);

        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
       beaconManager.setRangeNotifier(this);

    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

        List<Beacon> beaconList = new ArrayList(beacons);
        //sort the beacons by distance from closest to farthest
        Collections.sort(beaconList ,new Comparator<Beacon>(){
            @Override
            public int compare(final Beacon lhs,Beacon rhs) {
                return Double.compare(lhs.getDistance(), rhs.getDistance());
            }
        });


        for (Beacon beacon: beaconList) {
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
                if(AppUtils.EUIDResourceMap.containsKey(beacon.getId2().toString())) {
                    this.currentBeacon = beacon;
                    break;
                }
            }
        }

        updateBeaconResource();

    }

    //Update the selected resource and associated events based on the detected beacon
    private void updateBeaconResource(){
        if(currentBeacon!=null && !beaconOverride) {
            String beaconEmail = AppUtils.EUIDResourceMap.get(currentBeacon.getId2().toString());
            if (selectedResource == null || !beaconEmail.equals(selectedResource.getResourceEmail())) {
                final CalendarResource beaconResource = AppUtils.findResourceByEmail(((ResourceFilter) resourceListAdapter.getFilter()).getCalendarResources(), beaconEmail);
                if (beaconResource != null) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            selectResource(beaconResource);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void setBeaconEnabled(boolean enable) {
        if(enable){
            initBeacons();
        }else{
            if(beaconManager!=null) {
                beaconManager.unbind(this);
            }
        }
    }

    private void suspendBeacon() {
        if(getPreferences(Context.MODE_PRIVATE).getBoolean(AppUtils.BEACON_STATE_KEY, false)) {
            beaconOverride = true;
            mBinding.beaconOverrideText.setVisibility(View.VISIBLE);

            if(beaconOverrideCountdownTimer!=null){
                beaconOverrideCountdownTimer.cancel();
            }

            beaconOverrideCountdownTimer = new CountDownTimer(getPreferences(Context.MODE_PRIVATE).getInt(AppUtils.BEACON_OVERRIDE_DURATION_KEY
                    , getResources().getInteger(R.integer.settings_beacon_override_duration_default)), 1000) {

                public void onTick(long millisUntilFinished) {
                    mBinding.beaconOverrideText.setText(getString(R.string.beacon_override_prefix) + " " + String.valueOf(millisUntilFinished / 1000));
                }

                public void onFinish() {
                    Log.v(TAG, "CountDownTimer.onFinish()");
                    beaconOverride = false;
                    mBinding.beaconOverrideText.setVisibility(View.INVISIBLE);
                }
            }.start();
        }
    }
}
