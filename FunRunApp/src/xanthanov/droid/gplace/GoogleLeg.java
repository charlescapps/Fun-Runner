package xanthanov.droid.gplace; 

import java.util.List;
import java.util.ArrayList;

import com.google.android.maps.GeoPoint; 

public class GoogleLeg {
	//DATA FROM GOOGLE MAPS HTTP QUERY
	private List<GoogleStep> steps; 
	private String distanceString; 
	private int distanceMeters; 
	private GeoPoint swBound;
	private GeoPoint neBound;  

	//CUSTOM DATA
	private int actualDistanceRan; 
	private long startTime; 
	private long endTime; 
	boolean doneRunning; 

	//ACTUAL PATH RAN
	List<GeoPoint> actualPath; 

	public GoogleLeg(String distanceStr, int distMeters) {
		steps = new ArrayList<GoogleStep>(); 
		distanceString = distanceStr; 
		distanceMeters = distMeters; 
		startTime = endTime = 0;
		actualDistanceRan = 0;  
		doneRunning = false; 
		swBound = neBound = null; 
		actualPath = new ArrayList<GeoPoint>(); 
	}

	public boolean isDone() {return doneRunning; }

	public void finishLeg() {doneRunning = true; }

	public void setNeBound(GeoPoint b0) {neBound = b0;}
	public void setSwBound(GeoPoint b1) {swBound = b1;}
	public GeoPoint getSwBound() {return swBound;}
	public GeoPoint getNeBound() {return neBound;}
	public void add(GoogleStep gs) {steps.add(gs); }
	public int size() {return steps.size(); }
	public GoogleStep get(int i) {return steps.get(i); }
	public GeoPoint getFirstPoint() {return steps.get(0).getStart(); }
	public GeoPoint getLastPoint() { return steps.get(steps.size()-1).getEnd(); }
	public List<GoogleStep> getSteps() {return steps; }
	public long getStartTime() { return startTime;}
	public long getEndTime() {return endTime;}
	public void setStartTime(long st) {startTime = st;}
	public void setEndTime(long et) {endTime = et;}
	public String getDistanceString() {return distanceString;}
	public int getDistanceMeters() {return distanceMeters; }
	public GoogleStep finalStep() {return steps.get(steps.size() - 1); }
	public GoogleStep firstStep() {return steps.get(0); }
	public List<GeoPoint> getActualPath() {return actualPath;}

	public void setActualDistanceToCompletedSteps() {
		int dist = 0; 

		for (GoogleStep s: steps) {
			if (s.isComplete()) {
				dist+= s.getDistanceMeters(); 
			}
		}
		actualDistanceRan = dist; 
	}

	public void setActualDistanceToWalkingDistance() {
		actualDistanceRan = distanceMeters;
	}

	public int getActualDistanceRan() {return actualDistanceRan;}

	@Override
	public String toString() {
		String s = "Total Distance:" + distanceMeters + "," + distanceString + "\n"; 

		for (int i = 0; i < steps.size(); i++) {
			s += "\tStep #" + i + ": " + steps.get(i).toString() + "\n"; 
		}
		return s;
	}
}
