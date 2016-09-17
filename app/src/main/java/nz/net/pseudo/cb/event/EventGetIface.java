package nz.net.pseudo.cb.event;


import com.google.api.services.calendar.model.Event;

/**
 * Created by i on 25/04/16.
 * Interface between main activity and asynctask
 */
public interface EventGetIface {
    void onEventGetSuccess(Event event);
    void onFail(String title, String content);
}
