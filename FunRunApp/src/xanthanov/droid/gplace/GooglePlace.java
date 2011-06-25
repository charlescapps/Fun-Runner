package xanthanov.droid.gplace; 

import com.google.android.maps.GeoPoint; 
import java.net.URL; 

public class GooglePlace {
	private String name; 
	private GeoPoint geoPt;
	private URL iconUrl;  

	public GooglePlace() {
		name = null; 
		geoPt = null; 
		iconUrl =  null; 
	}

	public GooglePlace(String name, GeoPoint geoPt, URL icon) {
		this.name = name; 
		this.geoPt = geoPt; 
		this.iconUrl = icon; 
	}

	public float distanceTo(GeoPoint myLocation) {
		double startLat = myLocation.getLatitudeE6()*1E-6; 
		double startLng = myLocation.getLongitudeE6()*1E-6;
		double endLat = geoPt.getLatitudeE6()*1E-6; 
		double endLng = geoPt.getLongitudeE6()*1E-6; 
 
		float result[] = new float[1]; 

		android.location.Location.distanceBetween(startLat, startLng, endLat, endLng, result); 

		return result[0]; 
	}

	public String getName() {
		return name; 
	}

	public GeoPoint getGeoPoint() {
		return geoPt; 
	}	

	public URL getIconUrl() {
		return iconUrl; 
	}

	@Override
	public String toString() {
		String s = new String(); 

		s+="Name:'" + name + "',"; 
		s+="GeoPt:'" + geoPt + "',"; 
		s+="Icon:'" + iconUrl + "'"; 
		return s; 
	}
	
	@Override 
	public boolean equals(Object o) {
		if (this==o) {
			return true; 
		}
		if (!GooglePlace.class.isInstance(o)) {
			return false; 
		}
		GooglePlace other = (GooglePlace) o; 

		if (this.name.equals(other.name) && this.geoPt.equals(other.geoPt) && this.iconUrl.equals(other.iconUrl)) {
			return true; 
		}

		return false; 
	
	}

}
