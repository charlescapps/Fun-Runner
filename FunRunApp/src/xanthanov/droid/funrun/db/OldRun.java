//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun.db; 

import java.util.Date; 
import java.util.List; 

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
	
}
