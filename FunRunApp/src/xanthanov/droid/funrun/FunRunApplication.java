package xanthanov.droid.funrun; 

import xanthanov.droid.gplace.*;
import android.app.Application; 

public class FunRunApplication extends Application {

	private GoogleDirections runDirections; 
	private GoogleStep currentStep; 
	private boolean killRunActivity; 

	public static final long MIN_GPS_UPDATE_TIME_MS = 3000; //3 second update time

	public FunRunApplication() {
		super(); 
		runDirections = new GoogleDirections(); 
		killRunActivity = false; 
	}

	public void setRunDirections(GoogleDirections d) {runDirections = d;}
	
	public GoogleDirections getRunDirections() {return runDirections; }

	public GoogleStep getCurrentStep() {return currentStep;} 

	public void setCurrentStep(GoogleStep s) {currentStep=s;} 

	public void setKillRunActivity(boolean b) {
		killRunActivity = b; 
	}

	public boolean killRunActivity() {
		return killRunActivity;
	}
}
