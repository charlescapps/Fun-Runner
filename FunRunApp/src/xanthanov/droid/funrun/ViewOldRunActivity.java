//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun; 

import xanthanov.droid.funrun.persist.FunRunData; 
import xanthanov.droid.gplace.*; 
import xanthanov.droid.xantools.DroidLoc; 

import android.view.animation.Animation; 
import android.view.animation.AlphaAnimation;

import com.google.android.maps.MapView; 
import com.google.android.maps.MapActivity; 
import com.google.android.maps.GeoPoint; 
import com.google.android.maps.MapController; 

import android.app.Activity; 
import android.os.Bundle; 

import android.content.Intent; 
import android.widget.Button; 
import android.widget.ImageButton; 
import android.widget.TextView; 
import android.view.View; 
import android.text.Spanned; 

import java.text.SimpleDateFormat; 
import java.text.DateFormat; 

/**
*<h3>Activity for viewing an old run on a MapView showing your route</h3>
*
*<b>Things this class does:</b>
*<ul>
*<li>Cycle through legs of your run, displaying them on map.</li>
*<li>Called from ViewStatsActivity</li>
*<li>TODO: add some functionality, such as maybe displaying distance on the map.</li>
*</ul>
*
*@author Charles L. Capps
*@version 0.9b
**/

public class ViewOldRunActivity extends MapActivity {

	private ImageButton prevLegButton; 
	private ImageButton nextLegButton;
	private Button zoomToRouteButton;
	private Button zoomInButton;
	private Button zoomOutButton;
	private TextView placeTextView; 

	private int runIndex; 
	private int legIndex; 
	private FunRunData state; 
	private GoogleDirections run; 
	private MapController myMapController; 
	private FunRunOverlay myFunRunOverlay; 
	private FunRunApplication myFunRunApp; 
	private MapView myMap; 

	private final DateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm aa"); 

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b); 
		setContentView(R.layout.view_old_run); 		

		myFunRunApp = (FunRunApplication) getApplicationContext(); 
		state = myFunRunApp.getState(); 

		Intent sourceIntent = getIntent(); 
		runIndex = sourceIntent.getIntExtra(ViewStatsActivity.RUN_INDEX_EXTRA, 0); 
		run = state.get(runIndex); 

		legIndex = 0; 

		getViews(); 
		setupButtons(); 
		setButtonsEnabledState(legIndex); 

		centerOnLeg(legIndex); 

		setupMap();  
		setupText(); 
			
	}

	@Override
	public void onStop() {
		super.onStop();
	
	}
	
	@Override
	public boolean isRouteDisplayed() {
		return true;
	}

	private void getViews() {
		placeTextView = (TextView) findViewById(R.id.placeTextView); 
		prevLegButton = (ImageButton) findViewById(R.id.leftArrow); 
		nextLegButton = (ImageButton) findViewById(R.id.rightArrow); 
		zoomToRouteButton = (Button) findViewById(R.id.run_buttonZoomToRoute); 
		zoomInButton = (Button) findViewById(R.id.run_buttonZoomIn); 
		zoomOutButton = (Button) findViewById(R.id.run_buttonZoomOut); 
		myMap = (MapView) findViewById(R.id.oldRunMap); 
		myMapController = myMap.getController(); 
	}

	private void setupText() {
		String txt = "Run on <b>" + dateFormat.format(run.getDate())  + "</b><BR>"
					+ "<b><i>" + run.get(legIndex).getLegDestination().getName() + "</b></i>";
		Spanned dateSpanned = android.text.Html.fromHtml(txt); 
		placeTextView.setText(dateSpanned); 
	}

	private void setButtonsEnabledState(int position) {
		if (position <= 0) {
			prevLegButton.setEnabled(false); 
		}
		else {
			prevLegButton.setEnabled(true); 
		}

		if (position >= run.size() - 1) {
			nextLegButton.setEnabled(false); 
		}
		else {
			nextLegButton.setEnabled(true); 
		}
		
	}

	private void setupButtons() {
		prevLegButton.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				legIndex = (legIndex - 1 <= 0 ? 0 : legIndex - 1);

				setButtonsEnabledState(legIndex); 	
				centerOnLeg(legIndex);  
				setupText(); 
				myFunRunOverlay.setSpecificLeg(run.get(legIndex)); 
			}
		}); 
	
		nextLegButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				legIndex = (legIndex + 1 >= run.size() - 1 ? run.size() - 1 : legIndex + 1);

				setButtonsEnabledState(legIndex); 	

				centerOnLeg(legIndex);  
				setupText(); 
				myFunRunOverlay.setSpecificLeg(run.get(legIndex)); 
			}
   		}); 

		setupZoomButtons(); 
		setupZoomToRouteButton(); 
	}

	private void centerOnLeg(int index) {
		zoomToRoute(run.get(index)); 

	}

	private void zoomToRoute(GoogleLeg currentLeg) {
		final GeoPoint neBound = DroidLoc.degreesToGeoPoint(currentLeg.getNeBound()); 
		final GeoPoint swBound = DroidLoc.degreesToGeoPoint(currentLeg.getSwBound()); 
		final GeoPoint midPoint = new GeoPoint( (neBound.getLatitudeE6() + swBound.getLatitudeE6())/2, (neBound.getLongitudeE6() + swBound.getLongitudeE6())/2);
		final int latSpan = Math.abs(neBound.getLatitudeE6() - swBound.getLatitudeE6()); 
		final int lngSpan = Math.abs(neBound.getLongitudeE6() - swBound.getLongitudeE6()); 

		myMapController.animateTo(midPoint); 
		myMapController.zoomToSpan(latSpan, lngSpan); 
	}

	private void setupMap() {
		myFunRunOverlay = new FunRunOverlay(myMap, null, true, true, false, null);
		myFunRunOverlay.updateCurrentDirections(run); 
		myFunRunOverlay.setSpecificLeg(run.get(legIndex)); 
		myMap.getOverlays().add(myFunRunOverlay); 
		myMap.preLoad(); 
		myMap.postInvalidate(); 
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
					zoomToRoute(run.get(legIndex)); 
				}
			});	
	} 

}
