package xanthanov.droid.gplace; 

import java.util.List;
import java.util.ArrayList;

import xanthanov.droid.xantools.*; 

import android.app.PendingIntent; 

import com.google.android.maps.GeoPoint; 

public class GoogleStep implements java.io.Serializable{

	private double[] startLatLng, endLatLng; 
	private int distanceMeters; 
	private String distanceString; 
	private String htmlInstructions; 
	private boolean completed; 
	private long startTime;
	private long endTime;
	private PendingIntent proximityIntent;  

	public GoogleStep(double[] start, double[] end, int distanceMeters, String distanceString, String html) {
		this.startLatLng = start; 
		this.endLatLng = end; 
		this.distanceMeters = distanceMeters; 
		this.distanceString = distanceString; 
		this.htmlInstructions = html; 
		this.completed = false; 
		this.startTime = this.endTime = 0; 
		this.proximityIntent = null; 
	}

	public GeoPoint getStartGeoPoint() {return DroidLoc.degreesToGeoPoint(startLatLng[0], startLatLng[1]); }
	public GeoPoint getEndGeoPoint() {return DroidLoc.degreesToGeoPoint(endLatLng[0], endLatLng[1]); }
	public double[] getStart() {return startLatLng; }
	public double[] getEnd() {return endLatLng; }
	public int getDistanceMeters() {return distanceMeters;}
	public String getDistanceString() {return distanceString;}
	public String getHtmlInstructions() {return htmlInstructions;}
	public boolean isComplete() {return completed;}
	public void completeStep() {completed = true; }
	public long getStartTime() {return startTime;}
	public long getEndTime() {return endTime;}
	public void setStartTime(long st) {startTime = st;}
	public void setEndTime(long et) {endTime = et;}
	public void setProximityIntent(PendingIntent i) {proximityIntent = i; }
	public PendingIntent getProximityIntent() {return proximityIntent; }
	
	@Override
	public boolean equals(Object o) {
		if (this==o) {
			return true; 
		}
		if (!GoogleStep.class.isInstance(o)) {
			return false; 
		}
		GoogleStep s = (GoogleStep) o; 

		if (startLatLng[0] == s.startLatLng[0] && startLatLng[1] == s.startLatLng[1] && endLatLng[0] == s.endLatLng[0] && endLatLng[1] == s.endLatLng[1] && completed == s.completed && startTime == s.startTime && endTime == s.endTime) {
			return true; 
		}
		return false; 
		
	}

	public String toString() {
		return "Start:" + startLatLng[0] +"," + startLatLng[1] + ",End:" + endLatLng[0] + "," + endLatLng[1] + ",dist:" + distanceMeters + "distStr:"+distanceString; 
	}
}
