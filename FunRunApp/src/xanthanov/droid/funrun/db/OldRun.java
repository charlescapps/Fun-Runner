//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun.db; 

import java.util.Date; 
import java.util.List; 
import java.util.ArrayList; 

/**
*<h3>Contains exactly the data stored in the database for an old run.</h3>
*
* No directions info is stored. Nothing about the individual GoogleStep's are stored. 
* This class is just a list of OldLeg objects plus the date of the run. 
*
*
*@author Charles L. Capps
*@version 0.9b
**/

public class OldRun  {

	private final Date runDate; 
	private final List<OldLeg> oldLegs; 

	public OldRun(Date runDate, List<OldLeg> oldLegs) {
		super(); 
		this.runDate = runDate; 
		this.oldLegs = oldLegs; 
	}

	public Date getRunDate() {return runDate; }
	public List<OldLeg> getOldLegs() {return oldLegs; }
	public OldLeg get(int i) {return oldLegs.get(i); }
	public int size() {return oldLegs.size(); }

	public long getTotalRunTime() {
		long time = 0; 
		for (int i = 0; i < oldLegs.size(); i++) {
			time += oldLegs.get(i).getDuration(); 
		}
		return time; 
	}

	public double getTotalRunDistance() {
		double dist = 0.0; 
		for (int i = 0; i < oldLegs.size(); i++) {
			dist+=oldLegs.get(i).getDistanceRan(); 
		}
		return dist; 
	}

	public List<String> getPlacesVisited() {
		List<String> places = new ArrayList<String>(); 
		for (int i =0; i < oldLegs.size(); i++) {
			places.add(oldLegs.get(i).getPlaceName()); 
		}
	
		return places; 
	}
	
}
