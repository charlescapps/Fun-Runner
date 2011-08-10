//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun;

import xanthanov.droid.xantools.*; 
import xanthanov.droid.gplace.*;
import xanthanov.droid.funrun.persist.RunDataSerializer; 

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog; 
import android.content.DialogInterface; 

import android.os.Bundle;
import android.os.Handler;
import android.os.AsyncTask; 
import android.view.KeyEvent; 
import android.view.View;
import android.view.Gravity;
import android.view.animation.Animation; 
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Button; 
import android.widget.ImageButton; 
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.content.Context; 
import android.location.LocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.content.Intent;
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

/**
* <h3>First activity when you start a new run. Runner chooses the next place to run to. </h3>
* Has a spinner to choose the category, and a button to 'Find nearby places'. 
* MapView shows where the runner is. 
*<h3>Notes:</h3>
*<ul>
* <li>Uses PlaceSearcher class to perform Google Places API query.</li>
* <li>Uses DirectionGetter class to get Google walking directions as the user presses "next place" button.</li>
* <li>Does these requests with an AsyncTask.</li>
* <li>MapView shows previous legs the user ran.</li>
* <li>TODO: Change dialog so that you can see the route when deciding where to run. </li>
* <li>Along same line, make the background less dark as well. </li>
* <li>(Seems silly to get the directions but not let the user see them before running.) </li>
* </ul>
*
* @see xanthanov.droid.gplace
* @see xanthanov.droid.xantools
* @author Charles L. Capps
* @version 0.9b
**/

public class ChoosePlaceActivity extends MapActivity
{

	//***********VIEW OBJECTS DEFINED IN XML**********************
	private Spinner runCategorySpinner; 
	private Button whereAmIButton; 
	private ImageButton nextDestinationButton;
	private MapView myMap;
	private Button zoomInButton;
	private Button zoomOutButton;
	//*******************OTHER OBJECTS****************************
	private DroidLoc droidLoc; 
	private MyLocationOverlay myLocOverlay;
	private MapController myMapController; 
	private LocationListener myLocListener; 
	private PlaceSearcher myPlaceSearcher; //Class to do HTTP request to get place data from google maps API
	private DirectionGetter myDirectionGetter; //Class to do an HTTP request to get walking directions
	private FunRunOverlay myFunRunOverlay; 
	private AlertDialog popup;
	private GeoPoint lastKnownLocation; 
	private GeoPoint firstGpsFix;
	//Places found, directions found, etc.
	private List<GooglePlace> nearbyPlaces;
	private List<GooglePlace> remainingPlaces;
	private GoogleDirections currentDirections; 
	private GooglePlace currentRunToPlace;
	private GoogleLeg tempLeg; 
	private FunRunApplication funRunApp; 
	//*****************CONSTANTS**********************************
	private final static int DEFAULT_ZOOM = 15; 
	private final static int DEFAULT_RADIUS_METERS = 1000;
	public final static int MAX_RADIUS_METERS = 4000; 
	//************************************************************
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectlayout);

		//***************GET VIEWS DEFINED IN XML***********************
		runCategorySpinner = (Spinner) findViewById(R.id.runCategorySpinner); 
		myMap = (MapView) findViewById(R.id.myMap); 
		whereAmIButton = (Button) findViewById(R.id.gpsButton); 
		nextDestinationButton = (ImageButton) findViewById(R.id.nextDestinationButton); 
		zoomInButton = (Button) findViewById(R.id.buttonZoomIn); 
		zoomOutButton = (Button) findViewById(R.id.buttonZoomOut); 
		//******************DEFINE OTHER OBJECTS**************************
		droidLoc = new DroidLoc(this); 
		myLocOverlay = new MyLocationOverlay(this, myMap); 
		myMapController = myMap.getController(); 
		myPlaceSearcher = new PlaceSearcher(this.getResources()); 
		myDirectionGetter = new DirectionGetter(); 
		lastKnownLocation = droidLoc.getLastKnownLoc();
		System.out.println("Last location: " + lastKnownLocation);
		funRunApp = (FunRunApplication) getApplicationContext();

		currentDirections = new GoogleDirections(); //Create new directions now, since they correspond to a run
		funRunApp.setRunDirections(currentDirections); //Store the current directions object with the application 
		funRunApp.setCurrentDirectionsAdded(false); 

		if (!RunDataSerializer.createRunDir(currentDirections)) {//Create directory to store these runs
			showCriticalErrorPopup("Critical Error", "Failed to create new run directory.\nTry restarting the app."); 
		} 
		//******************CALL SETUP METHODS****************************
		setupLocListener(); 
		setupSpinner(); 
		setupMap(); 
		setupWhereAmIButton(); 
		setupNextButton(); 
		setupZoomButtons(); 
		centerOnMe(); 
		myMap.preLoad(); 

		List<String> providers = 
		droidLoc.getLocManager().getProviders(false); 

		System.out.println("ALL PROVIDERS:"); 
		for (String s: providers) {
			System.out.println("\t" + s); 
		}
    }

	@Override	
	public boolean onKeyDown( int keycode, KeyEvent e) {		
		//super.onKeyDown(keycode, e); 

		if (keycode == KeyEvent.KEYCODE_BACK) {
			DroidDialogs.showPopup(this, false, "Finished Running?", 
				"Finished running for the day?\nYour run will be saved.", 
				"Okay", "No way!", 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss(); 
						ChoosePlaceActivity.this.finish(); 
					}
				},	
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss(); 
					}
				}	
				); 
			return true;
		}
		return false; 

	}

	@Override
	public boolean isRouteDisplayed() {
		return true;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus); 

		if (hasFocus) {
			myMap.invalidate(); 
			centerOnMe(); 
		}

	}

	@Override
	public void onStart() {
		super.onStart(); 
		checkGps(); 
		myLocOverlay.enableCompass(); 	
		droidLoc.getLocManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, FunRunApplication.MIN_GPS_UPDATE_TIME_MS, 0, myLocListener);
		lastKnownLocation = droidLoc.getLastKnownLoc(); 
		myFunRunOverlay.updateCurrentLocation(lastKnownLocation); 
	}

	@Override
	public void onStop() {
		super.onStop(); 
		droidLoc.getLocManager().removeUpdates(myLocListener); 
		myLocOverlay.disableCompass();
	}

	@Override 
	protected void onDestroy() {
		super.onDestroy(); 

		FunRunApplication funRunApp = (FunRunApplication) getApplicationContext(); 

		if (currentDirections != null) {
			System.out.println("Distance so far for current directions when leaving ChoosePlace: " + currentDirections.getDistanceSoFar()); 
		}
		else {
			System.out.println("Directions null upon leaving ChoosePlaceActivity."); 
		}

		if (currentDirections.size() <= 0) {
			RunDataSerializer.deleteEmptyRun(currentDirections); 
		}

	}

	private void checkGps() {
		LocationManager lm = droidLoc.getLocManager(); 
		boolean isGpsEnabled = false; 
		try {		
			isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER); 
		}
		catch (Exception e) {
			//Do nothing, isGpsEnabled will remain false if anything goes awry	
		}
		if (!isGpsEnabled) {
			showCriticalErrorPopup("GPS Not Enabled", "GPS appears to be disabled on your device.\nPlease turn on GPS and restart the app."); 
		}

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
		if (l==null) {
			return;
		}
		boolean wasNull = (lastKnownLocation == null || firstGpsFix == null); 

		lastKnownLocation = firstGpsFix = DroidLoc.degreesToGeoPoint(l.getLatitude(), l.getLongitude()); 
		myFunRunOverlay.updateCurrentLocation(lastKnownLocation); 

		if (wasNull) {
			centerOnMe(); 
		}
	}

	private void setupSpinner() {

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_array, R.layout.normal_spinner);	
		adapter.setDropDownViewResource(R.layout.spinner_dropdown);
		runCategorySpinner.setAdapter(adapter); 
		
		//runCategorySpinner.setOnItemSelectedListener(new FunRunOnItemSelected());
	}

	private void setupMap() {
		myFunRunOverlay = new FunRunOverlay(myMap, null, false, false, false, null);
		myFunRunOverlay.updateCurrentLocation(lastKnownLocation); 
		myFunRunOverlay.updateCurrentDirections(currentDirections); 
		myMap.getOverlays().add(myLocOverlay); 
		myMap.getOverlays().add(myFunRunOverlay); 
		myMapController.setZoom(DEFAULT_ZOOM); 
		myMap.invalidate(); 
	}

	private void setupWhereAmIButton() {

		Animation animation = new AlphaAnimation(1.0f, 0.7f);
		animation.setFillAfter(true);
		whereAmIButton.startAnimation(animation);

		whereAmIButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					centerOnMe(); 
				}
			});	
	}

	private void centerOnMe() {
		
		if (lastKnownLocation !=null) {
			myMapController.animateTo(lastKnownLocation); 
			myFunRunOverlay.updateCurrentLocation(lastKnownLocation); 
		}
	}

	private void setupNextButton() {
		nextDestinationButton.setOnClickListener(new OnClickNext(this));
	}

	private List<GooglePlace> performPlacesQuery(String search, GeoPoint lastLocation, int currentRadiusMeters) throws Exception {
 
		List<GooglePlace> foundPlaces = null; 

		//START LOADING DIALOG
		ProgressRunnable pr = DroidDialogs.showProgressDialog(this, "", "Loading nearby places...");  
		
		try {
			//Increase the radius until something is found (or the max radius is reached)
			while ( currentRadiusMeters <= MAX_RADIUS_METERS ) {
				//Create a new thread 
				foundPlaces = myPlaceSearcher.getNearbyPlaces(search, lastLocation, currentRadiusMeters); 

				//IF we didn't find nothin', 
				//Increase search radius, though google says it is merely a "suggestion" so fuck if I know how much this matters
				if (foundPlaces == null || foundPlaces.size() <= 0) {
					currentRadiusMeters*=2; 
				}
				else {
					break; //Done. We found at least one place 
				}
			}
		}
		catch (Exception e) {
			nearbyPlaces = null; //An error occurred. Use null to indicate we didn't simply find zero places
			throw e; 
		}
		finally {
			pr.dismissDialog();
		}

		return foundPlaces; 

	}

	private void getNextPlace() {

		if (nearbyPlaces == null || nearbyPlaces.size() == 0 || remainingPlaces == null) {
			//There *should* be a dialog alerting the user if no places were found!
			System.out.println("No nearby places found in getNextPlace\n"); 
			return; 
		}
		
		//check if no places are remaining
		if (remainingPlaces.size() == 0) {
			remainingPlaces = new ArrayList<GooglePlace>(nearbyPlaces); 
			DroidDialogs.showPopup(this, "All places rejected :-(", "You rejected all nearby places of this type!\nChoose a new category, or click 'Next Place' to see your options again.");
			currentRunToPlace = null; 
			return; 
		}

		//Choose a "random" place from list of remaining places, actually getting closest remaining place
		currentRunToPlace = remainingPlaces.get(0);			
		remainingPlaces.remove(currentRunToPlace); 
		System.out.println("Chosen place:" + currentRunToPlace); 

		//Make another HTTP request to get directions from current location to 'runToPlace'
		lastKnownLocation = droidLoc.getLastKnownLoc(); //Get most recent location before getting directions
		myFunRunOverlay.updateCurrentLocation(lastKnownLocation); 

		tempLeg = myDirectionGetter.getDirections(lastKnownLocation, currentRunToPlace.getGeoPoint());
		System.out.println("Temp leg: " + tempLeg); 

		if (tempLeg != null) {

			tempLeg.setLegDestination(currentRunToPlace); 
			
			showAcceptRejectPopup("Run to:\n" + currentRunToPlace.getName() + "?", 
				"Place: " + currentRunToPlace.getName() + "\n" +
				"Distance: " + tempLeg.getDistanceString() + " / " + tempLeg.getDistanceMeters() + "m" );	
		}
		else {
			DroidDialogs.showPopup(this, "Error connecting to \nGoogle Maps", "An error occurred while connecting to Google Maps. Make sure you have an internet connection and try again."); 
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

	private void showAcceptRejectPopup(String title, String txt) {
		AlertDialog.Builder myBuilder = new AlertDialog.Builder(this); 
		myBuilder.setMessage(txt); 
		myBuilder.setTitle(title); 

		myBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
				startRunning(); 
           }
       }); 

		myBuilder.setNegativeButton("Next Place!", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
				getNextPlace(); 
           }
       }); 

		popup = myBuilder.create(); 
		popup.setCancelable(true); 

		android.view.WindowManager.LayoutParams WMLP = popup.getWindow().getAttributes();

		//WMLP.x = 100;   //x positionv
		WMLP.gravity = android.view.Gravity.TOP; 
		WMLP.verticalMargin = .05f;

		popup.getWindow().setAttributes(WMLP);

		popup.show(); 
	}

	private void showCriticalErrorPopup(String title, String txt) {
		AlertDialog.Builder myBuilder = new AlertDialog.Builder(this); 
		myBuilder.setMessage(txt); 
		myBuilder.setTitle(title); 

		myBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
           }
       }); 

		popup = myBuilder.create(); 
		popup.setCancelable(false); 
		popup.show(); 

	}

	private void startRunning() {
		final Intent i = new Intent(this, FunRunActivity.class); 
		final Activity a = this; 

		if (tempLeg == null || currentDirections == null || currentRunToPlace == null) {
			DroidDialogs.showPopup(a, "No Place Selected", "You must choose a place before running.\nChoose a category, then click 'Find a Place'. "); 
			return; 
		}
		
		//Add tempLeg to the global directions
		//Will be removed if the runner doesn't complete any steps
		currentDirections.add(tempLeg); 

		//Download image for new leg in a thread--currently not using the image
		//Google usually doesn't have a special image for most places
		/*
		Thread downloadImageThread = new Thread(new Runnable() {
			@Override
			public void run() {
				currentRunToPlace.downloadImage(); 
			}
		});

		downloadImageThread.start(); 
		*/
		startActivity(i); 
	}

	private class PlacesQueryTask extends AsyncTask<String, Integer, List<GooglePlace>>{
		private String searchStr = null; 
		private Activity a = null;

		public PlacesQueryTask(Activity a) {
			super(); 
			this.a = a; 
		}
	
		protected List<GooglePlace> doInBackground(String... searchParams) {
			searchStr = searchParams[0]; 
			List<GooglePlace> result = null; 

			try {
				result = performPlacesQuery(searchStr, lastKnownLocation, DEFAULT_RADIUS_METERS ); 	
			}
			catch (Exception e) {
				DroidDialogs.showPopup(a, "Error connecting to Google Maps", "Unable to connect to Google Maps.\nPlease check your internet connection and try again."); 
				e.printStackTrace(); 
				return null; 
			}
			return result; 
		
		}

		protected void onPostExecute(List<GooglePlace> result) {
			//Done attempting to get places, so assign to field nearbyPlaces 
			nearbyPlaces = result; 
			//Set remainingPlaces = shallow copy of nearbyPlaces  
			remainingPlaces = (nearbyPlaces == null ? null : new ArrayList(nearbyPlaces)); 

			if (result == null) {
				; //Places query should show a popup indicating you don't have an internet connection or some other horrible error occurred
			}
			else if (result.size() <= 0) {
				//Output dialog indicating nothing was found...choose a new category
				DroidDialogs.showPopup(a, "Choose a new category", "No '" + searchStr + "'s found within " + MAX_RADIUS_METERS/1000 + " km.\n\n" 
						+ "Please choose a different category and try again."); 
				System.err.println("Places query: ZERO PLACES FOUND"); 
				
				return; 
			}
			else {
			//If we successfully got the places, print them out for debugging.
			//AND sort them by which is closer...
			//To avoid getting directions for all of them, do the crude thing of getting "as the bird flies" distance
				//PlaceSearcher.printListOfPlaces(nearbyPlaces);  
				java.util.Collections.sort(nearbyPlaces, new PlaceComparator(lastKnownLocation)); 
			}
			//Call the dialog to cycle through places
			getNextPlace();
		}
	}

	private class OnClickNext implements View.OnClickListener {

		private Activity a = null; 

		public OnClickNext(Activity a) {
			super(); 
			this.a = a; 
		}
		
		public void onClick(View v) {
			if (firstGpsFix == null) {
				DroidDialogs.showPopup(a, false, "No GPS location found", "Up-to-date location not found since app started. Turn on GPS and go outside.\n\n Start anyway?", 
												"Yeah", "Wait a bit", 
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int id) {
														dialog.dismiss(); 
														startPlacesQuery(); 
													}
												}, 
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int id) {
														dialog.dismiss(); 
													}
												});
			}
			else {
				startPlacesQuery(); 
			}

		}

	}

	private void startPlacesQuery() {
		String search = (runCategorySpinner.getSelectedItem()).toString();
		new PlacesQueryTask(this).execute(search); 	
	}
	
}

