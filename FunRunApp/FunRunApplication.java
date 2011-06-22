package xanthanov.droid.funrun; 

import android.app.Application; 

public class FunRunApplication extends Application {

	private GoogleDirections runDirections; 
	private GooglePlace runPlace; 

	public Application() {
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
