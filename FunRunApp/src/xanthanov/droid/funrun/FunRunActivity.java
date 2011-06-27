package xanthanov.droid.funrun;

import xanthanov.droid.gplace.*;
import xanthanov.droid.xantools.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface; 

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation; 
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Button; 
import android.widget.LinearLayout;
import android.content.Context; 
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Intent;
import android.app.PendingIntent;
import android.text.Html; 
import android.text.Spanned; 

import java.util.List;
import java.util.ArrayList;
import java.util.Random; 

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MyLocationOverlay; 
import com.google.android.maps.Overlay; 

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
	//*****************CONSTANTS**********************************
	private final static int DEFAULT_ZOOM = 15; 
	private final static int DEFAULT_RADIUS_METERS = 1000;
	public final static int MAX_RADIUS_METERS = 4000; 
	public final static int MIN_RADIUS_METERS = 50; 

	public final static float ACCEPT_RADIUS_METERS = 40.0f; 
	public final static float PATH_INCREMENT_METERS = 20.0f; 
	//************************************************************
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.runlayout);

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
		runPlace = funRunApp.getRunPlace();
		//Initialize currentStep to the first step in the last leg of the GoogleDirections object
		//As the runner arrives at destinations, new legs will be added
		currentLeg = runDirections.get(runDirections.size()-1); 
		currentStep = currentLeg.get(0);  
		//Store current step in the Application object to pass between activities
		funRunApp.setCurrentStep(currentStep); 

		//Get the HTML directions from the raw string and set the text view
		htmlInstructions = Html.fromHtml(currentStep.getHtmlInstructions().trim());		
		System.out.println("HTML: " + htmlInstructions); 
		chosenPlaceTextView.setText("Running to " + runPlace.getName()); 		
		updateDirectionsTextView(); 

		//******************CALL SETUP METHODS****************************
		setupLocListener(); 
		setupMap(); 
		setupCenterOnMeButton(); 
		setupZoomToRouteButton(); 
		setupZoomButtons(); 

		long theTime = System.currentTimeMillis(); 

		//onCreate() is called when a new leg starts, so set the start time to now
		currentLeg.setStartTime(theTime); 

		//Make a proximity alert for end of route and current step, in case runner randomly takes a different route than given
		if (!currentStep.equals(currentLeg.finalStep())) {
			setupStepProximityAlert(currentStep, theTime);  
		}		

		setupStepProximityAlert(currentLeg.finalStep(), theTime);  

		lastKnownLocation = droidLoc.getLastKnownLoc(); 
		myFunRunOverlay.updateCurrentLocation(lastKnownLocation); 
		myFunRunOverlay.updateCurrentDirections(runDirections); 

		zoomToRoute(); 
    }
	
	public boolean isRouteDisplayed() {
		return true;
	}

	@Override 
	protected void onResume() {
		super.onResume();
		myLocOverlay.enableCompass(); 	
		droidLoc.getLocManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, FunRunApplication.MIN_GPS_UPDATE_TIME_MS, 0, myLocListener);
		if (funRunApp.killRunActivity()) {
			funRunApp.setKillRunActivity(false); 
			finish(); 
		}
	}
	
	@Override 
	protected void onPause() {
		super.onPause(); 
		droidLoc.getLocManager().removeUpdates(myLocListener); 
		myLocOverlay.disableCompass();
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

	void setupStepProximityAlert(GoogleStep step, long startTime) {
		//Add a proximity alert for the end point of the current step, start activity StepCompleteActivity
		step.setStartTime(startTime); 
		
		PendingIntent stepCompleteIntent = PendingIntent.getActivity(this, 0,  new Intent(this, StepCompleteActivity.class), PendingIntent.FLAG_ONE_SHOT); 	

		double latLng[] = DroidLoc.geoPointToDegrees(step.getEnd()); 
		droidLoc.getLocManager().addProximityAlert(latLng[0], latLng[1], ACCEPT_RADIUS_METERS, -1, stepCompleteIntent);    
	}

	private void updateLocation(Location l) {
		if (l==null) { //not sure if this locListener will ever return null here. May as well check
			return;
		}
		//Update the local variable with the last known location
		lastKnownLocation = DroidLoc.degreesToGeoPoint(l.getLatitude(), l.getLongitude()); 
		
		//Update the overlay so it draws properly
		myFunRunOverlay.updateCurrentLocation(lastKnownLocation); 
		myMap.postInvalidate(); 

		//Just for fun! Animate the title bar.
		//As a side effect, this indicates whether GPS is working or not (^o ^o)
		animateTitleBar(); 

		//Add a GeoPoint to the actualPath in the currentLeg, provided the previous point is far enough away from the current point. 
		//This obviously is intended to prevent 
		addToActualPath(lastKnownLocation); 
		myFunRunOverlay.updateActualPath(currentLeg.getActualPath()); 
	}

	private void addToActualPath(GeoPoint g) {
		List<GeoPoint> actualPath = currentLeg.getActualPath();
		int size = actualPath.size(); 
		if (size == 0) {
			actualPath.add(g); 
			return; 
		}
		GeoPoint lastPathPoint = actualPath.get(size - 1); 
		double[] lastPathPtDeg = DroidLoc.geoPointToDegrees(lastPathPoint); 
		double[] lastKnownDeg = DroidLoc.geoPointToDegrees(g); 
		float[] distance = new float[1]; 

		android.location.Location.distanceBetween(lastPathPtDeg[0], lastPathPtDeg[1], lastKnownDeg[0], lastKnownDeg[1], distance);

		if (distance[0] >= PATH_INCREMENT_METERS) {
			actualPath.add(g); 
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
		myFunRunOverlay = new FunRunOverlay(myMap, null);
		myMap.getOverlays().add(myLocOverlay); 
		myMap.getOverlays().add(myFunRunOverlay); 
		myLocOverlay.enableCompass(); 	
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

