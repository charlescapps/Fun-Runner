package xanthanov.droid.funrun;

import android.app.Activity;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

/**
 * Created by charlescapps on 8/27/14.
 */
public class AnimateMarkerThread extends Thread {
    private final Marker marker;
    private final List<BitmapDescriptor> bitmapDescriptors;
    private final long pause;
    private final Activity sourceActivity;
    private boolean doContinue = true;
    private int index = 0;

    public AnimateMarkerThread(Marker marker, List<BitmapDescriptor> bitmapDescriptors,
                               long pause, Activity sourceActivity) {
        this.marker = marker;
        this.bitmapDescriptors = bitmapDescriptors;
        this.pause = pause;
        this.sourceActivity = sourceActivity;
    }

    @Override
    public void run() {
        while (doContinue) {
            sourceActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    marker.setIcon(bitmapDescriptors.get(index));
                }
            });
            index = (index + 1) % bitmapDescriptors.size();
            try {
                Thread.sleep(pause);
            } catch (Exception e) {
                // zzz
            }
        }
    }

    public void interruptMe() {
        doContinue = false;
    }
}
