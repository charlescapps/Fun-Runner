package xanthanov.droid.funrun;

import com.google.android.maps.GeoPoint; 

import java.util.List; 
import java.util.ArrayList; 

public class GoogleDirections {

	private List<GoogleLeg> legs; 
	private GeoPoint swBound;
	private GeoPoint neBound;  
	private boolean completed; 

	public GoogleDirections() {
		legs = new ArrayList<GoogleLeg>();
		swBound = neBound = null; 
		completed = false; 
	}

	public List<GoogleLeg> getLegs() {return legs;}
	public void add(GoogleLeg l) {legs.add(l);}
	public void setNeBound(GeoPoint b0) {neBound = b0;}
	public void setSwBound(GeoPoint b1) {swBound = b1;}
	public int size() {return legs.size();}
	public GoogleLeg get(int i) {return legs.get(i);}
	public boolean isCompleted() {return completed;}
	public void complete() {this.completed = true;} 

	public GeoPoint getSwBound() {return swBound;}
	public GeoPoint getNeBound() {return neBound;}

	//Method to take a list of legs and get back a simple list of points to draw!
	public List<GeoPoint> getPathPoints() {
		assert (legs.size() == 1) : "Assert failed: more than 1 leg!";
		List<GeoPoint> points = new ArrayList<GeoPoint>(); 
		GoogleLeg currentLeg = null; 

		for (int i = 0; i < legs.size(); i++) {
			currentLeg = legs.get(i); 

			for (int j = 0; j < currentLeg.size(); j++) {
				if (i==0 && j==0) {//If it's the very first leg/step, must add start point
					points.add(currentLeg.get(j).getStart()); 
				}
				points.add(currentLeg.get(j).getEnd()); //Add end point of step
			} 
		}	
		return points; 
	}

	public GeoPoint getFirstPoint() {
		return legs.get(0).getFirstPoint(); 
	}

	public GeoPoint getLastPoint() {
		return legs.get(legs.size() - 1).getLastPoint(); 
	}
}
