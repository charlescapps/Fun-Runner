package xanthanov.droid.funrun; 

import xanthanov.droid.gplace.*;
import xanthanov.droid.funrun.persist.*; 
import android.app.Application; 
import android.os.Bundle; 
import android.content.Context; 

import java.io.File; 

public class FunRunApplication extends Application {

	//Stores the run currently in progress
	private GoogleDirections runDirections; 
	private GoogleStep currentStep; 
	private FunRunData state; 

	private File dataDir; 
	private String fullPath; 

	public static final long MIN_GPS_UPDATE_TIME_MS = 3000; //3 second update time
	public static final String DATA_DIR = "funrun_data";
	public static final String DATA_FILE = "MY_FUNRUN_DATA.ser";

	@Override
	public void onCreate() {
		super.onCreate(); 

		dataDir = getDir(DATA_DIR, Context.MODE_PRIVATE); 
		fullPath = dataDir + File.pathSeparator + DATA_FILE; 

		try {
			state = RunDataSerializer.getFunRunData(fullPath); 
		}
		catch (Exception e) {
			e.printStackTrace(); 
			state = null; 
		}
	}

	public FunRunApplication() {
		super(); 
		runDirections = new GoogleDirections(); 

	}

	public void setRunDirections(GoogleDirections d) {runDirections = d;}
	
	public GoogleDirections getRunDirections() {return runDirections; }

	public GoogleStep getCurrentStep() {return currentStep;} 

	public void setCurrentStep(GoogleStep s) {currentStep=s;} 

	//Called 
	public void addDirectionsToState(GoogleDirections gd) {
		state.add(gd); 
	}

}
