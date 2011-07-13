package xanthanov.droid.funrun;

import xanthanov.droid.gplace.*;
import xanthanov.droid.xantools.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface; 
import android.content.IntentFilter; 

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation; 
import android.view.animation.AlphaAnimation;
import android.view.KeyEvent; 
import android.widget.TextView;
import android.widget.Button; 
import android.widget.LinearLayout;
import android.widget.Toast; 
import android.content.Context; 
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Intent;
import android.app.PendingIntent;
import android.text.Html; 
import android.text.Spanned; 
import android.media.AudioManager;

import android.speech.tts.TextToSpeech; 

import java.util.List;
import java.util.ArrayList;
import java.util.Random; 

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MyLocationOverlay; 
import com.google.android.maps.Overlay; 

/**
*	Copyright (c) 2011 Charles L. Capps
*
*	@author Xanthanov
*
**/


public class FunRunActivity extends MapActivity
{
	//***********VIEW OBJECTS DEFINED IN XML**********************
	private LinearLayout parentContainer; 
	private Button centerOnMeButton; 
	private MapView myMap;
	private Button zoomInButton;
	private Button zoomOutButton;
	private Button zoomToRouteButton;
	private TextView directionsTextView; 
	private TextView chosenPlaceTextView; 
	//*******************OTHER OBJECTS****************************
	private FunRunApplication funRunApp; 
	private MyLocationOverlay myLocOverlay;
	private MapController myMapController; 
	private LocationListener myLocListener; 
	private FunRunOverlay myFunRunOverlay; 
	private GeoPoint lastKnownLocation; 
	private GoogleDirections runDirections; 
	private GoogleLeg currentLeg; 
	private GoogleStep currentStep; 
	private GooglePlace runPlace;  
	private Spanned htmlInstructions; 
	private DroidLoc droidLoc; 

	private TextToSpeech myTts; 
	private DroidTTS ttsTools; 
	private AudioManager audioMan;  
	//*****************CONSTANTS**********************************
	private final static int DEFAULT_ZOOM = 15; 
	private final static int DEFAULT_RADIUS_METERS = 1000;
	public final static int MAX_RADIUS_METERS = 4000; 
	public final static int MIN_RADIUS_METERS = 50; 

	public final static float ACCEPT_RADIUS_METERS = 30.0f; 
	public final static float PATH_INCREMENT_METERS = 10.0f; 

	public final static String STEP_EXTRA = "step_no";
	//************************************************************
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.runlayout);

		audioMan = (AudioManager)getSystemService(Context.AUDIO_SERVICE); 
		//***************GET VIEWS DEFINED IN XML***********************
		myMap = (MapView) findViewById(R.id.run_myMap); 
		centerOnMeButton = (Button) findViewById(R.id.run_buttonCenterOnMe); 
		zoomToRouteButton = (Button) findViewById(R.id.run_buttonZoomToRoute); 
		zoomInButton = (Button) findViewById(R.id.run_buttonZoomIn); 
		zoomOutButton = (Button) findViewById(R.id.run_buttonZoomOut); 
		directionsTextView = (TextView) findViewById(R.id.directionsTextView); 
		chosenPlaceTextView = (TextView) findViewById(R.id.chosenPlaceTextView); 
		parentContainer = (LinearLayout) findViewById(R.id.run_parentContainer); 
		//******************DEFINE OTHER OBJECTS**************************
		droidLoc = new DroidLoc(this); 
		myLocOverlay = new MyLocationOverlay(this, myMap); 
		myMapController = myMap.getController(); 
		//Get the Application object and its global data
		funRunApp = (FunRunApplication) this.getApplicationContext(); 
		runDirections = funRunApp.getRunDirections(); 
		//Initialize currentStep to the first step in the last leg of the GoogleDirections object
		//As the runner arrives at destinations, new legs will be added
		currentLeg = runDirections.lastLeg(); 
		currentStep = currentLeg.get(0);  
		runPlace = currentLeg.getLegDestination();
		//Store current step in the Application object to pass between activities
		funRunApp.setCurrentStep(currentStep); 

		//Get the HTML directions from the raw string and set the text view
		htmlInstructions = Html.fromHtml(currentStep.getHtmlInstructions().trim());		
		Spanned txt = android.text.Html.fromHtml("Running to <b>" + runPlace.getName() + "</b>"); 		
		chosenPlaceTextView.setText(txt); 
		updateDirectionsTextView(); 

		//******************CALL SETUP METHODS****************************
		setupLocListener(); 
		setupMap(); 
		setupCenterOnMeButton(); 
		setupZoomToRouteButton(); 
		setupZoomButtons(); 

		long theTime = System.currentTimeMillis(); 

		//onCreate() is called when a new leg starts, so set the start time for the leg and the first step
		currentLeg.setStartTime(theTime); 
		currentLeg.get(0).setStartTime(theTime); 

		zoomToRoute(); 

		myTts = funRunApp.getTextToSpeech(); 
		ttsTools = new DroidTTS(); 
		
    }
	
	public boolean isRouteDisplayed() {
		return true;
	}

	@Override
	public boolean onKeyDown( int keycode, KeyEvent e) {		
		super.onKeyDown(keycode, e); 

		if (keycode == KeyEvent.KEYCODE_VOLUME_UP) {
			audioMan.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI); 
			if (!myTts.isSpeaking()) { //If a TTS isn't already playing, say the directions again. This avoids the annoying-as-hell possibility of spamming the TTS
				speakDirections(); 	
			}
		}
		else if (keycode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			audioMan.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE); 
			if (funRunApp.isTtsReady()) { //Stop anything playing when you press volume down
				myTts.playSilence(100, TextToSpeech.QUEUE_FLUSH, null); 
			}
		}

		return true; 
	}

	@Override 
	protected void onStart() {
		super.onStart();
		//See if current step was updated by StepCompleteActivity
		currentStep = ((FunRunApplication)getApplication()).getCurrentStep(); 
		if (currentStep != null) {
			htmlInstructions = Html.fromHtml(currentStep.getHtmlInstructions().trim());		
			updateDirectionsTextView(); 

			if (funRunApp.isTtsReady()) {
				System.out.println("Html instructions string: " + htmlInstructions.toString()); 
				String fullString = ttsTools.expandDirectionsString(htmlInstructions.toString()); 
				myTts.speak(fullString, TextToSpeech.QUEUE_FLUSH, null); 
				myTts.playSilence(1000, TextToSpeech.QUEUE_ADD, null); 
				myTts.speak("Press volume up to hear directions again.", TextToSpeech.QUEUE_ADD, null); 
			}
			else {
				System.err.println("TTS NOT READY! OWNED!"); 
			}

			//Start up compass and location updates
			myLocOverlay.enableCompass(); 	
			droidLoc.getLocManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, FunRunApplication.MIN_GPS_UPDATE_TIME_MS, 0, myLocListener);

			//Force update of lastKnownLocation
			lastKnownLocation = droidLoc.getLastKnownLoc(); 

			//Update visuals so it doesn't show you in the wrong place
			myFunRunOverlay.updateCurrentLocation(lastKnownLocation); 
			myMap.postInvalidate(); 
		}
		else {
			//Go choose another place	
			finish(); 
		}
	}

	private void speakDirections() {

		if (currentStep != null) {
			htmlInstructions = Html.fromHtml(currentStep.getHtmlInstructions().trim());		

			if (funRunApp.isTtsReady()) {
				System.out.println("Html instructions string: " + htmlInstructions.toString()); 
				String fullString = ttsTools.expandDirectionsString(htmlInstructions.toString()); 
				myTts.speak(fullString, TextToSpeech.QUEUE_FLUSH, null); 
			}
		}
	}
	
	@Override 
	protected void onStop() {
		super.onStop(); 
		droidLoc.getLocManager().removeUpdates(myLocListener); 
		myLocOverlay.disableCompass();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy(); 

		//Remove this leg if the runner didn't go any distance. 
		if (currentLeg.getActualDistanceRan() <= 0.0 || currentLeg.getMaxStepCompleted() < 0) {
			runDirections.remove(currentLeg); 
			(Toast.makeText(this, "No steps complete. Progress not saved.", 5)).show(); 
			
		}
		else {
			(Toast.makeText(this, "Progress saved", 5)).show(); 
			funRunApp.addDirectionsToState(); 
			funRunApp.writeState(); 
		}
	}

	private void updateDirectionsTextView() {
		directionsTextView.setText(htmlInstructions); 	
	}

	private void setupLocListener() {
		myLocListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location l) {
				updateLocation(l); 
			}
			public void onStatusChanged(String provider, int status, Bundle extras) {}

    		public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}		
		};

		droidLoc.getLocManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocListener);
	}

	private void updateLocation(Location l) {
		if (l==null) { //not sure if this locListener will ever return null here. May as well check
			return;
		}
		//Update the local variable with the last known location
		lastKnownLocation = DroidLoc.degreesToGeoPoint(l.getLatitude(), l.getLongitude()); 
		double[] latLng = new double[] {l.getLatitude(), l.getLongitude()}; 

		//Check if we've finished a step
		checkForCompleteSteps(); 
		
		//Update the overlay so it draws properly
		myFunRunOverlay.updateCurrentLocation(lastKnownLocation); 
		myMap.postInvalidate(); 

		//Just for fun! Animate the title bar.
		//As a side effect, this indicates whether GPS is working or not (^o ^o)
		animateTitleBar(); 

		//Add a GeoPoint to the actualPath in the currentLeg, provided the previous point is far enough away from the current point. 
		//This obviously is intended to prevent 
		addToActualPath(latLng); 
	}

	private void checkForCompleteSteps() {
		float distance[] = new float[1]; 
		GoogleStep step = null;
		double latLng[] = DroidLoc.geoPointToDegrees(lastKnownLocation); 
		for (int i = currentLeg.getMaxStepCompleted() + 1; i < currentLeg.size(); i++) {
			step = currentLeg.get(i); 
			Location.distanceBetween(step.getEnd()[0], step.getEnd()[1], latLng[0], latLng[1], distance); 
			if (distance[0] <= ACCEPT_RADIUS_METERS) {
				currentLeg.setMaxStepCompleted(i); 
				long time = System.currentTimeMillis(); 
				currentLeg.setEndTime(time); 
				currentLeg.get(i).setEndTime(time); 
				Intent completeStepIntent = new Intent(this, StepCompleteActivity.class); 
				completeStepIntent.putExtra(STEP_EXTRA, i); 
				startActivity(completeStepIntent);
				break;  
			} 
		}
	} 

	private void addToActualPath(double[] latLng) {
		List<LatLng> actualPath = currentLeg.getActualPath();
		int size = actualPath.size(); 
		if (size == 0) {
			actualPath.add(new LatLng(latLng)); 
			return; 
		}
		LatLng lastPathPoint = actualPath.get(size - 1); 
		float[] distance = new float[1]; 

		android.location.Location.distanceBetween(lastPathPoint.lat, lastPathPoint.lng, latLng[0], latLng[1], distance);

		if (distance[0] >= PATH_INCREMENT_METERS) {
			currentLeg.addToActualPath(new LatLng(latLng)); 
		}
	}

	private void animateTitleBar() {
		String txt = (String)(this.getTitle());  
		int len = txt.length(); 
		char lastChar = txt.charAt(len - 1); 
		txt = (String.valueOf(lastChar) + txt).substring(0, len); 

		setTitle(txt); 
		
	}

	private void setupMap() {
		myFunRunOverlay = new FunRunOverlay(myMap, null, true, false);
		myFunRunOverlay.updateCurrentDirections(runDirections); 
		myMap.getOverlays().add(myLocOverlay); 
		myMap.getOverlays().add(myFunRunOverlay); 
		
		myMap.postInvalidate(); 
	}

	private void setupCenterOnMeButton() {
		final MapController mc = myMapController; 
		final GeoPoint loc = lastKnownLocation; 

		Animation animation = new AlphaAnimation(1.0f, 0.7f);
		animation.setFillAfter(true);
		centerOnMeButton.startAnimation(animation);

		centerOnMeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					centerOnMe(); 
				}
			});	
	}

	private void centerOnMe() {
		if (lastKnownLocation == null) {
			return; 
		}
		else {
			myMapController.animateTo(lastKnownLocation); 
		}
		
	}

	private void setupZoomButtons() {
		Animation animation = new AlphaAnimation(1.0f, 0.7f);
		animation.setFillAfter(true);
		zoomInButton.startAnimation(animation);
		zoomOutButton.startAnimation(animation);

		zoomInButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					myMapController.zoomIn(); 
				}
			});	
		zoomOutButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					myMapController.zoomOut(); 
				}
			});	
	}

	private void setupZoomToRouteButton() {

		Animation animation = new AlphaAnimation(1.0f, 0.7f);
		animation.setFillAfter(true);
		zoomToRouteButton.startAnimation(animation);

		zoomToRouteButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					zoomToRoute(); 
				}
			});	
	} 

	private void zoomToRoute() {
		final GeoPoint neBound = currentLeg.getNeBound(); 
		final GeoPoint swBound = currentLeg.getSwBound(); 
		final GeoPoint midPoint = new GeoPoint( (neBound.getLatitudeE6() + swBound.getLatitudeE6())/2, (neBound.getLongitudeE6() + swBound.getLongitudeE6())/2);
		final int latSpan = Math.abs(neBound.getLatitudeE6() - swBound.getLatitudeE6()); 
		final int lngSpan = Math.abs(neBound.getLongitudeE6() - swBound.getLongitudeE6()); 

		myMapController.animateTo(midPoint); 
		myMapController.zoomToSpan(latSpan, lngSpan); 
	}

}

