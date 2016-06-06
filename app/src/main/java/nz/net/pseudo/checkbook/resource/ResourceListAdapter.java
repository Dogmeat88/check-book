package nz.net.pseudo.checkbook.resource;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.services.admin.directory.model.CalendarResource;
import com.google.api.services.admin.directory.model.CalendarResources;
import com.google.api.services.calendar.model.FreeBusyCalendar;
import com.google.api.services.calendar.model.FreeBusyResponse;

import java.util.ArrayList;

import nz.net.pseudo.checkbook.R;

/**
 * Created by i on 25/04/16.
 * Custom list adapter for resources
 */
public class ResourceListAdapter extends BaseAdapter implements Filterable {
    private CalendarResources filteredCalendarResources;
    private final ResourceFilter resourceFilter;
    private Context context;
    FreeBusyResponse freeBusyResponse;

    public ResourceListAdapter(Context context, CalendarResources calendarResources) {
        this.context=context;
        this.filteredCalendarResources=new CalendarResources();
        this.filteredCalendarResources.setItems(new ArrayList<CalendarResource>());
        this.resourceFilter = new ResourceFilter(calendarResources, this);
    }

    public void setCalendarResources(CalendarResources calendarResources) {
        this.filteredCalendarResources = calendarResources;
        resourceFilter.setCalendarResources(calendarResources);
        notifyDataSetChanged();
    }

    public void setFilteredCalendarResources(CalendarResources filteredCalendarResources){
        this.filteredCalendarResources = filteredCalendarResources;
        notifyDataSetChanged();
    }

    public CalendarResources getFilteredCalendarResources() {
        return filteredCalendarResources;
    }

    public void setFreeBusyResponse(FreeBusyResponse freeBusyResponse) {
        this.freeBusyResponse = freeBusyResponse;
    }

    @Override
    public int getCount() {
        return filteredCalendarResources.getItems().size();
    }

    @Override
    public Object getItem(int position) {
        return filteredCalendarResources.getItems().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.resource_list_item, parent, false);

        TextView resourceName = (TextView) convertView.findViewById(R.id.resource_name);
        resourceName.setText(filteredCalendarResources.getItems().get(position).getResourceName());

        ImageView resourceIcon = (ImageView) convertView.findViewById(R.id.resource_icon);

        if(filteredCalendarResources.getItems().get(position).getResourceType().contains(context.getString(R.string.meeting_room_type_text))){
            resourceIcon.setImageResource(R.drawable.ic_people_black_24dp);
        }else{
            //default
            resourceIcon.setImageResource(R.drawable.ic_help_black_24dp);
        }

        if(freeBusyResponse != null){
            FreeBusyCalendar freeBusyCalendar = freeBusyResponse.getCalendars().get(filteredCalendarResources.getItems().get(position).getResourceEmail());
            if(freeBusyCalendar!=null && freeBusyCalendar.getBusy()!=null && freeBusyCalendar.getBusy().size()==0 && freeBusyCalendar.getErrors()==null){
                resourceIcon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.resource_availability_available), PorterDuff.Mode.SRC_ATOP));
            }else if(freeBusyCalendar!=null && freeBusyCalendar.getErrors()==null){
                resourceIcon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.resource_availability_unavailable), PorterDuff.Mode.SRC_ATOP));
            } else {
                resourceIcon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.resource_availability_unknown), PorterDuff.Mode.SRC_ATOP));
            }
        }else{
            resourceIcon.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.resource_availability_unknown), PorterDuff.Mode.SRC_ATOP));
        }

        return convertView;

    }

    @Override
    public Filter getFilter() {
        return this.resourceFilter;
    }
}
