package xanthanov.droid.gplace; 

public class LatLng implements java.io.Serializable {

	static final long serialVersionUID = -8786662330311932246L;

	public double lat; 
	public double lng; 

	public LatLng(double[] latLng) {this.lat = latLng[0]; this.lng = latLng[1]; }

	public double[] getArray() {return new double[] {lat, lng}; }

}
