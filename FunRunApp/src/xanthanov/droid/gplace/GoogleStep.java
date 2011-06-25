package xanthanov.droid.gplace; 

import java.util.List;
import java.util.ArrayList;

import com.google.android.maps.GeoPoint; 

public class GoogleStep {

	GeoPoint start, end; 
	int distanceMeters; 
	String distanceString; 
	String htmlInstructions; 
	boolean completed; 
	long startTime;
	long endTime; 

	public GoogleStep(GeoPoint start, GeoPoint end, int distanceMeters, String distanceString, String html) {
		this.start = start; 
		this.end = end; 
		this.distanceMeters = distanceMeters; 
		this.distanceString = distanceString; 
		this.htmlInstructions = html; 
		this.completed = false; 
		this.startTime = this.endTime = 0; 
	}

	public GeoPoint getStart() {return start; }
	public GeoPoint getEnd() {return end; }
	public int getDistanceMeters() {return distanceMeters;}
	public String getDistanceString() {return distanceString;}
	public String getHtmlInstructions() {return htmlInstructions;}
	public boolean isComplete() {return completed;}
	public void completeStep() {completed = true; }
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

		if (start.equals(s.start) && end.equals(s.end) && completed == s.completed && startTime == s.startTime && endTime == s.endTime) {
			return true; 
		}
		return false; 
		
	}

	public String toString() {
		return "Start:" + start + ",End:" + end + ",dist:" + distanceMeters + "distStr:"+distanceString; 
	}
}
