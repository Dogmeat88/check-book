package nz.net.pseudo.checkbook;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by i on 25/04/16.
 */
public class EventsListAdapter extends BaseAdapter {
    private Context context;
    private Events events;
    SimpleDateFormat systemDateFormat;

    public EventsListAdapter(Context context, Events events) {
        this.context=context;
        this.events = events;
        systemDateFormat = AppUtils.getLocalisedDateTimeFormatter(context);
    }

    public Events getEvents() {
        return events;
    }

    public void setEvents(Events events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return events.getItems().size();
    }

    @Override
    public Object getItem(int position) {
        return events.getItems().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item, parent, false);

        Event eventItem = events.getItems().get(position);

        TextView eventSummary = (TextView) convertView.findViewById(R.id.event_summary);
        eventSummary.setText(eventItem.getSummary());

        TextView eventStartDate = (TextView) convertView.findViewById(R.id.event_start_date);
        DateTime startDateTime = eventItem.getStart().getDateTime();
        eventStartDate.setText(systemDateFormat.format(new Date(startDateTime.getValue())));

        TextView eventEndDate = (TextView) convertView.findViewById(R.id.event_end_date);
        DateTime endDateTime = eventItem.getEnd().getDateTime();
        eventEndDate.setText(systemDateFormat.format(new Date(endDateTime.getValue())));

        LinearLayout eventContainerLinearLayout = (LinearLayout)convertView.findViewById(R.id.event_container);

        if(eventItem.getCreator()==null) {
            eventContainerLinearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.quickBookEvent));
        }

        //Set the item height based on its duration
        Long itemDuration = (endDateTime.getValue()-startDateTime.getValue())/15000;
        //Scale based on phones DPI
        float scale = context.getResources().getDisplayMetrics().density;
        int itemHeightPixels = (int) (itemDuration * scale + 0.5f);
        eventContainerLinearLayout.getLayoutParams().height = itemHeightPixels;

        return convertView;
    }
}
