package xanthanov.droid.funrun; 

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

}
