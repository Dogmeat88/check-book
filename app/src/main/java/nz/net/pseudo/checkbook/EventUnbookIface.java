package nz.net.pseudo.checkbook;


/**
 * Created by i on 25/04/16.
 * Interface between main activity and asynctask
 */
public interface EventUnbookIface {
    void onEventUnbookSuccess();
    void onFail(String title, String content);
}
