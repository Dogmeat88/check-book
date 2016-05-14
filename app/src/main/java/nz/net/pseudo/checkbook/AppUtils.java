package nz.net.pseudo.checkbook;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.admin.directory.model.CalendarResources;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by i on 1/05/16.
 */
public class AppUtils {
    public static final String RESOURCES_KEY = "ResourcesKey";

    /**
     *
     * @param context
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
}
