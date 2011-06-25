package xanthanov.droid.gplace; 

import java.util.Comparator;
import com.google.android.maps.GeoPoint;

public class PlaceComparator implements Comparator<GooglePlace> {

	private GeoPoint basePoint; 

	public PlaceComparator(GeoPoint basePoint) {
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
