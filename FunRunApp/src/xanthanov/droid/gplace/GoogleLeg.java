package xanthanov.droid.gplace; 

import java.util.List;
import java.util.ArrayList;

import xanthanov.droid.xantools.DroidLoc; 
import android.content.Context; 

import com.google.android.maps.GeoPoint; 

public class GoogleLeg implements java.io.Serializable {
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
	private int maxStepUponGetDistance; //Used to check if the calculated 
	private boolean gotToDestination; 

	//ACTUAL PATH RAN
	List<LatLng> actualPath; 

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
	}

	public String getOverviewPolyline() {return overviewPolyline; }
	public void setOverviewPolyline(String line) {overviewPolyline = line; }

	public int getMaxStepCompleted() {return maxStepCompleted;}
	public void setMaxStepCompleted(int n) {maxStepCompleted = n;}

	public boolean gotToDestination() {return gotToDestination; }
	public void setGotToDestination(boolean b) {gotToDestination = b; }

	public GoogleStep lastStepDone() {return steps.get(maxStepCompleted); }


	//Method to decode the overview_polyline data from Google Directions API into a list of GeoPoint's
	//
	public List<LatLng> getPathPoints() {

		return decodePoly(overviewPolyline); 
	}

	public void setNeBound(double[] latLng) {neBound = latLng;}
	public void setSwBound(double[] latLng) {swBound = latLng;}

	public GeoPoint getSwBound() {return DroidLoc.degreesToGeoPoint(swBound[0], swBound[1]);}
	public GeoPoint getNeBound() {return DroidLoc.degreesToGeoPoint(neBound[0], neBound[1]);}

	public void add(GoogleStep gs) {steps.add(gs); }
	public int size() {return steps.size(); }
	public GoogleStep get(int i) {return steps.get(i); }
	public GeoPoint getFirstPoint() {return steps.get(0).getStartGeoPoint(); }
	public GeoPoint getLastPoint() { return steps.get(steps.size()-1).getEndGeoPoint(); }
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

		//Add distance from previous point to added point to the actualDistanceRan

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
	private List<LatLng> decodePoly(String encoded) {

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

		  GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6),
			   (int) (((double) lng / 1E5) * 1E6));
		  poly.add(new LatLng(DroidLoc.geoPointToDegrees(p)));
	  }

	  return poly;
	}
}
