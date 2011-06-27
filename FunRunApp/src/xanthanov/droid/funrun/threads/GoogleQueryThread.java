package xanthanov.droid.funrun.threads; 

import xanthanov.droid.gplace.GoogleDirections; 
import xanthanov.droid.gplace.GooglePlace; 
import xanthanov.droid.gplace.DirectionGetter; 
import xanthanov.droid.funrun.PlaceSearcher;

import java.util.List; 

import android.app.ProgressDialog; 
import android.os.Handler;
import android.os.Message;

import com.google.android.maps.GeoPoint; 

public class GoogleQueryThread extends Thread {

	private GoogleDirections directionsResult = null; 
	private List<GooglePlace> placesResult = null; 

	private DirectionGetter directionsGetter = null; 
	private GeoPoint startPoint = null; 
	private GeoPoint endPoint = null; 

	private PlaceSearcher placeSearcher = null;
	private String search = null; 
	private GeoPoint currentLocation = null;
	private int radius;  

	private Exception anException = null; 

	private ProgressDialog pd = null; 

	private Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
					pd.dismiss();

			}
	};

	public GoogleQueryThread(DirectionGetter dg, GeoPoint startPoint, GeoPoint endPoint, ProgressDialog pd) { directionsGetter = dg; this.startPoint = startPoint; this.endPoint = endPoint; this.pd = pd; }
	public GoogleQueryThread(PlaceSearcher ps, String search, GeoPoint currentLocation, int radius, ProgressDialog pd) { placeSearcher = ps; this.search = search; this.currentLocation = currentLocation; this.radius = radius; this.pd = pd; }

	@Override
	public void run() {
		try {	
			if (directionsGetter != null) {
				directionsResult = directionsGetter.getDirections(startPoint, endPoint); 			
			}
			else {
				placesResult = placeSearcher.getNearbyPlaces(search, currentLocation, radius); 
			}
		}
		catch (Exception e) {
			anException = e; 
		}
		handler.sendEmptyMessage(0);
	}
 
	public List<GooglePlace> getPlacesResult() {return placesResult;}
	public GoogleDirections getDirectionsResult() {return directionsResult;}
	public Exception getException() {return anException; }
}
