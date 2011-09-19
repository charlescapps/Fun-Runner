//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.gplace; 

import java.util.List;
import java.util.ArrayList;

import xanthanov.droid.xantools.DroidLoc; 
import android.content.Context; 

/**
*<h3>Class for one &apos;leg&apos; of the user's run, running to one place.</h3>
*
* <p>
* Holds a List of GoogleStep objects. Corresponds to the leg item returned in the JSON from the Google Directions API. 
* Stores the distance string returned by Google, the polyline for drawing a more accurate route, etc.
* <h3>Other things to note:</h3>
* <ul>
* <li>Holds the SW and NE bound as returned by Google; used to fit the route to the map view.</li>
* <li>Updates the actual distance ran by the user every time a point is added to the actual path ran.</li>
* <li>Stores all latitude/longitudes as doubles since the Google class GeoPoint is not serializable!</li>
* <li>Stores the destination of the leg as a GooglePlace</li>
* <li>Stores the start/end times, i.e. time the user started/finished running this leg in ms.</li>
* <li>A leg is saved to SD card when the FunRunActivity is destroyed, provided the user ran at least 200m </li>
* <li>TODO: May make this minimum distance a preference the user can tweak </li>
* <li>Stores the max step of the directions that the user completed </li>
* </ul>
* </p>
*
*@author Charles L. Capps
*@version 0.9b
* @see xanthanov.droid.gplace.GooglePlace
**/

public class GoogleLeg implements java.io.Serializable {

	static final long serialVersionUID = 8152718132267205755L;

	//DATA FROM GOOGLE MAPS HTTP QUERY
	private List<GoogleStep> steps; 
	private String distanceString; 
	private int distanceMeters; 
	private double[] swBound;
	private double[] neBound;  
	private String overviewPolyline; 

	//CUSTOM DATA
	private double actualDistanceRan; //Calculated by summing path segments of the actualPath
	private long startTime; 
	private long endTime; 
	private GooglePlace legDestination; 
	private int maxStepCompleted; 
	private final static double POINT_FACTOR = 50.0; 
	private final static double MAX_SPEED = 30.0; //If speed is more than 30 m/s clearly something was wrong with GPS. 
	private final static int PLACE_BONUS = 10; 

	//ACTUAL PATH RAN
	List<LatLng> actualPath; 

	//Directions path
	List<LatLng> directionsPath; 

	public GoogleLeg(String distanceStr, int distMeters) {
		steps = new ArrayList<GoogleStep>(); 
		distanceString = distanceStr; 
		distanceMeters = distMeters; 
		startTime = endTime = 0;
		actualDistanceRan = 0.0;  
		swBound = neBound = null; 
		overviewPolyline = null; 
		actualPath = new ArrayList<LatLng>(); 
		legDestination = null; 
		maxStepCompleted = -1; 
		directionsPath = null; 
	}

	public String getOverviewPolyline() {return overviewPolyline; }
	public void setOverviewPolyline(String line) {
		overviewPolyline = line; 
		directionsPath = decodePoly(overviewPolyline); 
	}

	public int getMaxStepCompleted() {return maxStepCompleted;}
	public void setMaxStepCompleted(int n) {maxStepCompleted = n;}

	//To do: make this method do something
	public int getLegPoints() {
		if (endTime <= startTime || startTime == 0 || endTime == 0) {
			return 0; 
		}
		double speed = actualDistanceRan / (endTime - startTime); 
		speed = Math.min(speed, MAX_SPEED); 

		double speedFactor = Math.max(speed*speed, 1.0); 
		int points = (int) (speedFactor*actualDistanceRan/POINT_FACTOR) + (gotToDestination() ? PLACE_BONUS : 0); 
		return points; 
	}

	public boolean gotToDestination() {return maxStepCompleted >= steps.size() - 1; }

	public GoogleStep lastStepDone() {return steps.get(maxStepCompleted); }

	//Method to decode the overview_polyline data from Google Directions API into a list of GeoPoint's
	//
	public List<LatLng> getPathPoints() {

		return directionsPath; 
	}

	public static List<LatLng> getPathPoints(String polyline) {
		return decodePoly(polyline); 
	}

	public void setNeBound(double[] latLng) {neBound = latLng;}
	public void setSwBound(double[] latLng) {swBound = latLng;}

	public double[] getSwBound() {return swBound;}
	public double[] getNeBound() {return neBound;}

	public void add(GoogleStep gs) {steps.add(gs); }
	public int size() {return steps.size(); }
	public GoogleStep get(int i) {return steps.get(i); }
	public double[] getFirstPoint() {return steps.get(0).getStartPoint(); }
	public double[] getLastPoint() { return steps.get(steps.size()-1).getEndPoint(); }
	public List<GoogleStep> getSteps() {return steps; }
	public long getStartTime() { return startTime;}
	public long getEndTime() {return endTime;}
	public void setStartTime(long st) {startTime = st;}
	public void setEndTime(long et) {endTime = et;}
	public String getDistanceString() {return distanceString;}
	public int getDistanceMeters() {return distanceMeters; }
	public GoogleStep finalStep() {return steps.get(steps.size() - 1); }
	public GoogleStep firstStep() {return steps.get(0); }
	public List<LatLng> getActualPath() {return actualPath;}
	public void setLegDestination(GooglePlace gp) {this.legDestination = gp; }
	public GooglePlace getLegDestination() {return legDestination; }

	public void addToActualPath(LatLng toAdd) {
		actualPath.add(toAdd);
		if (actualPath.size() <= 1) {
			return; 
		}

		//Add (distance between previous point -> new point) to the actualDistanceRan

		int size  = actualPath.size(); 
		double[] latLngStart = actualPath.get(size - 2).getArray(); 
		double[] latLngEnd = actualPath.get(size - 1).getArray(); 
		float[] tmpDist = new float[1]; 
		android.location.Location.distanceBetween(latLngStart[0], latLngStart[1], latLngEnd[0], latLngEnd[1], tmpDist); 
		actualDistanceRan += tmpDist[0]; 
	}

	public double getActualDistanceRan() {return actualDistanceRan;}

	@Override
	public String toString() {
		String s = "(GoogleLeg String) Google Distance:" + distanceMeters + "," + distanceString + "\n"; 
		s+="\tStart:" + startTime + ",End:" + endTime + ",numSteps:" + steps.size() + ",maxStepComplete:" + maxStepCompleted + "\n";

		for (int i = 0; i < steps.size(); i++) {
			s += "\tStep #" + i + ": " + steps.get(i).toString() + "\n"; 
		}
		return s;
	}

	@Override
	public boolean equals(Object o ) {
		if (this == o) {
			return true; 
		}

		if (!GoogleLeg.class.isInstance(o) ) {
			return false; 
		}

		return super.equals(o) ;
	}
	
//Got this from Jeffrey Sander's blog before I found the Google Documentation on this. 
//Received permission via email from Mr. Sanders to use this code in this app
//He is also mentioned in the README
	private static List<LatLng> decodePoly(String encoded) {

	  List<LatLng> poly = new ArrayList<LatLng>();
	  int index = 0, len = encoded.length();
	  int lat = 0, lng = 0;

	  while (index < len) {
		  int b, shift = 0, result = 0;
		  do {
			  b = encoded.charAt(index++) - 63;
			  result |= (b & 0x1f) << shift;
			  shift += 5;
		  } while (b >= 0x20);
		  int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
		  lat += dlat;

		  shift = 0;
		  result = 0;
		  do {
			  b = encoded.charAt(index++) - 63;
			  result |= (b & 0x1f) << shift;
			  shift += 5;
		  } while (b >= 0x20);
		  int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
		  lng += dlng;

		  int latE6 = (int) (((double) lat / 1E5) * 1E6);
		  int lngE6 = (int) (((double) lng / 1E5) * 1E6);
		  poly.add(new LatLng(DroidLoc.latLngE6ToDegrees(latE6, lngE6)));
	  }

	  return poly;
	}
}
