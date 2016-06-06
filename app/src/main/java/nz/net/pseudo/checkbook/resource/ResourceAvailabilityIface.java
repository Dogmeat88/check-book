package nz.net.pseudo.checkbook.resource;

import com.google.api.services.calendar.model.FreeBusyResponse;

/**
 * Created by i on 25/04/16.
 * Interface between main activity and asynctask
 */
public interface ResourceAvailabilityIface {
    void onResourceAvailabilitySuccess(FreeBusyResponse freeBusyResponse);
    void onFail(String title, String content);
}
