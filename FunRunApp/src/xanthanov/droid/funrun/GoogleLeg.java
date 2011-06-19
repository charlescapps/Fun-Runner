package xanthanov.droid.funrun; 

import java.util.List;
import java.util.ArrayList;

import com.google.android.maps.GeoPoint; 

public class GoogleLeg {
	List<GoogleStep> steps; 
	String distanceString; 
	int distanceMeters; 

	public GoogleLeg(String distanceStr, int distMeters) {
		steps = new ArrayList<GoogleStep>(); 
		distanceString = distanceStr; 
		distanceMeters = distMeters; 
	}

	public void add(GoogleStep gs) {
		steps.add(gs); 
	}

	public int size() {
		return steps.size(); 
	}

	public GoogleStep get(int i) {
		return steps.get(i); 
	}

	public GeoPoint getFirstPoint() {
		return steps.get(0).getStart(); 
	}
	
	public GeoPoint getLastPoint() {
		return steps.get(steps.size()-1).getEnd(); 
	}

	@Override
	public String toString() {
		String s = "Total Distance:" + distanceMeters + "," + distanceString + "\n"; 

		for (int i = 0; i < steps.size(); i++) {
			s += "\tStep #" + i + ": " + steps.get(i).toString() + "\n"; 
		}
		return s;
	}
}
