//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.transition.Visibility;
import android.view.*;
import android.view.WindowManager.LayoutParams;
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
import xanthanov.droid.funrun.db.FunRunWriteOps;
import xanthanov.droid.funrun.mapsutils.ZoomHelper;
import xanthanov.droid.gplace.*;
import xanthanov.droid.xantools.DroidDialogs;
import xanthanov.droid.xantools.DroidLoc;
import xanthanov.droid.xantools.ProgressRunnable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

public class ChoosePlaceActivity extends Activity
{

	//***********VIEW OBJECTS DEFINED IN XML**********************
	private Spinner runCategorySpinner; 
	private Button whereAmIButton; 
	private ImageButton nextDestinationButton;
	private MapView myMap;
    private GoogleMap googleMap;
	private Button zoomInButton;
	private Button zoomOutButton;
	//*******************OTHER OBJECTS****************************
	private DroidLoc droidLoc; 
	private LocationListener myGpsListener;
	private LocationListener myNetworkListener; 
	private PlaceSearcher myPlaceSearcher; //Class to do HTTP request to get place data from google maps API
	private DirectionGetter myDirectionGetter; //Class to do an HTTP request to get walking directions
	private FunRunOverlay myFunRunOverlay; 
	private AlertDialog popup;
	private Location bestLocation; 
	private LatLng lastKnownLatLng;
	private LatLng firstGpsFix;
	//Places found, directions found, etc.
	private List<GooglePlace> nearbyPlaces;
	private List<GooglePlace> remainingPlaces;
	private GoogleDirections currentDirections; 
	private GooglePlace currentRunToPlace;
	private GoogleLeg tempLeg; 
	private FunRunApplication funRunApp; 
	private FunRunWriteOps dbWriter; 
	private boolean dbWriteSuccess; 
	private EditText customSearchText; 
	private String CUSTOM_SEARCH_STRING = "Name of place"; 
	//*****************CONSTANTS**********************************
	private final static int DEFAULT_ZOOM = 15; 
	private int SEARCH_RADIUS_METERS = 1000;
	private final static int CUSTOM_SEARCH_POPUP_ID = 0; 
	//************************************************************
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectlayout);

		customSearchText = new EditText(this); 

		//***************GET VIEWS DEFINED IN XML***********************
		runCategorySpinner = (Spinner) findViewById(R.id.runCategorySpinner); 
		myMap = (MapView) findViewById(R.id.myMap);
        // Forward onCreate to the MapView
        myMap.onCreate(savedInstanceState);
        googleMap = getGoogleMap();
		whereAmIButton = (Button) findViewById(R.id.gpsButton); 
		nextDestinationButton = (ImageButton) findViewById(R.id.nextDestinationButton); 
		zoomInButton = (Button) findViewById(R.id.buttonZoomIn); 
		zoomOutButton = (Button) findViewById(R.id.buttonZoomOut); 
		//******************DEFINE OTHER OBJECTS**************************
		droidLoc = new DroidLoc(this); 
		myPlaceSearcher = new PlaceSearcher(this.getResources());
		myDirectionGetter = new DirectionGetter(); 

		bestLocation = droidLoc.getBestLocation(null); 
		if (bestLocation != null) {
			lastKnownLatLng = DroidLoc.locationToLatLng(bestLocation);
		}

		//System.out.println("Last location: " + lastKnownLatLng);
		funRunApp = (FunRunApplication) getApplicationContext();

		currentDirections = new GoogleDirections(); //Create new directions now, since they correspond to a run
		funRunApp.setRunDirections(currentDirections); //Store the current directions object with the application 

		//Instantiate a new object for writing data to the database, and store it in the Application object to pass between activities
		this.dbWriter = new FunRunWriteOps(funRunApp); 
		funRunApp.setDbWriter(this.dbWriter); 

		//Write new run to DB
		dbWriteSuccess = false; 

		try {//Insert new run id for this run
			dbWriteSuccess = dbWriter.insertNewRun(currentDirections); 
		}
		catch (java.sql.SQLException e) {
			//System.err.println("Error inserting new run into DB."); 
			//e.printStackTrace(); 
			dbWriteSuccess = false; 
		}

		if (!dbWriteSuccess) {
			DroidDialogs.showPopup(this, "Critical Error", "Failed to add new run to database.\nTry restarting the app.", 
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface diag, int id) {
											ChoosePlaceActivity.this.finish(); 
										}
									}); 
		} 

		//******************CALL SETUP METHODS****************************
		setupLocListener(); 
		setupSpinner(); 
		setupMap(); 
		setupWhereAmIButton(); 
		setupNextButton(); 
		setupZoomButtons(); 
		centerOnMe(); 

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
        getGoogleMap();
    }

    @Override
    public void onPause() {
        super.onPause();
        myMap.onPause();
    }

	@Override	
	public boolean onKeyDown( int keycode, KeyEvent e) {		
		//super.onKeyDown(keycode, e); 

		if (keycode == KeyEvent.KEYCODE_BACK) {
			endActivity(); 
			return true;
		}
		return false; 

	}

	public void endActivity() {
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

	}

	private void grabPrefs() {
		Resources res = getResources(); 

		SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this); 

		String default_search_radius = res.getString(R.string.default_search_radius); 
		String search_radius_key = res.getString(R.string.search_radius_pref); 
		SEARCH_RADIUS_METERS = Integer.parseInt(prefs.getString(search_radius_key, default_search_radius)); 

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus); 

		if (hasFocus) {
			myMap.invalidate(); 
		}

	}

	@Override
	public void onStart() {
		super.onStart(); 

		grabPrefs(); //Get default search radius preference

		checkGps(); //Output error dialog if GPS is disabled 

		bestLocation = droidLoc.getBestLocation(bestLocation); //Get immediate location
		lastKnownLatLng = DroidLoc.locationToLatLng(bestLocation); //Convert to GeoPoint immediately
		myFunRunOverlay.updateCurrentLocation(lastKnownLatLng); //Update location in overlay
        GoogleMap googleMap = getGoogleMap();
        if (googleMap != null) {
            myFunRunOverlay.drawOverlays(googleMap);
        }
		centerOnMe(); 
		myMap.invalidate(); //Redraw

		//Start GPS and Network updates
		droidLoc.getLocManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, FunRunApplication.MIN_GPS_UPDATE_TIME_MS, 0, myGpsListener);
		droidLoc.getLocManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, FunRunApplication.MIN_GPS_UPDATE_TIME_MS, 0, myNetworkListener);
	}

	@Override
	public void onStop() {
		super.onStop(); 
		droidLoc.getLocManager().removeUpdates(myGpsListener); 
		droidLoc.getLocManager().removeUpdates(myNetworkListener); 
		myFunRunOverlay.setSpecificLeg(null);
	}

	@Override 
	protected void onDestroy() {
		super.onDestroy(); 

		if (currentDirections != null) {
			//System.out.println("Distance so far for current directions when leaving ChoosePlace: " + currentDirections.getDistanceSoFar()); 
		}
		else {
			//System.out.println("Directions null upon leaving ChoosePlaceActivity."); 
		}

		if (currentDirections.size() <= 0) { //If we didn't actually add any legs, delete residual crap from database
			try {
				dbWriter.deleteRun();
			}
			catch (SQLException e) {
				//System.err.println("Error deleting empty run in ChoosePlaceActivity.onDestroy() "); 
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

		myGpsListener = new MyLocListener(); 
		myNetworkListener = new MyLocListener(); 

	}

	private void updateLocation(Location l) {
		if (l==null) {
			return;
		}
		boolean wasNull = (firstGpsFix == null); 

		bestLocation = droidLoc.compareLocations(bestLocation, l); //Compare new location to previous best, and return the best one

		LatLng newLatLng = DroidLoc.locationToLatLng(bestLocation);

		lastKnownLatLng = newLatLng;

		myFunRunOverlay.updateCurrentLocation(lastKnownLatLng);

        GoogleMap googleMap = getGoogleMap();

        if (googleMap != null) {
            myFunRunOverlay.drawOverlays(googleMap);
        }

		if (wasNull) {
			firstGpsFix = newLatLng;
            centerOnMe();
        }

    }

	private void setupSpinner() {

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_array, R.layout.normal_spinner);	
		adapter.setDropDownViewResource(R.layout.spinner_dropdown);
		runCategorySpinner.setAdapter(adapter); 
		runCategorySpinner.setSelection(0); 

		//runCategorySpinner.setOnItemSelectedListener(new FunRunOnItemSelected());
	}

	private void setupMap() {
		myFunRunOverlay = new FunRunOverlay(myMap, null, false, true, false, null);
		myFunRunOverlay.updateCurrentLocation(lastKnownLatLng);
		myFunRunOverlay.updateCurrentDirections(currentDirections); 
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

        if (lastKnownLatLng == null) {
            return;
        }
        else {
            GoogleMap googleMap = getGoogleMap();

            if (googleMap == null) {
                return;
            }
            final float zoomLevel = ZoomHelper.getZoomLevel(0.8f, googleMap);
            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(lastKnownLatLng)
                    .zoom(zoomLevel)
                    .build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            googleMap.animateCamera(cameraUpdate);
            myFunRunOverlay.updateCurrentLocation(lastKnownLatLng);
            myFunRunOverlay.drawOverlays(googleMap);
        }
	}

	private void setupNextButton() {
		nextDestinationButton.setOnClickListener(new OnClickNext(this));
	}

	private List<GooglePlace> performPlacesQuery(String search, LatLng lastLocation, int currentRadiusMeters) throws Exception {

		List<GooglePlace> foundPlaces = null; 

		//START LOADING DIALOG
		ProgressRunnable pr = DroidDialogs.showProgressDialog(this, "", "Downloading Google places...");  
		
		try {
			//Just search with constant radius. Reduce number of queries
			foundPlaces = myPlaceSearcher.getNearbyPlaces(search, lastLocation, SEARCH_RADIUS_METERS); 
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
			//System.out.println("No nearby places found in getNextPlace\n"); 
			return; 
		}
		
		//check if no places are remaining
		if (remainingPlaces.size() == 0) {
			remainingPlaces = new ArrayList<GooglePlace>(nearbyPlaces); 
			DroidDialogs.showPopup(this, "All places rejected :-(", "You rejected all nearby places of this type!\nChoose a new category, or touch 'Find Nearby Places' to see your options again.");
            showButtons();
			currentRunToPlace = null; 
			myFunRunOverlay.setSpecificLeg(null); 
			centerOnMe(); 
			return; 
		}

		//Choose a "random" place from list of remaining places, actually getting closest remaining place
		currentRunToPlace = remainingPlaces.get(0);			
		remainingPlaces.remove(currentRunToPlace); 
		//System.out.println("Chosen place:" + currentRunToPlace); 

		//Make another HTTP request to get directions from current location to 'runToPlace'
		bestLocation = droidLoc.getBestLocation(bestLocation); 
		lastKnownLatLng = new LatLng(bestLocation.getLatitude(), bestLocation.getLongitude()); //Get most recent location before getting directions
		myFunRunOverlay.updateCurrentLocation(lastKnownLatLng);
        GoogleMap googleMap = getGoogleMap();
        if (googleMap != null) {
            myFunRunOverlay.drawOverlays(googleMap);
        }

		new DirectionsQueryTask(this).execute(lastKnownLatLng, currentRunToPlace.getLatLng());

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

    private void hideButtons() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Animation animation = new AlphaAnimation(0.7f, 0.0f);
                animation.setFillAfter(true);
                if (zoomInButton != null) {
                    zoomInButton.startAnimation(animation);
                }
                if (zoomOutButton != null) {
                    zoomOutButton.startAnimation(animation);
                }
                if (whereAmIButton != null) {
                    whereAmIButton.startAnimation(animation);
                }
            }
        });
    }

    private void showButtons() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Animation animation = new AlphaAnimation(0.0f, 0.7f);
                animation.setFillAfter(true);
                if (zoomInButton != null) {
                    zoomInButton.startAnimation(animation);
                }
                if (zoomOutButton != null) {
                    zoomOutButton.startAnimation(animation);
                }
                if (whereAmIButton != null) {
                    whereAmIButton.startAnimation(animation);
                }
            }
        });

    }

	private void showAcceptRejectPopup(String title, String txt) {
        hideButtons();
		AlertDialog.Builder myBuilder = new AlertDialog.Builder(this); 
		myBuilder.setMessage(txt); 
		myBuilder.setTitle(title); 

		myBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
                showButtons();
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

		popup.setOnCancelListener(new AlertDialog.OnCancelListener() {  //Clear the route being drawn when you cancel the dialogue
			@Override
			public void onCancel(DialogInterface d) {
				myFunRunOverlay.setSpecificLeg(null);
                showButtons();
				centerOnMe(); 
			}
		}); 

		LayoutParams WMLP = popup.getWindow().getAttributes();
		//LayoutParams WMLP = new LayoutParams(LayoutParams.TYPE_APPLICATION_PANEL, ;

		//System.out.println(WMLP.toString()); 
		WMLP.gravity = android.view.Gravity.TOP; 
		WMLP.verticalMargin = .02f;
		WMLP.dimAmount = 0.0f; 

		popup.getWindow().setAttributes(WMLP);
		popup.getWindow().clearFlags(LayoutParams.FLAG_DIM_BEHIND); 

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
        showButtons();

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

			if (lastKnownLatLng == null) {
				DroidDialogs.showPopup(a, "No location found.", "No location found. Turn on GPS and go outside, then try again."); 
				return null; 
			}

			try {
				result = performPlacesQuery(searchStr, lastKnownLatLng, SEARCH_RADIUS_METERS );
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
			if (nearbyPlaces != null) {
				java.util.Collections.sort(nearbyPlaces, new PlaceComparator(lastKnownLatLng));
			}
			//Set remainingPlaces = shallow copy of nearbyPlaces  
			remainingPlaces = (nearbyPlaces == null ? null : new ArrayList(nearbyPlaces)); 

			if (result == null) {
				; //Places query should show a popup indicating you don't have an internet connection or some other horrible error occurred
			}
			else if (result.size() <= 0) {
				//Output dialog indicating nothing was found...choose a new category
				DroidDialogs.showPopup(a, "Choose a new category", "No '" + searchStr + "'s found within " + SEARCH_RADIUS_METERS + " meters.\n\n" 
						+ "Please choose a different category and try again."); 
				//System.err.println("Places query: ZERO PLACES FOUND"); 
				
				return; 
			}
			else {
			//If we successfully got the places, print them out for debugging.
			//AND sort them by which is closer...
			//To avoid getting directions for all of them, do the crude thing of getting "as the bird flies" distance
				//PlaceSearcher.printListOfPlaces(nearbyPlaces);  
			}
			//Call the dialog to cycle through places
			getNextPlace();
		}
	}

	private class DirectionsQueryTask extends AsyncTask<LatLng, Integer, GoogleLeg> {
		private Activity a = null;

		public DirectionsQueryTask(Activity a) {
			super(); 
			this.a = a; 
		}

		protected GoogleLeg doInBackground(LatLng... directionPoints) {
			if (directionPoints.length != 2) {
				return null; 
			}

			//START LOADING DIALOG
			ProgressRunnable pr = DroidDialogs.showProgressDialog(a, "", "Grabbing Google walking directions...");  
			GoogleLeg result = null; 

			try {
				result = myDirectionGetter.getDirections(lastKnownLatLng, currentRunToPlace.getLatLng());
			}
			catch (Exception e) { //Dialog will popup due to null result. No need to deal with exception here
				e.printStackTrace();
			}
			finally {
				pr.dismissDialog(); 
			}
	
			return result; 
		}

		protected void onPostExecute(GoogleLeg result) {
			tempLeg = result; 

			if (tempLeg != null) {

				tempLeg.setLegDestination(currentRunToPlace); 
				myFunRunOverlay.setSpecificLeg(tempLeg);
                GoogleMap googleMap = getGoogleMap();
                if (googleMap != null) {
                    myFunRunOverlay.drawOverlays(googleMap);
                }
				zoomToTempRoute(); 
				myMap.invalidate(); 
				
				showAcceptRejectPopup("Run to:\n" + currentRunToPlace.getName() + "?", 
					"Place: " + currentRunToPlace.getName() + "\n" +
					"Distance: " + tempLeg.getDistanceString() + " / " + tempLeg.getDistanceMeters() + "m" );	
			}
			else {
				DroidDialogs.showPopup(a, "Error connecting to \nGoogle Maps", "An error occurred while connecting to Google Maps. Make sure you have an internet connection and try again."); 
				myFunRunOverlay.setSpecificLeg(null);
                showButtons();
				centerOnMe(); 
			}
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
														String spinnerSelection = runCategorySpinner.getSelectedItem().toString(); 
														if (spinnerSelection.equals(getString(R.string.custom_search))) {
															customSearchText.selectAll(); 
															showDialog(CUSTOM_SEARCH_POPUP_ID); 
														}
														else {
															startPlacesQuery();
														} 
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
				String spinnerSelection = runCategorySpinner.getSelectedItem().toString(); 
				if (spinnerSelection.equals(getString(R.string.custom_search))) {
					customSearchText.selectAll(); 
					showDialog(CUSTOM_SEARCH_POPUP_ID); 
				}
				else {
					startPlacesQuery();
				} 
			}

		}

	}

	private void startPlacesQuery() {
		String search = (runCategorySpinner.getSelectedItem()).toString();
		if (search.equals(getString(R.string.custom_search))) {
			search = CUSTOM_SEARCH_STRING; 
		}
		new PlacesQueryTask(this).execute(search); 	
	}

	private void zoomToTempRoute() {
		if (tempLeg == null) {
			return; 
		}
		final LatLng neBound = DroidLoc.degreesToLatLng(tempLeg.getNeBound());
		final LatLng swBound = DroidLoc.degreesToLatLng(tempLeg.getSwBound());
		final LatLng midPoint = new LatLng( (neBound.latitude + swBound.latitude) / 2.0d, (neBound.longitude + swBound.longitude) / 2.0d);

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
			ChoosePlaceActivity.this.updateLocation(l); 
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
			this.endActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this); 
		switch(id) {
			case CUSTOM_SEARCH_POPUP_ID:
				// do the work to define the email Dialog
				customSearchText.setText(CUSTOM_SEARCH_STRING, TextView.BufferType.EDITABLE); 
				customSearchText.selectAll(); 

				builder.setTitle("Enter name to search for:"); 
				builder.setView(customSearchText); 
				builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String text = customSearchText.getText().toString(); 
						CUSTOM_SEARCH_STRING = text; 
						ChoosePlaceActivity.this.startPlacesQuery(); 

					}
				}); 

				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss(); 
					}

				}); 

				dialog= builder.create(); 

				break;

			default: 
				dialog = null; 
				break; 

		} 
		return dialog;
	}
	
}

