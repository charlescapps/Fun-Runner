package xanthanov.droid.funrun.mapsutils;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by charles.capps on 8/18/14.
 */
public class ZoomHelper {
    public static float getZoomLevel(final float fraction, final GoogleMap googleMap) {
        final float min = googleMap.getMinZoomLevel();
        final float max = googleMap.getMaxZoomLevel();
        return min + (max - min)*fraction;
    }
}
