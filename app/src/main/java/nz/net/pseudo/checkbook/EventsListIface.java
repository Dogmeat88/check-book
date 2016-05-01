package nz.net.pseudo.checkbook;

import com.google.api.services.calendar.model.Events;

/**
 * Created by i on 25/04/16.
 */
public interface EventsListIface {
    void onSuccess(Events events);
}
