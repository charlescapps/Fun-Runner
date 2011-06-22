package xanthanov.droid.funrun; 

import java.util.List;
import java.util.ArrayList;

import com.google.android.maps.GeoPoint; 

public class GoogleStep {

	GeoPoint start, end; 
	int distanceMeters; 
	String distanceString; 
	String htmlInstructions; 
	boolean completed; 

	public GoogleStep(GeoPoint start, GeoPoint end, int distanceMeters, String distanceString, String html) {
		this.start = start; 
		this.end = end; 
		this.distanceMeters = distanceMeters; 
		this.distanceString = distanceString; 
		this.htmlInstructions = html; 
		this.completed = false; 
	}

	public GeoPoint getStart() {return start; }
	public GeoPoint getEnd() {return end; }
	public int getDistanceMeters() {return distanceMeters;}
	public String getDistanceString() {return distanceString;}
	public String getHtmlInstructions() {return htmlInstructions;}
	public boolean isComplete() {return completed;}
	public void completeStep() {completed = true; }

	public String toString() {
		return "Start:" + start + ",End:" + end + ",dist:" + distanceMeters + "distStr:"+distanceString; 
	}
}
