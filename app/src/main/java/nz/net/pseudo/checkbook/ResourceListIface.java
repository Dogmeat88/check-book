package nz.net.pseudo.checkbook;

import com.google.api.services.admin.directory.model.CalendarResources;

/**
 * Created by i on 25/04/16.
 */
public interface ResourceListIface {
    void onSuccess(CalendarResources calendarResources);
}
