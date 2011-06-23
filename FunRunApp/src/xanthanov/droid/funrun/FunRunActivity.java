package xanthanov.droid.funrun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface; 

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button; 
import android.widget.LinearLayout;
import android.content.Context; 
import android.location.LocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.content.Intent;
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
	private Button showDirectionsButton; 
	private TextView directionsTextView; 
	//*******************OTHER OBJECTS****************************
	private FunRunApplication fra; 
	private MyLocationOverlay myLocOverlay;
	private MapController myMapController; 
	private LocationListener myLocListener; 
	private LocationManager myLocManager;
	private FunRunOverlay myFunRunOverlay; 
	private AlertDialog popup;
	private GeoPoint lastKnownLocation; 
	private GoogleDirections runDirections; 
	private GooglePlace runPlace;  
	private GoogleStep currentStep; 
	private Spanned htmlInstructions; 
	private String showDirections; 
	private String hideDirections; 
	//*****************CONSTANTS**********************************
	private final static int DEFAULT_ZOOM = 15; 
	private final static int DEFAULT_RADIUS_METERS = 1000;
	public final static int MAX_RADIUS_METERS = 4000; 
	public final static int MIN_RADIUS_METERS = 50; 

	public final static int ACCEPT_RADIUS_METERS = 50; 
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
//		showDirectionsButton = (Button) findViewById(R.id.showDirectionsButton); 
		directionsTextView = (TextView) findViewById(R.id.directionsTextView); 
		parentContainer = (LinearLayout) findViewById(R.id.run_parentContainer); 
		//********************POPUP DIALOG*******************************
		popup = null; 
		//******************DEFINE OTHER OBJECTS**************************
		myLocOverlay = new MyLocationOverlay(this, myMap); 
		myMapController = myMap.getController(); 
		//Get the Application object with global data
		fra = (FunRunApplication) this.getApplicationContext(); 
		runDirections = fra.getRunDirections(); 
		runPlace = fra.getRunPlace();
		//Initialize currentStep to the first step in the first leg of the GoogleDirections object
		currentStep = runDirections.get(runDirections.size() - 1).get(0);  

		//Just string constants used on the "Show / Hide directions" button
//		showDirections= (String)(this.getResources().getText(R.string.show_directions)); 
//		hideDirections= (String)(this.getResources().getText(R.string.hide_directions)); 

		//Get the HTML directions from the raw string
		htmlInstructions = Html.fromHtml(currentStep.getHtmlInstructions());		
		

		//******************CALL SETUP METHODS****************************
		//Get the last known location in case GPS isn't currently going
		setupLocListener(); 
		setupMap(); 
		setupCenterOnMeButton(); 
		setupZoomToRouteButton(); 
		setupZoomButtons(); 
//		setupShowDirectionsButton(); 
		updateDirectionsTextView(); 
		lastKnownLocation = getLastKnownLoc(); //this gets the last known location, even if the location updates aren't working yet
		myFunRunOverlay.updateCurrentLocation(lastKnownLocation); 
		myFunRunOverlay.updateCurrentDirections(runDirections); 
		centerOnMe(); 
    }
	
	public boolean isRouteDisplayed() {
		return true;
	}

	@Override 
	protected void onResume() {
		super.onResume();
		myLocOverlay.enableMyLocation(); 
		myLocOverlay.enableCompass(); 	
		myLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, FunRunApplication.MIN_GPS_UPDATE_TIME_MS, 0, myLocListener);
	}
	
	@Override 
	protected void onPause() {
		super.onPause(); 
		myLocManager.removeUpdates(myLocListener); 
		myLocOverlay.disableMyLocation(); 
		myLocOverlay.disableCompass();
	}

	private void updateDirectionsTextView() {
		directionsTextView.setText(htmlInstructions); 	
	}

	private void setupLocListener() {
		myLocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		myLocListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location l) {
				updateLocation(l); 
			}
			public void onStatusChanged(String provider, int status, Bundle extras) {}

    		public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}		
		};

		myLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocListener);
	}

	private GeoPoint getLastKnownLoc() {
		
		//If we've gotten our location from the location update listener, just return what we already have. 
		if (lastKnownLocation != null) {
			return lastKnownLocation; 
		}

		Location l= null; 

		//Otherwise, try to get the last known location from the LocationManager
		try { 
			l = myLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 	
		}
		catch (Exception e) {
			System.out.println("Failure getting last known location."); 
			return null; 
		}

		if (l!= null) {
			lastKnownLocation = new GeoPoint((int) (l.getLatitude()*1E6), (int) (l.getLongitude()*1E6)); 
			myFunRunOverlay.updateCurrentLocation(lastKnownLocation); 
		}
		return lastKnownLocation; 
	}

	private void updateLocation(Location l) {
		if (l==null) {
			return;
		}
		 
		Double latPoint=l.getLatitude(); 
		Double lngPoint=l.getLongitude(); 
	
		//Update the local variable with the last known location
		this.lastKnownLocation = new GeoPoint((int) (latPoint*1E6), (int) (lngPoint*1E6)); 
		
		//Update the overlay so it draws properly
		myFunRunOverlay.updateCurrentLocation(lastKnownLocation); 
	}

	private void setupMap() {
		myFunRunOverlay = new FunRunOverlay(myMap, null);
		myMap.getOverlays().add(myLocOverlay); 
		myMap.getOverlays().add(myFunRunOverlay); 
		myMapController.setZoom(DEFAULT_ZOOM); 
		//myLocOverlay.enableMyLocation(); //Disable for now, have stick figure instead 
		myLocOverlay.enableCompass(); 	
		myMap.postInvalidate(); 
	}

	private void setupCenterOnMeButton() {
		final MapController mc = myMapController; 
		final GeoPoint loc = lastKnownLocation; 

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
		GeoPoint neBound = runDirections.getNeBound(); 
		GeoPoint swBound = runDirections.getSwBound(); 
		final GeoPoint midPoint = new GeoPoint( (neBound.getLatitudeE6() + swBound.getLatitudeE6())/2, (neBound.getLongitudeE6() + swBound.getLongitudeE6())/2);
		final int latSpan = Math.abs(neBound.getLatitudeE6() - swBound.getLatitudeE6()); 
		final int lngSpan = Math.abs(neBound.getLongitudeE6() - swBound.getLongitudeE6()); 
		final MapController mc = myMapController; 

		zoomToRouteButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					 mc.animateTo(midPoint); 
					 mc.zoomToSpan(latSpan, lngSpan); 
				}
			});	
	} 
/*
	private void setupShowDirectionsButton() {

		showDirectionsButton.setOnClickListener( new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				if (showDirectionsButton.getText().equals(showDirections) ) {
					directionsTextView.setVisibility(View.VISIBLE); 				
					showDirectionsButton.setText(hideDirections);
				}
				else {
					directionsTextView.setVisibility(View.GONE); 				
					showDirectionsButton.setText(showDirections);
				}
			}
		});	
	}
*/

	private void showPopup(String title, String txt) {
		AlertDialog.Builder myBuilder = new AlertDialog.Builder(this); 
		myBuilder.setMessage(txt); 
		myBuilder.setTitle(title); 
		myBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
           }
       }); 

		popup = myBuilder.create(); 
		popup.show(); 
	}

}

