//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun; 

import xanthanov.droid.gplace.*; 
import java.util.Random; 
import java.util.List; 
import android.content.Context; 
import com.google.android.maps.GeoPoint; 

/**
*<h3>Class to automatically get a random Google Place and Google walking directions for testing.</h3>
*
*Not much else to say about this class; it uses the DirectionGetter and PlaceSearcher classes. <br/>
*Needed for testing so I didn't have to go running every single time I tested something, before persistent data was working. 
*
*@author Charles L. Capps
*@version 0.9b
**/


public class MockFactory {

	private Context theContext; 
	private Random randGen; 

	private static final int MAX_DURATION = 360000; 

	public MockFactory(Context c) {this.theContext = c; randGen = new Random(System.currentTimeMillis());}

	public GoogleDirections getMockDirections(String[] placeSearches, GeoPoint loc, int radiusMeters) {

		PlaceSearcher placeSearcher = new PlaceSearcher(theContext.getResources());
		GoogleDirections zeDirections = new GoogleDirections(); 
		DirectionGetter myDirGetter = new DirectionGetter(); 

		for (int i = 0; i < placeSearches.length; i++) {
			List<GooglePlace> nearbyPs = null; 
			try {
				nearbyPs = placeSearcher.getNearbyPlaces(placeSearches[i], loc, radiusMeters);   
			}
			catch (Exception e) {
				e.printStackTrace(); 
				continue; //This one failed, just get the next one.
			}
			GooglePlace dest = nearbyPs.get(randGen.nextInt(nearbyPs.size())); //get random place from place search
			GoogleLeg newLeg = myDirGetter.getDirections(loc, dest.getGeoPoint());			
			if (newLeg == null) continue; 
			newLeg.setLegDestination(dest); 
			newLeg.setMaxStepCompleted(randGen.nextInt(newLeg.size())); 
			newLeg.setStartTime(System.currentTimeMillis()); 
			newLeg.setEndTime(randGen.nextInt(MAX_DURATION) + System.currentTimeMillis()); //Random time to complete leg of run
			zeDirections.add(newLeg); 
		}
		return zeDirections; 
	}

}
