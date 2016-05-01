package nz.net.pseudo.checkbook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.services.admin.directory.model.CalendarResources;

/**
 * Created by i on 25/04/16.
 */
public class ResourceListAdapter extends BaseAdapter {
    private CalendarResources calendarResources;

    public ResourceListAdapter(CalendarResources calendarResources) {
        this.calendarResources=calendarResources;
    }

    public CalendarResources getCalendarResources() {
        return calendarResources;
    }

    public void setCalendarResources(CalendarResources calendarResources) {
        this.calendarResources = calendarResources;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return calendarResources.getItems().size();
    }

    @Override
    public Object getItem(int position) {
        return calendarResources.getItems().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_list_item, parent, false);

        TextView resourceName = (TextView) convertView.findViewById(R.id.resource_name);
        resourceName.setText(calendarResources.getItems().get(position).getResourceName());

        ImageView resourceIcon = (ImageView) convertView.findViewById(R.id.resource_icon);

        if(calendarResources.getItems().get(position).getResourceType().equals("MeetingRoom")){
            resourceIcon.setImageResource(R.drawable.ic_people_black_24dp);
        }else if(calendarResources.getItems().get(position).getResourceType().equals("Resource")){
            resourceIcon.setImageResource(R.drawable.ic_videocam_black_24dp);
        }else{
            //default
            resourceIcon.setImageResource(R.drawable.ic_help_black_24dp);
        }


        return convertView;

    }
}
