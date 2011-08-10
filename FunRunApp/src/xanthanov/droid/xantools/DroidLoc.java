//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.xantools; 

import android.location.LocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.content.Context; 

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;

import xanthanov.droid.gplace.LatLng; 

/**
* 
* <h3>Convenience class for dealing with location providers</h3>
*
* 
* @version 0.9b
* @author Charles L. Capps
**/

public class DroidLoc {

	private LocationManager locManager; 
	private Context theContext; 
	private final static String MOCK_PROVIDER = "gps_mock_provider"; 
	private final static double PDX_LAT = 45.517413; 
	private final static double PDX_LNG = -122.677459; 

	public DroidLoc(Context c) {
		theContext = c; 
		locManager = (LocationManager)theContext.getSystemService(Context.LOCATION_SERVICE);
	} 

	public LocationManager getLocManager() {return locManager;}

	public static GeoPoint latLngToGeoPoint(LatLng ll) {
		return degreesToGeoPoint(ll.lat, ll.lng); 
	}

    public GeoPoint getLastKnownLoc() {

		Location l = null;
		GeoPoint g = null; 

		try {
			l = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		catch (Exception e) {
			System.out.println("Exception raised while getting last known location from GPS provider.");
			return null;
		}

		if (l!= null) {
			g = degreesToGeoPoint(l.getLatitude(), l.getLongitude()); 
		}
		else {
			System.out.println("Null location returned from GPS provider"); 
		}

		return g;
	}

	public static GeoPoint degreesToGeoPoint(double lat, double lng) {
		return new GeoPoint((int) (lat*1E6), (int) (lng*1E6));	
	}

	public static GeoPoint degreesToGeoPoint(double[] latLng) {
		return new GeoPoint((int) (latLng[0]*1E6), (int) (latLng[1]*1E6));	
	}

	public static double[] geoPointToDegrees(GeoPoint g) {
		double[] latLng = new double[2]; 
		latLng[0] = ((double)g.getLatitudeE6())*1E-6;
		latLng[1] = ((double)g.getLongitudeE6())*1E-6;
		return latLng; 
	}

	public static double[] latLngE6ToDegrees(int latE6, int lngE6) {
		double[] latLng = new double[2]; 
		latLng[0] = ((double)latE6)*1E-6;
		latLng[1] = ((double)lngE6)*1E-6;
		return latLng; 
	}

	public GeoPoint getMockLocation() {
		/*
		if (locManager.getProvider(MOCK_PROVIDER) == null) {
			locManager.addTestProvider (MOCK_PROVIDER, false, false, false, false, false, false, false, 0, 20); 
		}
		Location testLoc = new Location(MOCK_PROVIDER); 
		testLoc.setLatitude(PDX_LAT); 
		testLoc.setLongitude(PDX_LNG); 
		locManager.setTestProviderLocation(MOCK_PROVIDER, testLoc); 
		locManager.setTestProviderEnabled(MOCK_PROVIDER, true); 

		Location mockLoc = locManager.getLastKnownLocation(MOCK_PROVIDER); 	
		return degreesToGeoPoint(mockLoc.getLatitude(), mockLoc.getLongitude());
		*/
		Location testLoc = new Location(MOCK_PROVIDER); 
		testLoc.setLatitude(PDX_LAT); 
		testLoc.setLongitude(PDX_LNG); 
		return degreesToGeoPoint(testLoc.getLatitude(), testLoc.getLongitude());
	
	}

}
