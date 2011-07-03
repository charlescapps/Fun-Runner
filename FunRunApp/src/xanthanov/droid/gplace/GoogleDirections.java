package xanthanov.droid.gplace;

import com.google.android.maps.GeoPoint; 

import java.util.List; 
import java.util.ArrayList; 
import java.util.Date; 

public class GoogleDirections implements java.io.Serializable {

	private List<GoogleLeg> legs; 
	private boolean completed; 
	private Date dateOfRun; 

	public GoogleDirections() {
		legs = new ArrayList<GoogleLeg>();
		completed = false; 
		dateOfRun = new Date(); 
	}

	public List<GoogleLeg> getLegs() {return legs;}
	public void add(GoogleLeg l) {legs.add(l);}
	public int size() {return legs.size();}
	public GoogleLeg get(int i) {return legs.get(i);}
	public boolean isCompleted() {return completed;}
	public void complete() {this.completed = true;} 
	public GoogleLeg lastLeg() {return legs.get(legs.size() - 1);}
	public Date getDate() {return dateOfRun;}

	public List<GooglePlace> getPlacesVisited() {
		List<GooglePlace> places = new ArrayList<GooglePlace>(); 

		for (GoogleLeg l: legs) {
			places.add(l.getLegDestination()); 
		}

		return places; 
	}

	public int getDistanceSoFar() {
		int dist = 0; 
		for (GoogleLeg l: legs) {
			dist += l.getDistanceSoFar();  
		}
		return dist; 
	}

	public void remove(GoogleLeg gl) {
		legs.remove(gl); 
	}


	public long totalTime() {
		return lastLeg().getEndTime() - legs.get(0).getStartTime(); 
	}

	public GeoPoint getFirstPoint() {
		return legs.get(0).getFirstPoint(); 
	}

	public GeoPoint getLastPoint() {
		return legs.get(legs.size() - 1).getLastPoint(); 
	}
}
