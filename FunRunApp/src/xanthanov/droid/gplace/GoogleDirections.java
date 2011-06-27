package xanthanov.droid.gplace;

import com.google.android.maps.GeoPoint; 

import java.util.List; 
import java.util.ArrayList; 

public class GoogleDirections {

	private List<GoogleLeg> legs; 
	private boolean completed; 

	public GoogleDirections() {
		legs = new ArrayList<GoogleLeg>();
		completed = false; 
	}

	public List<GoogleLeg> getLegs() {return legs;}
	public void add(GoogleLeg l) {legs.add(l);}
	public int size() {return legs.size();}
	public GoogleLeg get(int i) {return legs.get(i);}
	public boolean isCompleted() {return completed;}
	public void complete() {this.completed = true;} 
	public GoogleLeg lastLeg() {return legs.get(legs.size() - 1);}



	public GeoPoint getFirstPoint() {
		return legs.get(0).getFirstPoint(); 
	}

	public GeoPoint getLastPoint() {
		return legs.get(legs.size() - 1).getLastPoint(); 
	}
}
