//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.gplace; 

/**
*
* <h3>Simple container class to hold a double longitude and a double for latitude</h3>
*
* Needed this class simply so we could have an ArrayList&lt;LatLng&gt;--it seemed a poor idea
* to use 2 ArrayList&apos;s of Doubles. 
*
* @author Charles L. Capps
* @version 0.9b
**/


public class LatLng implements java.io.Serializable {

	static final long serialVersionUID = -8786662330311932246L;

	public double lat; 
	public double lng; 

	public LatLng(double[] latLng) {this.lat = latLng[0]; this.lng = latLng[1]; }

	public double[] getArray() {return new double[] {lat, lng}; }

}
