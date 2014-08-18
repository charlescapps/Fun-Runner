//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.gplace;

import android.graphics.Bitmap;
import com.google.android.gms.maps.model.LatLng;
import xanthanov.droid.xantools.DroidBitmapDownload;

import java.net.URL;

/**
*
*<h3>Class representing a place the user can run to--has data from Google Place search</h3>
*
* Stores the name of the place, the latitude/longitude, the icon's URL, and--optionally--the actual Bitmap icon from Google. <br/>
* The icon provided by Google is *rarely* not the default icon, so decided not to use this feature. <br/>
* The Bitmap is no longer being downloaded, so this field is unnecessary. Not sure if this app will use the icon in the future. 
*
*@author Charles L. Capps
*@version 0.9b
**/

public class GooglePlace implements java.io.Serializable {
	private String name; 
	private double[] latLng; 
	private URL iconUrl;  
	private Bitmap iconBmp; 

	public GooglePlace() {
		name = null; 
		latLng = null; 
		iconUrl =  null; 
	}

	public void downloadImage() {
		//Download the Bitmap
		iconBmp = DroidBitmapDownload.getBitmapFromURL(iconUrl); 
		iconBmp.setDensity(iconBmp.getDensity() * 2 ) ;
	}

/*	public GooglePlace(String name, GeoPoint geoPt, URL icon) {
		this.name = name; 
		this.geoPt = geoPt; 
		this.iconUrl = icon; 
		this.iconBmp = null; 
	}
*/

	public GooglePlace(String name, double latLng[], URL icon) {
		this.name = name; 
		//this.geoPt = null;
		this.latLng = latLng;  
		this.iconUrl = icon; 
		this.iconBmp = null; 
	}

	public float distanceTo(LatLng myLocation) {
		double startLat = myLocation.latitude;
		double startLng = myLocation.longitude;
 
		float result[] = new float[1]; 

		android.location.Location.distanceBetween(startLat, startLng, latLng[0], latLng[1], result); 

		return result[0]; 
	}

	public double[] getLatLngArray() {return latLng; }

	public float distanceTo(double[] myLocation) {
		float result[] = new float[1]; 

		android.location.Location.distanceBetween(myLocation[0], myLocation[1], latLng[0], latLng[1], result); 

		return result[0]; 
	}

	public void setIconBmp(Bitmap b) {
		this.iconBmp = b;
	}

	public String getName() {
		return name; 
	}

	public LatLng getLatLng() {
		return new LatLng(latLng[0], latLng[1]);
	}	

	public URL getIconUrl() {
		return iconUrl; 
	}

	public Bitmap getIconBmp() {
		return iconBmp; 
	}

	@Override
	public String toString() {
		String s = new String(); 

		s+="Name:'" + name + "',"; 
		s+="GeoPt:'" + latLng[0] + "," + latLng[1] + ","; 
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

		if (this.name.equals(other.name) && this.latLng[0] == other.latLng[0] && this.latLng[1] == other.latLng[1] && this.iconUrl.equals(other.iconUrl)) {
			return true; 
		}

		return false; 
	
	}

}
