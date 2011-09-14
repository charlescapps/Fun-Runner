//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun.db; 

import xanthanov.droid.xantools.DroidUnits; 

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

	public long getTotalPoints() {
		long pts = 0; 
		for (int i =0; i < oldLegs.size(); i++) {
			pts+=oldLegs.get(i).getLegPoints(); 
		}
		return pts; 
	}

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

	public String toHtml() {
		StringBuffer html = new StringBuffer(); 
		double totalDistMeters = this.getTotalRunDistance(); 
		long totalTimeMs = this.getTotalRunTime(); 
		String totalDistStr = DroidUnits.getDistanceStringV3(totalDistMeters).toString();
		String totalTimeStr = DroidUnits.msToStrV2(totalTimeMs).toString(); 
		String avgSpeedStr = DroidUnits.getSpeedStringV2(totalTimeMs, totalDistMeters).toString(); 
			
		java.text.NumberFormat nf = java.text.NumberFormat.getInstance(); 

		html.append("<h1>Your stats!</h1>"); 
		html.append("<b>Points Earned :</b>&nbsp;" + nf.format(this.getTotalPoints()) + "<br/>"); 
		html.append("<b>Total Distance :</b>&nbsp;" + totalDistStr + "<br/>"); 
		html.append("<b>Total Time&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b>&nbsp;" + totalTimeStr + "<br/>"); 
		html.append("<b>Avg. Speed&nbsp;&nbsp;&nbsp;&nbsp; :</b>&nbsp;" + avgSpeedStr + "<br/><br/>");

		html.append("<h1>Places Visited</h1>"); 
		
		for (int i = 0; i < oldLegs.size(); i++) {
			OldLeg leg = oldLegs.get(i); 
			html.append("<u>" + leg.getPlaceName() + "<b>&nbsp;" + (leg.gotToPlace() ? "(Got there)" : "(Attempted)") + "</b></u><br/>"); 
			html.append("<b>Distance&nbsp;&nbsp;:</b>&nbsp;" + DroidUnits.getDistanceStringV3(leg.getDistanceRan()).toString() + "<br/>"); 
			html.append("<b>Time&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b>&nbsp;" + DroidUnits.msToStrV2(leg.getDuration()).toString() + "<br/>"); 
			html.append("<b>Avg speed :</b>&nbsp;" + DroidUnits.getSpeedStringV2(leg.getDuration(), leg.getDistanceRan()).toString() + "<br/><br/>"); 
		} 

		return html.toString(); 

	}
	
}
