package nz.net.pseudo.checkbook;

import android.content.Context;
import android.text.format.DateFormat;

import com.google.api.client.util.DateTime;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by i on 1/05/16.
 */
public class AppUtils {

    /**
     *
     * @param context
     * @return Localised datetime formatter with device's timezone and preferred datetime format
     */
    public static SimpleDateFormat getLocalisedDateTimeFormatter(Context context){
        //Use system date time format
        SimpleDateFormat sdf = new SimpleDateFormat(AppUtils.getLocalisedDateFormat(context)+" "+AppUtils.getLocalisedTimeFormat(context));
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
}
