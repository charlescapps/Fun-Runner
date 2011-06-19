package xanthanov.droid.funrun;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button; 
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.ArrayAdapter;
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

public class RunActivity extends MapActivity
{

	//***********VIEW OBJECTS DEFINED IN XML**********************
	private Spinner runCategorySpinner; 
	private Button gpsButton; 
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
	private PlaceSearcher myPlaceSearcher; //Class to grab data from google maps API
	private Random myRandom; 
	//*****************CONSTANTS**********************************
	private final static int DEFAULT_ZOOM = 15; 
	private final static int DEFAULT_RADIUS_METERS = 500;
	private final static int MAX_RADIUS_METERS = 4000; 
	//************************************************************
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.runlayout);

		//***************GET VIEWS DEFINED IN XML******************
		runCategorySpinner = (Spinner) findViewById(R.id.runCategorySpinner); 
		myMap = (MapView) findViewById(R.id.myMap); 
		latText = (TextView) findViewById(R.id.latText); 
		lngText = (TextView) findViewById(R.id.lngText); 
		gpsButton = (Button) findViewById(R.id.gpsButton); 
		nextDestinationButton = (Button) findViewById(R.id.nextDestinationButton); 
		zoomInButton = (Button) findViewById(R.id.buttonZoomIn); 
		zoomOutButton = (Button) findViewById(R.id.buttonZoomOut); 
		//******************DEFINE OTHER OBJECTS**************************
		myLocOverlay = new MyLocationOverlay(this, myMap); 
		myMapController = myMap.getController(); 
		myPlaceSearcher = new PlaceSearcher(); 
		myRandom = new Random(System.currentTimeMillis()); //For randomly choosing a place from list 
		//******************CALL SETUP METHODS****************************
		setupLocListener(); 
		setupSpinner(); 
		setupMap(); 
		setupGpsButton(); 
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
	}
	
	@Override 
	protected void onPause() {
		super.onPause(); 
		myLocManager.removeUpdates(myLocListener); 
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
	
		setupMap(latPoint, lngPoint); 
	}

	private void setupSpinner() {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_item);	
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		runCategorySpinner.setAdapter(adapter); 
		
		runCategorySpinner.setOnItemSelectedListener(new FunRunOnItemSelected());

		emptySpinnerView = new TextView(this);
		emptySpinnerView.setText("Choose a Fun Category"); 
		runCategorySpinner.setEmptyView(emptySpinnerView); 
	}

	private void setupMap() {
		myMap.getOverlays().add(myLocOverlay); 
		myMapController.setZoom(DEFAULT_ZOOM); 
	}

	private void setupGpsButton() {
		gpsButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					loadCoords(); 
				}
			});	
	}

	private void setupNextButton() {
		
		nextDestinationButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					performGmapQuery(); 
				}
			});	
	}

	private void performGmapQuery() {
		String search = (String)runCategorySpinner.getSelectedItem();
		GeoPoint lastLocation = getLastKnownLoc();

		int currentRadiusMeters = DEFAULT_RADIUS_METERS; 
 
		List<GooglePlace> foundPlaces = null; 

		while ( currentRadiusMeters < MAX_RADIUS_METERS && (foundPlaces == null || foundPlaces.size() == 0)) {
			foundPlaces = myPlaceSearcher.getNearbyPlaces(search, lastLocation, currentRadiusMeters);
			printListOfPlaces(foundPlaces);  
			
			
			if (foundPlaces == null || foundPlaces.size() == 0) {
				//Output some message and increase search radius
				currentRadiusMeters*=2; 
			}
		}

		
		if (foundPlaces == null || foundPlaces.size() == 0) {
			//Output dialog indicating nothing was found...choose a new category
		}

		//Choose a random place from list
		GooglePlace runToPlace = foundPlaces.get(myRandom.nextInt(foundPlaces.size()));			

		//Figure out how to get directions (straightforward) and how to put them on map (not sure)
	}

	private GeoPoint getLastKnownLoc() {
		Location l= null; 

		try { 
			l = myLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 	
		}
		catch (Exception e) {
			System.out.println("Failure getting last known location."); 
			return null; 
		}

		return new GeoPoint((int) (l.getLatitude()*1E6), (int) (l.getLongitude()*1E6)); 
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

	private static void printListOfPlaces(List<GooglePlace> places) {
		for (GooglePlace gp: places) {
			System.out.println(gp); 
		}
	}

}
