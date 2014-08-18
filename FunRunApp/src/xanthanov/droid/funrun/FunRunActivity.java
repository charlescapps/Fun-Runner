//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.*;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import xanthanov.droid.gplace.GoogleDirections;
import xanthanov.droid.gplace.GoogleLeg;
import xanthanov.droid.gplace.GooglePlace;
import xanthanov.droid.gplace.GoogleStep;
import xanthanov.droid.xantools.DroidDialogs;
import xanthanov.droid.xantools.DroidLoc;
import xanthanov.droid.xantools.DroidTTS;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
* <h3>Activity for when user is actually running.</h3>
*
*<h3>Things this class does:</h3>
*<ul>
*<li>Displays route the user runs as they run.</li>
*<li>Speaks walking directions. User can press &quot;volume up&quot; button to repeat them. (This fact is spoken aloud)</li>
*<li>Adds a point to this route every PATH_INCREMENT_METERS meters (public static constant, see below).</li>
*<li>Detects when the user is within ACCEPT_RADIUS_METERS meters of the end of a step for the Google directions.</li>
*<li>Passes index of completed step to the StepCompleteActivity using Intent &quot;extras&quot;</li>
*<li>TODO: Tweak accept radius. Need balance between accuracy, and guaranteeing it's actually accepted even if GPS is inaccurate.</li>
*<li>Also TODO: Make a preferences screen allowing the user to choose the accept radius, path increment</li>
*<li>Maybe try to integrate wifi, but from my experience it's way too inaccurate for this.</li>
*</ul>
*
*@author Charles L. Capps
*@version 0.9b
*@see xanthanov.droid.xantools.DroidTTS
*
**/

public class FunRunActivity extends Activity
{
	//***********VIEW OBJECTS DEFINED IN XML**********************
	private LinearLayout parentContainer; 
	private Button centerOnMeButton; 
	private MapView myMap;
    private GoogleMap googleMap;
	private Button zoomInButton;
	private Button zoomOutButton;
	private Button zoomToRouteButton;
	private TextView directionsTextView; 
	private TextView chosenPlaceTextView; 
	private TextView copyrightTextView; 
	private RelativeLayout mapRelLayout; 
	//*******************OTHER OBJECTS****************************
	private FunRunApplication funRunApp; 
	private LocationListener myGpsListener;
	private LocationListener myNetworkListener; 
	private FunRunOverlay myFunRunOverlay; 

	private LatLng lastKnownLatLng;
	private Location bestLocation; 

	private GoogleDirections runDirections; 
	private GoogleLeg currentLeg; 
	private GoogleStep currentStep; 
	private GooglePlace runPlace;  
	private DroidLoc droidLoc; 

	private TextToSpeech myTts; 
	private DroidTTS ttsTools; 
	private AudioManager audioMan;  

	private Spanned htmlInstructions;
	private String speakingInstructions;  
	private boolean firstSpeechCompleted; 
	private boolean SPEAK_DIRECTIONS; 
	//*****************CONSTANTS**********************************
	private final static int DEFAULT_ZOOM = 15; 
	private final static int DEFAULT_RADIUS_METERS = 1000;
	public final static int MAX_RADIUS_METERS = 4000; 
	public final static int MIN_RADIUS_METERS = 50; 

	private float ACCEPT_RADIUS_METERS; 
	private float PATH_INCREMENT_METERS = 10.0f; 

	private float MIN_DISTANCE_TO_SAVE; 

	public final static String STEP_EXTRA = "step_no";
	//************************************************************
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.runlayout);
		//System.
		//Get the Application object and its global data
		funRunApp = (FunRunApplication) this.getApplicationContext(); 

		//Get audio manager
		audioMan = (AudioManager)getSystemService(Context.AUDIO_SERVICE); 
		//***************GET VIEWS DEFINED IN XML***********************
		myMap = (MapView) findViewById(R.id.run_myMap);
        myMap.onCreate(savedInstanceState);
        centerOnMeButton = (Button) findViewById(R.id.run_buttonCenterOnMe);
		zoomToRouteButton = (Button) findViewById(R.id.run_buttonZoomToRoute); 
		zoomInButton = (Button) findViewById(R.id.run_buttonZoomIn); 
		zoomOutButton = (Button) findViewById(R.id.run_buttonZoomOut); 
		directionsTextView = (TextView) findViewById(R.id.directionsTextView); 
		chosenPlaceTextView = (TextView) findViewById(R.id.chosenPlaceTextView); 
		copyrightTextView = (TextView) findViewById(R.id.copyrightTextView); 
		parentContainer = (LinearLayout) findViewById(R.id.run_parentContainer); 
		mapRelLayout = (RelativeLayout) findViewById(R.id.run_relLayout); 
		//******************DEFINE OTHER OBJECTS**************************
		droidLoc = new DroidLoc(this);
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

		String copyrightPlusWarnings = currentLeg.getCopyright() + "<br/>" + currentLeg.getWarnings(); 
		Spanned copyrightSpanned = android.text.Html.fromHtml(copyrightPlusWarnings); 
		copyrightTextView.setText(copyrightSpanned); 

		//******************CALL SETUP METHODS****************************
		setupMap(); 
		setupCenterOnMeButton(); 
		setupZoomToRouteButton(); 
		setupZoomButtons(); 

		long theTime = System.currentTimeMillis(); 

		//onCreate() is called when a new leg starts, so set the start time for the leg and the first step
		currentLeg.setStartTime(theTime); 
		currentLeg.get(0).setStartTime(theTime); 

		myTts = funRunApp.getTextToSpeech(); 
		ttsTools = new DroidTTS(); 
		firstSpeechCompleted = false; 

		myTts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
			@Override
			public void onUtteranceCompleted(String id) {
				firstSpeechCompleted = true; 
			}
		});
    }

    private GoogleMap getGoogleMap() {
        if (googleMap != null) {
            return googleMap;
        }

        googleMap = myMap.getMap();
        return googleMap;
    }

    @Override
    public void onResume() {
        super.onResume();
        myMap.onResume();
        Log.i(getClass().getCanonicalName(), "Running onResume() in FunRunActivity");
        zoomToRoute();
    }

    @Override
    public void onPause() {
        super.onPause();
        myMap.onPause();
    }

	private void grabPrefs() {
		Resources res = getResources(); 

		SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this); 

		String default_accept_radius = res.getString(R.string.default_accept_radius); 
		String default_min_run = res.getString(R.string.default_min_run); 
		String default_path_segment = res.getString(R.string.default_path_segment); 
		String default_speak_directions = res.getString(R.string.default_speak_directions); 

		String accept_radius_key = res.getString(R.string.accept_radius_pref); 
		String min_run_key = res.getString(R.string.min_run_pref); 
		String path_segment_key = res.getString(R.string.path_segment_pref); 
		String speak_directions_key = res.getString(R.string.speak_directions_pref); 

		ACCEPT_RADIUS_METERS = Float.parseFloat(prefs.getString(accept_radius_key, default_accept_radius)); 
		MIN_DISTANCE_TO_SAVE = Float.parseFloat(prefs.getString(min_run_key, default_min_run)); 
		PATH_INCREMENT_METERS = Float.parseFloat(prefs.getString(path_segment_key, default_path_segment)); 
		SPEAK_DIRECTIONS = Boolean.parseBoolean(prefs.getString(speak_directions_key, default_speak_directions)); 

	}

	@Override
	public boolean onKeyDown( int keycode, KeyEvent e) {		
	//	super.onKeyDown(keycode, e); 

		if (keycode == KeyEvent.KEYCODE_VOLUME_UP) {
			if (firstSpeechCompleted && SPEAK_DIRECTIONS && funRunApp.isTtsReady() && (!myTts.isSpeaking())) { //If a TTS isn't already playing, say the directions again. This avoids the annoying-as-hell possibility of spamming the TTS
				speakDirections(); 	
			}
			audioMan.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI); 
			return true; 
		}
		else if (keycode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			audioMan.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI); 
			return true; 
		}
		else if (keycode == KeyEvent.KEYCODE_BACK) {
			endActivity(); 
			return true; 
		}

		return false; 
	}

	public void endActivity() {
		String msg; 
		double distanceRan = currentLeg.getActualDistanceRan(); 
		
		if (distanceRan < MIN_DISTANCE_TO_SAVE) {
			msg = "Your progress won't be saved. You ran less than " + (int)MIN_DISTANCE_TO_SAVE + " meters.";
		}
		else {
			msg = "Your progress will be saved, since you ran " + new java.text.DecimalFormat("#.##").format(distanceRan) + " meters.";  
		}

		DroidDialogs.showPopup(this, false, "Choose new place?", 
								"Stop running to " + runPlace.getName() + "?\n\n" + msg, 
								"Okay", "No way!", 
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
										dialog.dismiss(); 
										FunRunActivity.this.finish(); 
									}
								},	
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
										dialog.dismiss(); 
									}
								}	
								); 

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus); 

		if (hasFocus) {
			myFunRunOverlay.startRunAnimation(); 
		}

	}

	@Override 
	protected void onStart() {
		super.onStart();

		//******************GET PREFERENCES*****************************
		grabPrefs(); 

		firstSpeechCompleted = false; 

		//See if current step was updated by StepCompleteActivity
		currentStep = ((FunRunApplication)getApplication()).getCurrentStep(); 
		if (currentStep != null) {
			htmlInstructions = Html.fromHtml(currentStep.getHtmlInstructions().trim());		
			speakingInstructions = ttsTools.expandDirectionsString(htmlInstructions.toString()); 
			updateDirectionsTextView(); 

			if (SPEAK_DIRECTIONS && funRunApp.isTtsReady()) { //Expand abbreviations so it speaks properly and play it
				HashMap<String,String> params = new HashMap<String,String> (); 
				params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FIRST_SPEECH"); 
				myTts.speak(speakingInstructions, TextToSpeech.QUEUE_FLUSH, null); 
				myTts.playSilence(300, TextToSpeech.QUEUE_ADD, null); 
				myTts.speak("Press volume up to hear directions again.", TextToSpeech.QUEUE_ADD, params); 
			}

			setupLocListener(); //Instantiate new location listeners 

			//Start up location updates
			droidLoc.getLocManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, FunRunApplication.MIN_GPS_UPDATE_TIME_MS, 0, myGpsListener);
			droidLoc.getLocManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, FunRunApplication.MIN_GPS_UPDATE_TIME_MS, 0, myNetworkListener);

			//Force update of lastKnownLatLng
			bestLocation = droidLoc.getBestLocation(bestLocation); 
			lastKnownLatLng = DroidLoc.locationToLatLng(bestLocation);

			//Update visuals so it doesn't show you in the wrong place
			myFunRunOverlay.updateCurrentLocation(lastKnownLatLng);
			myMap.invalidate(); 
		}
		else { //current step was null, indicating the user finished running to a place.
			//Go choose another place	
			finish(); 
		}
	}

	private void speakDirections() {
		myTts.speak(speakingInstructions, TextToSpeech.QUEUE_FLUSH, null); 
	}
	
	@Override 
	protected void onStop() {
		super.onStop(); 
		droidLoc.getLocManager().removeUpdates(myGpsListener); 
		droidLoc.getLocManager().removeUpdates(myNetworkListener); 
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		//Remove this leg if the runner didn't go any distance. 
		if (currentLeg.getActualDistanceRan() < MIN_DISTANCE_TO_SAVE) {
			runDirections.remove(currentLeg); 
			(Toast.makeText(this, "You ran too little. Progress not saved.", Toast.LENGTH_SHORT)).show();
			
		}
		else {
			(Toast.makeText(this, currentLeg.getLegPoints() + " points earned!", Toast.LENGTH_SHORT)).show();
			//Here: write directions to DB. If this is too slow, even with a transaction, then will change implementation to instead write as they run, 
			//and delete if they don't run enough
			try {
				funRunApp.getDbWriter().insertLeg(currentLeg); 
			}
			catch (SQLException e) {
				System.err.println("Error writing leg to SQLite DB: "); 
				e.printStackTrace(); 
			}
		}
        myMap.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        myMap.onSaveInstanceState(bundle);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        myMap.onLowMemory();
    }

    private void updateDirectionsTextView() {
		directionsTextView.setText(htmlInstructions); 	
	}

	private void setupLocListener() {
		myGpsListener = new MyLocListener(); 
		myNetworkListener = new MyLocListener(); 
	}

	private void updateLocation(Location l) {
		if (l==null) {
			return;
		}

		bestLocation = droidLoc.compareLocations(bestLocation, l); //Compare new location to previous best, and return the best one

		LatLng newLatLng = DroidLoc.locationToLatLng(bestLocation);

		lastKnownLatLng = newLatLng;

		//Update location and invalidate map to redraw
		myFunRunOverlay.updateCurrentLocation(lastKnownLatLng);
		myMap.invalidate(); 

		LatLng latLng = null;
		if (bestLocation != null) {
			latLng = new LatLng(bestLocation.getLatitude(), bestLocation.getLongitude());
		}

		//Check if we've finished a step
		checkForCompleteSteps(); 

		//Just for fun! Animate the title bar.
		//As a side effect, this indicates whether GPS is working or not (^o ^o)
		animateTitleBar(); 

		//Add a GeoPoint to the actualPath in the currentLeg, provided the previous point is far enough away from the current point. 
		//This obviously is intended to prevent 
		if (latLng != null) {
			addToActualPath(latLng);
		} 
	}

	private void checkForCompleteSteps() {
		float distance[] = new float[1]; 
		GoogleStep step;
		double latLng[] = DroidLoc.latLngToArray(lastKnownLatLng);
		for (int i = currentLeg.getMaxStepCompleted() + 1; i < currentLeg.size(); i++) {
			step = currentLeg.get(i); 
			Location.distanceBetween(step.getEndPoint()[0], step.getEndPoint()[1], latLng[0], latLng[1], distance); 
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

	private void addToActualPath(LatLng latLng) {
		List<LatLng> actualPath = currentLeg.getActualPath();
		int size = actualPath.size(); 
		if (size == 0) {
			actualPath.add(latLng);
			return; 
		}
		LatLng lastPathPoint = actualPath.get(size - 1);
		float[] distance = new float[1]; 

		android.location.Location.distanceBetween(lastPathPoint.latitude, lastPathPoint.longitude, latLng.latitude, latLng.longitude, distance);

		if (distance[0] >= PATH_INCREMENT_METERS) {
			currentLeg.addToActualPath(latLng);
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
		myFunRunOverlay = new FunRunOverlay(myMap, null, true, false, true, mapRelLayout);
		myFunRunOverlay.updateCurrentDirections(runDirections);
        GoogleMap googleMap = getGoogleMap();
        if (googleMap != null) {
            myFunRunOverlay.drawOverlays(googleMap);
        }
		
		myMap.invalidate(); 
	}

	private void setupCenterOnMeButton() {

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
		if (lastKnownLatLng == null) {
			return; 
		}
		else {
            GoogleMap googleMap = getGoogleMap();
            if (googleMap == null) {
                return;
            }
            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(lastKnownLatLng)
                    .build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            googleMap.animateCamera(cameraUpdate);
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
                    GoogleMap googleMap = getGoogleMap();
                    if (googleMap != null) {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomIn();
                        googleMap.animateCamera(cameraUpdate);
                    }
				}
			});	
		zoomOutButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
                    GoogleMap googleMap = getGoogleMap();
                    if (googleMap != null) {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomOut();
                        googleMap.animateCamera(cameraUpdate);
                    }
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
		final LatLng neBound = DroidLoc.degreesToLatLng(currentLeg.getNeBound());
		final LatLng swBound = DroidLoc.degreesToLatLng(currentLeg.getSwBound());
		final LatLng midPoint = new LatLng( (neBound.latitude + swBound.latitude) / 2.0d, (neBound.longitude + swBound.longitude) / 2.0d );

        GoogleMap googleMap = getGoogleMap();
        if (googleMap != null) {
            CameraUpdate updatePosition = CameraUpdateFactory.newLatLng(midPoint);
            googleMap.animateCamera(updatePosition);

            LatLngBounds latLngBounds = new LatLngBounds(swBound, neBound);
            CameraUpdate zoomToSpan = CameraUpdateFactory.newLatLngBounds(latLngBounds, 100);

            googleMap.animateCamera(zoomToSpan);
        }
	}

	class MyLocListener implements LocationListener {

		@Override
		public void onLocationChanged(Location l) {
			FunRunActivity.this.updateLocation(l); 
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}

		@Override
		public void onProviderEnabled(String provider) {}

		@Override
		public void onProviderDisabled(String provider) {}		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.funrun_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			Intent i = new Intent(this, xanthanov.droid.funrun.pref.FunRunPref.class); 
			startActivity(i); 
			return true;
		case R.id.menu_back:
			endActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}

