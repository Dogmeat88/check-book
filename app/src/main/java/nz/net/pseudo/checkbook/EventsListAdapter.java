package nz.net.pseudo.checkbook;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
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
    private SimpleDateFormat systemTimeFormat;
    private GoogleAccountCredential credential;

    public EventsListAdapter(Context context, Events events, GoogleAccountCredential credential) {
        this.context=context;
        this.events = events;
        this.systemTimeFormat = AppUtils.getLocalisedTimeFormatter(context);
        this.credential=credential;
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

        if(eventItem.getSummary()!=null) {
            TextView eventSummary = (TextView) convertView.findViewById(R.id.event_summary);
            eventSummary.setText(eventItem.getSummary());
        }

        if(eventItem.getCreator()!=null) {
            TextView eventCreator = (TextView) convertView.findViewById(R.id.event_creator);
            if(eventItem.getCreator().getDisplayName()!=null) {
                eventCreator.setText(eventItem.getCreator().getDisplayName());
            }else if(eventItem.getCreator().getEmail()!=null){
                eventCreator.setText(eventItem.getCreator().getEmail());
            }
        }

        TextView eventStartEndTime = (TextView) convertView.findViewById(R.id.event_start_end_time);
        DateTime startDateTime = eventItem.getStart().getDateTime();
        DateTime endDateTime = eventItem.getEnd().getDateTime();
        eventStartEndTime.setText(systemTimeFormat.format(new Date(startDateTime.getValue()))+"\n"+systemTimeFormat.format(new Date(endDateTime.getValue())));


        LinearLayout eventContainerLinearLayout = (LinearLayout)convertView.findViewById(R.id.event_container);

        if(eventItem.getCreator()==null) {
            //Probably Quickbook event
            eventContainerLinearLayout.setBackgroundResource(R.drawable.quickbook_event_item);
        }else if(eventItem.getCreator().getEmail().equals(credential.getSelectedAccountName())){
            //Users event
            eventContainerLinearLayout.setBackgroundResource(R.drawable.user_event_item);
        }else{
            //Other Users event
            eventContainerLinearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.othersEvent));
        }

        //Set the item height based on its duration
        Long itemDuration = (endDateTime.getValue()-startDateTime.getValue())/20000;
        //Scale based on phones DPI
        float scale = context.getResources().getDisplayMetrics().density;
        int itemHeightPixels = (int) (itemDuration * scale + 0.5f);
        eventContainerLinearLayout.getLayoutParams().height = itemHeightPixels;

        return convertView;
    }
}
