//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun; 

import xanthanov.droid.gplace.*;
import xanthanov.droid.funrun.persist.*; 
import android.app.Application; 
import android.os.Bundle; 
import android.content.Context;
import xanthanov.droid.xantools.DroidLoc; 
import com.google.android.maps.GeoPoint;

import android.speech.tts.TextToSpeech;  
import java.io.File; 

/**
* <h3>Application class to store global data.</h3> 
*   <h4>Responsibilities include: </h4>
*<ul>
*	<li>Ready the Text-to-speech engine when the app starts</li>
*	<li>Deserialize data from files when the app starts, i.e. load previous run stats.</li>
*	<li>Store the GoogleDirections, GoogleStep, and FunRunData objects since they are globally used by several activities. </li>
*	<li>currentStep is the step in the Google walking directions that the runner is currently on. Of course the app also allows for 
*skipping any number of steps. The FunRunActivity class has a proximity listener for the end of every step.</li>
*</ul>
*
* <h4>File structure</h4>
* <ul>
* <li>Data is in funrun_data subfolder. </li>
* <li>Each 'run' has a subfolder labeled by the date/time the run started. </li>
* <li>A run is represented by a GoogleDirections object. It stores some user data in addition to what's pulled from Google, e.g. time at which each leg completed in ms. </li>
* <li>(Perhaps would be a good idea to make a subclass to separate data from google vs. data from the app. Doesn't seem 100% necessary.) </li>
* <li>There's a 'base run' file in each run folder, plus a file for each GoogleLeg object. </li>
* <li>Decided on this structure, because a DB seemed like a convoluted solution for this data, but multiple files makes losing data less likely.</li>
* <li>This way, a GoogleLeg is written to SD card as soon as the user finishes that leg of the run. Then it's only read for viewing previous runs </li>
* </ul>
**/

public class FunRunApplication extends Application {

	//Stores the run currently in progress
	private GoogleDirections runDirections; 
	private GoogleStep currentStep; 
	private FunRunData state; 

	private boolean currentDirectionsAdded; 

	//Shared TTS object
	private TextToSpeech myTextToSpeech; 
	private TextToSpeech.OnInitListener ttsListener; 
	private boolean ttsReady; 

	private File dataDir; 
	private String fullPath; 

	public static final long MIN_GPS_UPDATE_TIME_MS = 2000; //1 second update time
	public static final String DATA_DIR = "funrun_data";

	@Override
	public void onCreate() {
		super.onCreate(); 

		currentDirectionsAdded = false; 

		dataDir = getDir(DATA_DIR, Context.MODE_PRIVATE); 
		RunDataSerializer.setDataDir(dataDir); 	

		try {
			state = RunDataSerializer.getFunRunData(); 
		}
		catch (Exception e) {
			System.err.println("Failure reading in old runs."); 
			e.printStackTrace(); 
			state = new FunRunData(); 
		}

		//Call new method to get FunRunData from a collection of files here

		/*
		try {
			state = RunDataSerializer.getFunRunData(fullPath); 
		}
		catch (Exception e) {
			System.err.println("No FunRunData file found, creating new FunRunData"); 
		}
		if (state == null || state.size() <= 0) {
			state = new FunRunData(); 
			//**************Test persistent data saving*********************

			addTestDirections(); 	
		}*/
	
		System.out.println("NUMBER OF RUNS FOUND: " + state.size()); 

		/*for (int i = 0; i < state.size(); i++) {
			System.out.println(state.get(i).toString()); 
		}*/

		//***************END TEST**********************************

		ttsReady = false; 
		myTextToSpeech = null; 

		//Callback for initializing the text-to-speech engine. 
		ttsListener = new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if (status == TextToSpeech.SUCCESS) {
					ttsReady = true; 
				}	
				else {
					ttsReady = false; 
				}
			}
		};

		myTextToSpeech = new TextToSpeech(this, ttsListener); 
		myTextToSpeech.setSpeechRate(0.75f); 
		myTextToSpeech.setPitch(1.25f); 

	}

	public void setCurrentDirectionsAdded(boolean val) {
		currentDirectionsAdded = val; 
	}

	public File getDataDir() {
		return dataDir; 
	}

	private void addTestDirections() {

		final int arbitraryRadius = 1000; 
		final int numMockRuns = 2;

		MockFactory mockery = new MockFactory(this); 
		String[] placeSearches = new String[] {"Cafe", "Smoothie Joint", "Shopping Mall"  }; 
		DroidLoc myDloc = new DroidLoc(this); 
		GeoPoint locPt = myDloc.getMockLocation(); 

		for (int i = 0; i < numMockRuns; i++) {
			GoogleDirections testDirs = mockery.getMockDirections(placeSearches, locPt, arbitraryRadius); 

			System.out.println("****************MOCK RUN BEFORE WRITING*******************"); 
			System.out.println(testDirs.toString()); 

			state.add(testDirs); 

		}
	}


	public TextToSpeech getTextToSpeech() {return myTextToSpeech; }

	public boolean isTtsReady() {return ttsReady; }

	public FunRunData getState() {return state; }

	public void setRunDirections(GoogleDirections d) {runDirections = d;}
	
	public GoogleDirections getRunDirections() {return runDirections; }

	public GoogleStep getCurrentStep() {return currentStep;} 

	public void setCurrentStep(GoogleStep s) {currentStep=s;} 

	public void addDirectionsToState() {
		if (!currentDirectionsAdded) {
			state.add(runDirections); 
		}
	}


}
