//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun.db; 

import xanthanov.droid.gplace.LatLng; 

import java.util.List; 
import java.util.ArrayList; 

/**
*
* <h3>This class contains exactly the data stored in the database for a leg. </h3>
*
* It differs from the GoogleLeg class, because it doesn't store the GoogleSteps or HTML instructions. We don't need this info permanently in the database. 
* There no "setters" because all fields are final. All data is obtained from the database or calculated in the contructor. 
* After that point, there's no reason to modify fields. 
*
*@see xanthanov.droid.gplace
*@see xanthanov.droid.gplace.GoogleLeg
*@author Charles L. Capps
*@version 0.9b
**/


public class OldLeg {

//Run path and directions path (for drawing directions on map)
private final List<LatLng> runPath; 
private final List<LatLng> polylinePath; 

//Start and end times in ms since 1970
private final long startTime;
private final long endTime; 

//Distance ran in meters. Calculated from runPath. Store to avoid calculating redundantly
private final double distanceRan; 
private final double avgSpeed; 

//Bounds for zooming to fit route on screen
private final LatLng neBound; 
private final LatLng swBound; 

//"Points" earned by player this leg. Can be calculated from time and distance. 
//But storing in DB anyway to ensure points don't change if I modify algorithm to score points or some such
private final int legPoints; 

//Place info
private final String placeName; 
private final LatLng placeLatLng; 
private final boolean gotToPlace; 

public OldLeg(List<LatLng> runPath, List<LatLng> polylinePath, long startTime, long endTime, double distanceRan, LatLng neBound, LatLng swBound, int legPoints, String placeName, LatLng placeLatLng, boolean gotToPlace) {

	this.runPath = runPath; 
	this.startTime = startTime; 
	this.endTime = endTime; 
	this.neBound = neBound; 
	this.swBound = swBound; 
	this.legPoints = legPoints; 
	this.placeName = placeName; 
	this.placeLatLng = placeLatLng; 
	this.gotToPlace = gotToPlace; 

	this.distanceRan = distanceRan; 

	//Calculate avgSpeed here
	avgSpeed = distanceRan / (endTime - startTime); 

	this.polylinePath = polylinePath; 

}

	public List<LatLng> getRunPath() {return runPath;}
	public List<LatLng> getPolylinePath() {return polylinePath; }

	public long getStartTime() {return startTime; }
	public long getEndTime() {return endTime; }
	public long getDuration() {return endTime - startTime; }
	public double getDistanceRan() {return distanceRan; }
	public double getAvgSpeed() {return avgSpeed; }
	
	public int getLegPoints() {return legPoints; }

	public LatLng getNeBound() {return neBound; }
	public LatLng getSwBound() {return swBound; }

	public String getPlaceName() {return placeName; }
	public LatLng getPlaceLatLng() {return placeLatLng; }
	public boolean gotToPlace() {return gotToPlace; }

	public LatLng getRunStart() {return runPath.get(0); }
	public LatLng getRunEnd() {
		int size = runPath.size(); 
		if (size <= 0 ) {
			return null; 
		}
		return runPath.get(size - 1); 
	}

}
