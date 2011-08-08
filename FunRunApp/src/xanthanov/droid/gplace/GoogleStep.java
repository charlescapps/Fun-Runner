//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.gplace; 

import java.util.List;
import java.util.ArrayList;

import xanthanov.droid.xantools.*; 

/**
*
*<h3>Class with data for a &apos;step&apos; of the Google directions from API query.</h3>
*
* In addition to data from HTTP request, stores start time and end time from user. <br/>
* <b>Note that the start time and end time aren't always set--the user might not finish a run, or might skip a step</b> <br/>
* Accordingly, any code written that relies on this data should check if the start / end time is equal to the default value of 0. 
* 
*@author Charles L. Capps
*@version 0.9b
**/

public class GoogleStep implements java.io.Serializable{
	
	static final long serialVersionUID = 5475289992693869917L;

	//Data from Google Directions API query
	private double[] startLatLng, endLatLng; 
	private int distanceMeters; 
	private String distanceString; 
	private String htmlInstructions; 

	//Start and end time
	private long startTime;
	private long endTime;

	public GoogleStep(double[] start, double[] end, int distanceMeters, String distanceString, String html) {
		this.startLatLng = start; 
		this.endLatLng = end; 
		this.distanceMeters = distanceMeters; 
		this.distanceString = distanceString; 
		this.htmlInstructions = html; 
		this.startTime = this.endTime = 0; 
	}

	public double[] getStartPoint() {return startLatLng; }
	public double[] getEndPoint() {return endLatLng; }
	public int getDistanceMeters() {return distanceMeters;}
	public String getDistanceString() {return distanceString;}
	public String getHtmlInstructions() {return htmlInstructions;}
	public long getStartTime() {return startTime;}
	public long getEndTime() {return endTime;}
	public void setStartTime(long st) {startTime = st;}
	public void setEndTime(long et) {endTime = et;}
	
	@Override
	public boolean equals(Object o) {
		if (this==o) {
			return true; 
		}
		if (!GoogleStep.class.isInstance(o)) {
			return false; 
		}
		GoogleStep s = (GoogleStep) o; 

		if (startLatLng[0] == s.startLatLng[0] && startLatLng[1] == s.startLatLng[1] && endLatLng[0] == s.endLatLng[0] && endLatLng[1] == s.endLatLng[1] && startTime == s.startTime && endTime == s.endTime) {
			return true; 
		}
		return false; 
		
	}

	public String toString() {
		return "Start:" + startLatLng[0] +"," + startLatLng[1] + ",End:" + endLatLng[0] + "," + endLatLng[1] + ",dist:" + distanceMeters + "distStr:"+distanceString; 
	}
}
