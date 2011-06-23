package xanthanov.droid.funrun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface; 

import android.os.Bundle;
import android.view.View;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Button; 
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.content.Context; 
import android.location.LocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.content.Intent;

import java.util.List;
import java.util.ArrayList;
import java.util.Random; 

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MyLocationOverlay; 
import com.google.android.maps.Overlay; 

public class ChoosePlaceActivity extends MapActivity
{

	//***********VIEW OBJECTS DEFINED IN XML**********************
	private LinearLayout parentContainer; 
	private Spinner runCategorySpinner; 
	private Button whereAmIButton; 
	private Button nextDestinationButton;
	private MapView myMap;
	private Button zoomInButton;
	private Button zoomOutButton;
	private Button startRunningButton;
	//*******************OTHER OBJECTS****************************
	private MyLocationOverlay myLocOverlay;
	private MapController myMapController; 
	private LocationListener myLocListener; 
	private LocationManager myLocManager;
	private TextView emptySpinnerView; 
	private PlaceSearcher myPlaceSearcher; //Class to do HTTP request to get place data from google maps API
	private DirectionGetter myDirectionGetter; //Class to do an HTTP request to get walking directions
	private FunRunOverlay myFunRunOverlay; 
	private AlertDialog popup;
	private GeoPoint lastKnownLocation; 
	//****************Temporary storage...seemingly necessary since the dialogs are asynchronous!*****
	private List<GooglePlace> nearbyPlaces; 
	private List<GooglePlace> remainingPlaces; 
	private GoogleDirections currentDirections; 
	private GooglePlace currentRunToPlace;  
	//*****************CONSTANTS**********************************
	private final static int DEFAULT_ZOOM = 15; 
	private final static int DEFAULT_RADIUS_METERS = 1000;
	public final static int MAX_RADIUS_METERS = 4000; 
	public final static int MIN_RADIUS_METERS = 50; 
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
		nextDestinationButton = (Button) findViewById(R.id.nextDestinationButton); 
		zoomInButton = (Button) findViewById(R.id.buttonZoomIn); 
		zoomOutButton = (Button) findViewById(R.id.buttonZoomOut); 
		startRunningButton = (Button) findViewById(R.id.startRunningButton); 
		parentContainer = (LinearLayout) findViewById(R.id.parentContainer); 
		//********************POPUP DIALOG*******************************
		popup = null; 
		//******************DEFINE OTHER OBJECTS**************************
		myLocOverlay = new MyLocationOverlay(this, myMap); 
		myMapController = myMap.getController(); 
		myPlaceSearcher = new PlaceSearcher(this.getResources()); 
		myDirectionGetter = new DirectionGetter(); 
		lastKnownLocation = null;
		currentDirections = null; 
		currentRunToPlace = null; 
		nearbyPlaces = null; 
		remainingPlaces = null; 

		//******************CALL SETUP METHODS****************************
		setupLocListener(); 
		setupSpinner(); 
		setupMap(); 
		setupWhereAmIButton(); 
		setupNextButton(); 
		setupZoomButtons(); 
		setupStartRunningButton(); 
		
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

	private void updateLocation(Location l) {
		if (l==null) {
			return;
		}
		 
		Double latPoint=l.getLatitude(); 
		Double lngPoint=l.getLongitude(); 
	
		this.lastKnownLocation = new GeoPoint((int) (latPoint*1E6), (int) (lngPoint*1E6)); 
		
		myFunRunOverlay.updateCurrentLocation(lastKnownLocation); 

	}

	private void setupSpinner() {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_item);	
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		runCategorySpinner.setAdapter(adapter); 
		
		runCategorySpinner.setOnItemSelectedListener(new FunRunOnItemSelected());
	}

	private void setupMap() {
		myFunRunOverlay = new FunRunOverlay(myMap, null);
		myMap.getOverlays().add(myLocOverlay); 
		myMap.getOverlays().add(myFunRunOverlay); 
		myMapController.setZoom(DEFAULT_ZOOM); 
		myLocOverlay.enableMyLocation(); //Disable for now, have stick figure instead 
		myLocOverlay.enableCompass(); 	
		myMap.postInvalidate(); 
	}

	private void setupWhereAmIButton() {
		final MapController mc = myMapController; 
		final GeoPoint loc = getLastKnownLoc(); 

		whereAmIButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (loc != null) 
						mc.animateTo(loc); 
				}
			});	
	}

	private void setupNextButton() {
		
		nextDestinationButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						performPlacesQuery(); 
					}
					catch (Exception e) {
						showPopup("Error connecting to\nGoogle Maps", e.getMessage()); 
						nearbyPlaces = remainingPlaces = null; 
						return; 
					}
					getNextPlace();
				}
			});	
	}

	private void performPlacesQuery() throws Exception {
		System.out.println("Entering performGmapQuery()..."); 
		String search = (String)runCategorySpinner.getSelectedItem();
		GeoPoint lastLocation = getLastKnownLoc();

		System.out.println("Search:" + search + ",Last location:" + lastLocation); 

		int currentRadiusMeters = DEFAULT_RADIUS_METERS; 
 
		List<GooglePlace> foundPlaces = null; 

		//Increase the radius until something is found (or the max radius is reached)
		while ( currentRadiusMeters <= MAX_RADIUS_METERS && (foundPlaces == null || foundPlaces.size() <= 0)) {
			foundPlaces = myPlaceSearcher.getNearbyPlaces(search, lastLocation, currentRadiusMeters);
			
			if (foundPlaces == null || foundPlaces.size() <= 0) {
				//Output some message and increase search radius
				currentRadiusMeters*=2; 
			}
		}

		//Done attempting to get places, so assign to field nearbyPlaces (and remainingPlaces)		
		nearbyPlaces = foundPlaces; 
		remainingPlaces = (nearbyPlaces == null ? null : new ArrayList(nearbyPlaces)); //Shallow copy to have a separate record of which places remain 

		if (foundPlaces == null || foundPlaces.size() <= 0) {
			//Output dialog indicating nothing was found...choose a new category
			showPopup("Choose a new category", "No '" + search + "'s found within " + MAX_RADIUS_METERS/1000 + " km.\n\n" 
					+ "Please choose a different category and try again."); 
			System.out.println("Places query: ZERO PLACES FOUND"); 
			return; 
		}
		else {
		//If we successfully got the places, print them out for debugging.
		//AND sort them by which is closer...
		//To avoid getting directions for all of them, do the crude thing of getting "as the bird flies" distance
			PlaceSearcher.printListOfPlaces(nearbyPlaces);  
			java.util.Collections.sort(nearbyPlaces, new PlaceComparator(lastKnownLocation)); 
		}
	}

	private void getNextPlace() {

		if (nearbyPlaces == null || nearbyPlaces.size() == 0 || remainingPlaces == null ) {
			//There *should* be an asynchronous dialog alerting the user to this fact!
			return; 
		}
		//check if no places are remaining
		if (remainingPlaces.size() == 0) {
			remainingPlaces = new ArrayList<GooglePlace>(nearbyPlaces); 
			showPopup("All places rejected :-(", "You rejected all nearby places of this type!\nChoose a new category, or click 'Next Place' to see your options again.");
			return; 
		}

		//Choose a "random" place from list of remaining places, actually getting closest remaining place
		currentRunToPlace = remainingPlaces.get(0);			
		remainingPlaces.remove(currentRunToPlace); 
		System.out.println("Chosen place:" + currentRunToPlace); 

		//Make another HTTP request to get directions from current location to 'runToPlace'
		currentDirections = myDirectionGetter.getDirections(lastKnownLocation, currentRunToPlace.getGeoPoint());

		if (currentDirections != null) {
			assert (currentDirections.size() ==1); //Only 1 leg since we don't define any random waypoints

			showAcceptRejectPopup("Run to:\n" + currentRunToPlace.getName() + "?", 
				"Place: " + currentRunToPlace.getName() + "\n" +
				"Distance: " + currentDirections.get(0).getDistanceString() + " / " + currentDirections.get(0).getDistanceMeters() + "m" );	
		}
		else {
			showPopup("Error connecting to \nGoogle Maps", "An error occurred while connecting to Google Maps. Make sure you have an internet connection and try again."); 
		}
	}

	private GeoPoint getLastKnownLoc() {
		
		if (lastKnownLocation != null) {
			return lastKnownLocation; 
		}

		Location l= null; 

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

	private void showAcceptRejectPopup(String title, String txt) {
		AlertDialog.Builder myBuilder = new AlertDialog.Builder(this); 
		myBuilder.setMessage(txt); 
		myBuilder.setTitle(title); 
		final FunRunApplication fra = ((FunRunApplication)this.getApplicationContext());

		myBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
				fra.setRunDirections(currentDirections); 
				fra.setRunPlace(currentRunToPlace); 
				myFunRunOverlay.updateCurrentDirections(currentDirections); 
				myFunRunOverlay.updateCurrentLocation(lastKnownLocation); 
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
		popup.show(); 
	}

	private void setupStartRunningButton() {
		final Intent i = new Intent(this, FunRunActivity.class); 
		final FunRunApplication fra = ((FunRunApplication)this.getApplicationContext());

		startRunningButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (fra.getRunDirections() == null || fra.getRunPlace() == null) {
						showPopup("No Place Selected", "You must choose a place before running.\nChoose a category, then click 'Find a Place'. "); 
						return; 
					}
					startActivity(i); 
				}
			});
	}
}

