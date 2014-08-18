//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.gplace;

import com.google.android.gms.maps.model.LatLng;

import java.util.Comparator;

/**
*
*<h3>Comparator to sort places by distance from runner.</h3>
*
* Used by ChoosePlaceActivity to sort places by distance <i>before</i> getting Google directions. <br/>
* I think this was a good design decision since it reduces HTTP requests, but user can cycle through places by distance. <br />
* Of course, the &quot;as the crow flies&quot; distance will differ from the walking directions distance. 
* 
*
*@author Charles L. Capps
*@version 0.9b
**/

public class PlaceComparator implements Comparator<GooglePlace> {

	private LatLng basePoint;

	public PlaceComparator(LatLng basePoint) {
		this.basePoint = basePoint; 
	} 

	public int compare(GooglePlace p1, GooglePlace p2) {
		return (int) (p1.distanceTo(basePoint) - p2.distanceTo(basePoint));  
	}

	public boolean equals(Object o) {
		if (this==o) {
			return true; 
		}

		if (!PlaceComparator.class.isInstance(o) ) {
			return false; 
		}

		PlaceComparator pcObj = (PlaceComparator) o; 

		return pcObj.basePoint.equals(this.basePoint); 
	}

}
