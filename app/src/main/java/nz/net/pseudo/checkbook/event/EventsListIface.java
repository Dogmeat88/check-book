package nz.net.pseudo.checkbook.event;

import com.google.api.services.calendar.model.Events;

/**
 * Created by i on 25/04/16.
 * Interface between main activity and asynctask
 */
public interface EventsListIface {
    void eventsAsyncTask();
    void onEventsListSuccess(Events events);
    void onFail(String title, String content);
    void autoRefreshInit();
}
