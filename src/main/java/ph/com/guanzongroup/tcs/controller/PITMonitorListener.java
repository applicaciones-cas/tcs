package ph.com.guanzongroup.tcs.controller;

/**
 *
 * @author Maynard
 */
public interface PITMonitorListener {

    void onJobOrderChanged(int fnRowTag); // called on start / pause / resume / finish
    // action: "STARTED", "PAUSED", "RESUMED", "FINISHED"

    void onJobOrderChanged(String fsTransNox, String fsAction);
}
