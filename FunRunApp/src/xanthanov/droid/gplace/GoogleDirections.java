package xanthanov.droid.gplace;

import com.google.android.maps.GeoPoint; 

import java.util.List; 
import java.util.ArrayList; 
import java.util.Date; 
import java.text.DateFormat; 

public class GoogleDirections implements java.io.Serializable {

	static final long serialVersionUID = 86320041051816900L;

	private List<GoogleLeg> legs; 
	private Date dateOfRun; 

	public GoogleDirections() {
		legs = new ArrayList<GoogleLeg>();
		dateOfRun = new Date(); 
	}

	public List<GoogleLeg> getLegs() {return legs;}
	public void add(GoogleLeg l) {legs.add(l);}
	public int size() {return legs.size();}
	public GoogleLeg get(int i) {return legs.get(i);}
	public GoogleLeg lastLeg() {return (legs.size() > 0 ? legs.get(legs.size() - 1) : null);}
	public Date getDate() {return dateOfRun;}

	public long getEndTime() {return (lastLeg() == null ? -1 : lastLeg().getEndTime()); }
	public long getStartTime() {return (legs.get(0) == null ? -1 : legs.get(0).getStartTime()); }

	public void nullBitmapsForSerialization() {
		for (GoogleLeg leg: legs) {
			GooglePlace pl = leg.getLegDestination(); 
			pl.setIconBmp(null); 
		}
	}

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
		s += "\tStart time: " + getStartTime() + ", End time: " + getEndTime() + "\n";

 		for (int i = 0; i < legs.size(); i++) {
			s += "Leg #" + i + "\n" + legs.get(i).toString(); 	
		}

		return s; 
	}

	public double getDistanceSoFar() {
		double dist = 0.0; 
		for (GoogleLeg l: legs) {
			dist += l.getActualDistanceRan();  
		}
		return dist; 
	}

	public void remove(GoogleLeg gl) {
		legs.remove(gl); 
	}


	public long totalTime() {
		return (lastLeg() != null ? lastLeg().getEndTime() - legs.get(0).getStartTime() : 0); 
	}

	public double[] getFirstPoint() {
		return legs.get(0).getFirstPoint(); 
	}

	public double[] getLastPoint() {
		return legs.get(legs.size() - 1).getLastPoint(); 
	}
}
