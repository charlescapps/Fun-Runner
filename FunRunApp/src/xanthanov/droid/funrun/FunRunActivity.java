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

import java.util.List;
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
	private Spinner runCategorySpinner; 
	private Button whereAmIButton; 
	private Button nextDestinationButton;
	private TextView latText;
	private TextView lngText;
	private MapView myMap;
	private Button zoomInButton;
	private Button zoomOutButton;
	//*******************OTHER OBJECTS****************************
	private MyLocationOverlay myLocOverlay;
	private MapController myMapController; 
	private LocationListener myLocListener; 
	private LocationManager myLocManager;
	private TextView emptySpinnerView; 
	private PlaceSearcher myPlaceSearcher; //Class to do HTTP request to get place data from google maps API
	private DirectionGetter myDirectionGetter; //Class to do an HTTP request to get walking directions
	private Random myRandom; 
	private int myOverlayIndex; 
	private AlertDialog popup;
	private GeoPoint lastKnownLocation; 
	private DialogInterface.OnDismissListener acceptRejectListener;
	private DialogInterface.OnCancelListener cancelListener;
	private boolean runAccepted;
	private boolean repeatQuery; 
	private List<GooglePlace> nearbyPlaces; 
	private List<GooglePlace> remainingPlaces; 
	private List<GoogleLeg> currentDirections; 
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
        setContentView(R.layout.runlayout);

		//***************GET VIEWS DEFINED IN XML***********************
		runCategorySpinner = (Spinner) findViewById(R.id.runCategorySpinner); 
		myMap = (MapView) findViewById(R.id.myMap); 
		latText = (TextView) findViewById(R.id.latText); 
		lngText = (TextView) findViewById(R.id.lngText); 
		whereAmIButton = (Button) findViewById(R.id.gpsButton); 
		nextDestinationButton = (Button) findViewById(R.id.nextDestinationButton); 
		zoomInButton = (Button) findViewById(R.id.buttonZoomIn); 
		zoomOutButton = (Button) findViewById(R.id.buttonZoomOut); 
		parentContainer = (LinearLayout) findViewById(R.id.parentContainer); 
		//********************POPUP DIALOG*******************************
		popup = null; 
		//******************DEFINE OTHER OBJECTS**************************
		myLocOverlay = new MyLocationOverlay(this, myMap); 
		myMapController = myMap.getController(); 
		myPlaceSearcher = new PlaceSearcher(this.getResources()); 
		myDirectionGetter = new DirectionGetter(); 
		myRandom = new Random(System.currentTimeMillis()); //For randomly choosing a place from list 
		myOverlayIndex = -1; 
		lastKnownLocation = null;
		runAccepted = repeatQuery = false;  
		currentDirections = null; 
		currentRunToPlace = null; 

		acceptRejectListener = new DialogInterface.OnDismissListener() {
			@Override
		    public void onDismiss(DialogInterface dia) {
				if (runAccepted) {
					startRunning(); 
				}
				else if (repeatQuery) {
					chooseRandomPlace(); 
				}
			}
		};

		cancelListener = new DialogInterface.OnCancelListener() {
			@Override
		    public void onCancel(DialogInterface dia) {
				//Set run accepted to false if user pressed Back button
				//Don't immediately repeat query so that user can choose a new category
				runAccepted = repeatQuery = false;
			}
		};
		//******************CALL SETUP METHODS****************************
		setupLocListener(); 
		setupSpinner(); 
		setupMap(); 
		setupWhereAmIButton(); 
		setupNextButton(); 
		setupZoomButtons(); 
		loadCoords(); 
    }
	
	public boolean isRouteDisplayed() {
		return true;
	}

	@Override 
	protected void onResume() {
		super.onResume();
		myLocOverlay.enableMyLocation(); 
		myLocOverlay.enableCompass(); 	
		myLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocListener);
	}
	
	@Override 
	protected void onPause() {
		super.onPause(); 
		myLocManager.removeUpdates(myLocListener); 
		myLocOverlay.disableMyLocation(); 
		myLocOverlay.disableCompass();
	}

	private void startRunning() {
		List<Overlay> overlays = myMap.getOverlays(); 

		if (myOverlayIndex == -1) {
			overlays.add(new FunRunOverlay(myMap, currentDirections)); 
			myOverlayIndex = overlays.size() - 1; 
		}
		else {
			overlays.set(myOverlayIndex, new FunRunOverlay(myMap, currentDirections)); 
		}
		
		myMap.postInvalidate();

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
			
		latText.setText(latPoint.toString()); 
		lngText.setText(lngPoint.toString()); 
	
		this.lastKnownLocation = new GeoPoint((int) (latPoint*1E6), (int) (lngPoint*1E6)); 

	}

	private void setupSpinner() {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_item);	
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		runCategorySpinner.setAdapter(adapter); 
		
		runCategorySpinner.setOnItemSelectedListener(new FunRunOnItemSelected());

		/*emptySpinnerView = new TextView(this);
		emptySpinnerView.setText("Choose a Fun Category"); 
		runCategorySpinner.setEmptyView(emptySpinnerView); 
		*/
	}

	private void setupMap() {
		myMap.getOverlays().add(myLocOverlay); 
		myMapController.setZoom(DEFAULT_ZOOM); 
		myLocOverlay.enableMyLocation(); 
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
					performPlacesQuery(); 
					chooseRandomPlace();
				}
			});	
	}

	private void performPlacesQuery() {
		System.out.println("Entering performGmapQuery()..."); 
		String search = (String)runCategorySpinner.getSelectedItem();
		GeoPoint lastLocation = getLastKnownLoc();

		System.out.println("Search:" + search + ",Last location:" + lastLocation); 

		int currentRadiusMeters = DEFAULT_RADIUS_METERS; 
 
		List<GooglePlace> foundPlaces = null; 

		//Increase the radius until something is found (or the max radius is reached)
		while ( currentRadiusMeters <= MAX_RADIUS_METERS && (foundPlaces == null || foundPlaces.size() <= 0)) {
			foundPlaces = myPlaceSearcher.getNearbyPlaces(search, lastLocation, currentRadiusMeters);
			
			if (foundPlaces == null || foundPlaces.size() == 0) {
				//Output some message and increase search radius
				currentRadiusMeters*=2; 
			}
		}
		
		if (foundPlaces == null || foundPlaces.size() <= 0) {
			//Output dialog indicating nothing was found...choose a new category
			showPopup("Choose a new category", "No '" + search + "'s found within " + MAX_RADIUS_METERS/1000 + " km.\n\n" 
					+ "Please choose a different category and try again."); 
			System.out.println("Places query: ZERO PLACES FOUND"); 
			return; 
		}
		else {
			remainingPlaces = nearbyPlaces = foundPlaces; 		
			 
		}	
		//If we successfully got the places, print them out for debugging.
		//PlaceSearcher.printListOfPlaces(foundPlaces);  

	}

	private void chooseRandomPlace() {
		//check if no places are remaining
		if (remainingPlaces.size() ==0) {
			remainingPlaces = nearbyPlaces; 
			showPopup("All nearby places of this type rejected :-(", "You rejected all nearby places of this type! Choose a new category, or click next destination to see your options again");
			return; 
		}

		//Choose a random place from list of remaining places
		currentRunToPlace = remainingPlaces.get(myRandom.nextInt(remainingPlaces.size()));			
		System.out.println("Chosen place:" + currentRunToPlace); 

		//Make another HTTP request to get directions from current location to 'runToPlace'
		currentDirections = myDirectionGetter.getDirections(lastKnownLocation, currentRunToPlace.getGeoPoint());

		assert (currentDirections.size() ==1); //Only 1 leg since we don't define any random waypoints

		showAcceptRejectPopup("Run to:\n" + currentRunToPlace.getName() + "?", 
			"Place: " + currentRunToPlace.getName() + "\n" +
			"Distance: " + currentDirections.get(0).getDistanceString() + " / " + currentDirections.get(0).getDistanceMeters() + "m" );	
		


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
		}
		return lastKnownLocation; 
	}

	private void loadCoords() {
		boolean isGpsEnabled = false; 
		try {
			isGpsEnabled = myLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER); 
			System.out.println(isGpsEnabled ? "GPS Status = ENABLED ": "GPS Staus = DISABLED"); 
		}
		catch (Exception e) {
			System.out.println("Exception of type: " + e.getClass().getName()); 
			e.printStackTrace();
			return;   
		}

		if (!isGpsEnabled) {
			System.out.println("Gps not enabled, returning..."); 
			return; 
		}
		Double latPoint=0.0; 
		Double lngPoint=0.0; 
		try {
			Location l = myLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 
			if (l==null) {
				System.out.println("Last Known Location is NULL, returning...");
				return; 
			}		
			else {
				latPoint= l.getLatitude(); 	
				lngPoint= l.getLongitude(); 	
			}
		}
		catch(Exception e) {
			System.out.println("Failed to get Last known location:"); 
			e.printStackTrace(); 
			return;
		}

		latText.setText(latPoint.toString());
		lngText.setText(lngPoint.toString());

		setupMap(latPoint, lngPoint); 
	}

	private void setupMap(Double latPt, Double lngPt) {
		GeoPoint myLocation = new GeoPoint((int) (latPt*1E6), (int) (lngPt*1E6)); 

		myMapController.setCenter(myLocation); 
		
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
				dialog.cancel();
           }
       }); 

		popup = myBuilder.create(); 
		popup.show(); 
	}

	private void showAcceptRejectPopup(String title, String txt) {
		AlertDialog.Builder myBuilder = new AlertDialog.Builder(this); 
		myBuilder.setMessage(txt); 
		myBuilder.setTitle(title); 

		myBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
				runAccepted = true; 
				repeatQuery = false; 
				dialog.dismiss();
           }
       }); 

		myBuilder.setNegativeButton("Next Place!", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
				runAccepted = false; 
				repeatQuery = true; 
				dialog.dismiss();
           }
       }); 

		popup = myBuilder.create(); 
		popup.setCancelable(true); 
		popup.setOnDismissListener(acceptRejectListener);
		popup.setOnCancelListener(cancelListener);
		popup.show(); 
	}
}

