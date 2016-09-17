package nz.net.pseudo.cb.event;


import com.google.api.services.calendar.model.Event;

/**
 * Created by i on 25/04/16.
 * Interface between main activity and asynctask
 */
public interface EventBookIface {
    void onEventBookSuccess(Event bookEvent,int position);
    void onFail(String title, String content);
}
