package xanthanov.droid.gplace;

import com.google.android.maps.GeoPoint; 

import java.util.List; 
import java.util.ArrayList; 
import java.util.Date; 
import java.text.DateFormat; 

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
	public GoogleLeg lastLeg() {return (legs.size() > 0 ? legs.get(legs.size() - 1) : null);}
	public Date getDate() {return dateOfRun;}

	public List<GooglePlace> getPlacesVisited() {
		List<GooglePlace> places = new ArrayList<GooglePlace>(); 

		for (GoogleLeg l: legs) {
			places.add(l.getLegDestination()); 
		}

		return places; 
	}

	@Override
	public String toString() {
		String s = "(GoogleDirections) dateOfRun:" + DateFormat.getDateInstance().format(dateOfRun) + ",num legs:" + legs.size() + "\n"; 

 		for (int i = 0; i < legs.size(); i++) {
			s += "Leg #" + i + "\n" + legs.get(i).toString(); 	
		}

		return s; 
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
		return (lastLeg() != null ? lastLeg().getEndTime() - legs.get(0).getStartTime() : 0); 
	}

	public GeoPoint getFirstPoint() {
		return legs.get(0).getFirstPoint(); 
	}

	public GeoPoint getLastPoint() {
		return legs.get(legs.size() - 1).getLastPoint(); 
	}
}
