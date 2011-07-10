package xanthanov.droid.gplace; 

public class LatLng implements java.io.Serializable {

	public double lat; 
	public double lng; 

	public LatLng(double[] latLng) {this.lat = latLng[0]; this.lng = latLng[1]; }

	public double[] getArray() {return new double[] {lat, lng}; }

}
