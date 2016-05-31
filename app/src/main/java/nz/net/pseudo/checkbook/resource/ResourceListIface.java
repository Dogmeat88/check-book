package nz.net.pseudo.checkbook.resource;

import com.google.api.services.admin.directory.model.CalendarResources;

/**
 * Created by i on 25/04/16.
 * Interface between main activity and asynctask
 */
public interface ResourceListIface {
    void resourcesAsyncTask();
    void onResourceListSuccess(CalendarResources resources);
    void onFail(String title, String content);
}
