package xanthanov.droid.funrun; 

import java.util.List;
import java.util.ArrayList;

import com.google.android.maps.GeoPoint; 

public class GoogleStep {

	GeoPoint start, end; 
	int distanceMeters; 
	String distanceString; 
	String htmlInstructions; 

	public GoogleStep(GeoPoint start, GeoPoint end, int distanceMeters, String distanceString, String html) {
		this.start = start; 
		this.end = end; 
		this.distanceMeters = distanceMeters; 
		this.distanceString = distanceString; 
		this.htmlInstructions = html; 
	}

	public GeoPoint getStart() {return start; }
	public GeoPoint getEnd() {return end; }
	public int getDistanceMeters() {return distanceMeters;}
	public String getDistanceString() {return distanceString;}
	public String getHtmlInstructions() {return htmlInstructions;}

	public String toString() {
		return "Start:" + start + ",End:" + end + ",dist:" + distanceMeters + "distStr:"+distanceString; 
	}
}
