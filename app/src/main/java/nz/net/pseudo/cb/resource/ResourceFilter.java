package nz.net.pseudo.cb.resource;

import android.widget.Filter;

import com.google.api.services.admin.directory.model.CalendarResource;
import com.google.api.services.admin.directory.model.CalendarResources;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by i on 5/05/16.
 * Filter the resources shown to the user
 */
public class ResourceFilter extends Filter {
    private CalendarResources calendarResources;
    private final ResourceListAdapter resourceListAdapter;

    public ResourceFilter(CalendarResources calendarResources, ResourceListAdapter resourceListAdapter) {
        this.calendarResources = calendarResources;
        this.resourceListAdapter = resourceListAdapter;
    }

    public void setCalendarResources(CalendarResources calendarResources) {
        this.calendarResources = calendarResources;
    }

    public CalendarResources getCalendarResources() {
        return calendarResources;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        List<CalendarResource> resultsList = new ArrayList<>();

        if("".equals(constraint.toString())){
            resultsList.addAll(this.calendarResources.getItems());
        }else {
            for (CalendarResource calendarResource : this.calendarResources.getItems()) {
                if (calendarResource.getResourceName().toUpperCase().contains(constraint.toString().toUpperCase())) {
                    resultsList.add(calendarResource);
                }
            }
        }

        FilterResults results = new FilterResults();
        results.values = resultsList;
        results.count = resultsList.size();

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        CalendarResources filteredCalendarResources = new CalendarResources();
        filteredCalendarResources.setItems((List<CalendarResource>) results.values);
        this.resourceListAdapter.setFilteredCalendarResources(filteredCalendarResources);
    }
}
