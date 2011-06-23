package xanthanov.droid.funrun; 

import android.app.Application; 

public class FunRunApplication extends Application {

	private GoogleDirections runDirections; 
	private GooglePlace runPlace; 
	public static final long MIN_GPS_UPDATE_TIME_MS = 3000; //3 second update time

	public FunRunApplication() {
		super(); 
		runDirections = null; 
		runPlace = null; 
	}

	public void setRunDirections(GoogleDirections d) {
		runDirections = d; 
	}
	
	public GoogleDirections getRunDirections() {return runDirections; }

	public void setRunPlace(GooglePlace p) {
		runPlace = p; 
	}
	
	public GooglePlace getRunPlace() {return runPlace; }
}
