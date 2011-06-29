package xanthanov.droid.xantools; 

import android.location.LocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.content.Context; 

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;

public class DroidLoc {

	private LocationManager locManager; 
	private Context theContext; 

	public DroidLoc(Context c) {
		theContext = c; 
		locManager = (LocationManager)theContext.getSystemService(Context.LOCATION_SERVICE);
	} 

	public LocationManager getLocManager() {return locManager;}

    public GeoPoint getLastKnownLoc() {

		Location l = null;
		GeoPoint g = null; 

		try {
			l = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		catch (Exception e) {
			System.out.println("Failure getting last known location.");
			return null;
		}

		if (l!= null) {
			g = new GeoPoint((int) (l.getLatitude()*1E6), (int) (l.getLongitude()*1E6));
		}

		return g;
	}

	public static GeoPoint degreesToGeoPoint(double lat, double lng) {
		return new GeoPoint((int) (lat*1E6), (int) (lng*1E6));	
	}

	public static double[] geoPointToDegrees(GeoPoint g) {
		double[] latLng = new double[2]; 
		latLng[0] = ((double)g.getLatitudeE6())*1E-6;
		latLng[1] = ((double)g.getLongitudeE6())*1E-6;
		return latLng; 
	}

}
