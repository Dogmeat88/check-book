package nz.net.pseudo.checkbook;

import android.support.v7.widget.SearchView;
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

import java.util.ArrayList;

/**
 * Created by i on 25/04/16.
 */
public class ResourceListAdapter extends BaseAdapter implements SearchView.OnQueryTextListener, Filterable {
    private CalendarResources filteredCalendarResources;
    private ResourceFilter resourceFilter;

    public ResourceListAdapter(CalendarResources calendarResources) {
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

        if(filteredCalendarResources.getItems().get(position).getResourceType().equals("MeetingRoom")){
            resourceIcon.setImageResource(R.drawable.ic_people_black_24dp);
        }else if(filteredCalendarResources.getItems().get(position).getResourceType().equals("Resource")){
            resourceIcon.setImageResource(R.drawable.ic_videocam_black_24dp);
        }else{
            //default
            resourceIcon.setImageResource(R.drawable.ic_help_black_24dp);
        }

        return convertView;

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String filterText) {
        getFilter().filter(filterText);
        return true;
    }

    @Override
    public Filter getFilter() {
        return this.resourceFilter;
    }
}
