package nz.net.pseudo.checkbook;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.admin.directory.model.CalendarResource;
import com.google.api.services.admin.directory.model.CalendarResources;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by i on 1/05/16.
 * Commaon App utilities and constants
 */
public class AppUtils {
    private static final String RESOURCES_KEY = "ResourcesKey";
    public static final String QUICKBOOK_TITLE_KEY = "quickBookTitleKey";
    public static final String QUICKBOOK_DURATION_KEY = "quickBookDurationKey";
    public static final String BEACON_STATE_KEY = "beaconStateKey";
    public static final String AUTO_REFRESH_STATE_KEY = "autoRefreshStateKey";
    public static final String AUTO_REFRESH_RATE_KEY = "autoRefreshDurationKey";
    public static final String BEACON_OVERRIDE_DURATION_KEY = "beaconOverrideDurationKey";
    public static final String BEACON_TRIGGER_DISTANCE_KEY = "beaconTriggerDistanceKey";
    public static final String BEACON_SCAN_RATE_KEY = "beaconScanRateKey";
    public static final String EVENT_EMAIL_INTENT_KEY = "eventEmailIntentKey";
    public static final String EVENT_EMAIL_CONTENT_KEY = "eventEmailContentKey";

    public static final int SEC_IN_MS = 1000;
    public static final int MIN_IN_MS = 60000;
    public static final int HOUR_IN_MS = 3600000;


    /**
     * @return Localised datetime formatter with device's timezone and preferred datetime format
     */
    public static SimpleDateFormat getLocalisedDateTimeFormatter(Context context){
        //Use system date time format
        SimpleDateFormat sdf = new SimpleDateFormat(getLocalisedDateFormat(context)+" "+getLocalisedTimeFormat(context));
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf;
    }

    public static SimpleDateFormat getLocalisedTimeFormatter(Context context){
        //Use system date time format
        SimpleDateFormat sdf = new SimpleDateFormat(getLocalisedTimeFormat(context));
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf;
    }

    public static String getLocalisedDateFormat(Context context){
        //Use system time format
        Format dateFormat = DateFormat.getDateFormat(context);
        return ((SimpleDateFormat) dateFormat).toLocalizedPattern();
    }

    public static String getLocalisedTimeFormat(Context context){
        //Use system time format
        Format timeFormat = DateFormat.getTimeFormat(context);
        return ((SimpleDateFormat) timeFormat).toLocalizedPattern();
    }

    public static DateTime roundFifteenMinutes(DateTime dateTime){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(dateTime.getValue()));

        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % 15;
        calendar.add(Calendar.MINUTE, mod < 8 ? -mod : (15-mod));

        return new DateTime(calendar.getTime());
    }

    public static void persistResources(SharedPreferences settings, CalendarResources resources){
        SharedPreferences.Editor settingsEditor = settings.edit();
        if(resources!=null) {
            settingsEditor.putString(RESOURCES_KEY, resources.toString());
        }else{
            settingsEditor.remove(RESOURCES_KEY);
        }
        settingsEditor.commit();
    }

    public static CalendarResources retrieveResources(SharedPreferences settings) throws IOException {
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        String resourcesJson = settings.getString(RESOURCES_KEY, null);
        CalendarResources resources = null;
        if(resourcesJson!=null) {
            JsonParser jsonParser = jsonFactory.createJsonParser(resourcesJson);
            resources = jsonParser.parse(CalendarResources.class);
        }
        return resources;
    }

    public static CalendarResource findResourceByEmail(CalendarResources resources, String email){
        for(CalendarResource resource : resources.getItems()){
            if(resource.getResourceEmail().equals(email)){
                return resource;
            }
        }
        return null;
    }

    public static CalendarResource findResourceById(CalendarResources resources, String id){
        for(CalendarResource resource : resources.getItems()){
            if(resource.getResourceId().equalsIgnoreCase(id)){
                return resource;
            }
        }
        return null;
    }

    public static String parseUrl(String inUrl){
        return inUrl
                .replace("http://www.","")
                .replace("https://www.","")
                .replace("http://","")
                .replace("https://","");
    }
}
