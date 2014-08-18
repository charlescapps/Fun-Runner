//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import xanthanov.droid.funrun.db.OldLeg;
import xanthanov.droid.funrun.db.OldRun;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List; 

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

public class ViewOldRunActivity extends Activity {

	private ImageButton prevLegButton; 
	private ImageButton nextLegButton;
	private Button zoomToRouteButton;
	private Button zoomInButton;
	private Button zoomOutButton;
	private TextView placeTextView; 
	private TextView pointsTextView; 

	private int runIndex; 
	private int legIndex; 
	private List<OldRun> oldRuns; 
	private OldRun run; 
	private OldRunOverlay myOldRunOverlay;
	private FunRunApplication myFunRunApp; 
	private MapView myMap; 

	private final DateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm aa"); 

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b); 
		setContentView(R.layout.view_old_run); 		

		myFunRunApp = (FunRunApplication) getApplicationContext(); 
		oldRuns = myFunRunApp.getOldRuns(); 

		Intent sourceIntent = getIntent(); 
		runIndex = sourceIntent.getIntExtra(ViewStatsActivity.RUN_INDEX_EXTRA, 0); 
		run = oldRuns.get(runIndex); 

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

	private void getViews() {
		placeTextView = (TextView) findViewById(R.id.placeTextView); 
		pointsTextView = (TextView) findViewById(R.id.pointsTextView); 
		prevLegButton = (ImageButton) findViewById(R.id.leftArrow); 
		nextLegButton = (ImageButton) findViewById(R.id.rightArrow); 
		zoomToRouteButton = (Button) findViewById(R.id.run_buttonZoomToRoute); 
		zoomInButton = (Button) findViewById(R.id.run_buttonZoomIn); 
		zoomOutButton = (Button) findViewById(R.id.run_buttonZoomOut); 
		myMap = (MapView) findViewById(R.id.oldRunMap); 
	}

	private void setupText() {
		String txt = "Run on <b>" + dateFormat.format(run.getRunDate())  + "</b><BR>"
					+ "<b><i>" + run.get(legIndex).getPlaceName() + "</b></i>";
		Spanned dateSpanned = android.text.Html.fromHtml(txt); 
		placeTextView.setText(dateSpanned); 
		pointsTextView.setText(" " + run.get(legIndex).getLegPoints()); 
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
				myOldRunOverlay.setLegIndex(legIndex); 
			}
		}); 
	
		nextLegButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				legIndex = (legIndex + 1 >= run.size() - 1 ? run.size() - 1 : legIndex + 1);

				setButtonsEnabledState(legIndex); 	

				centerOnLeg(legIndex);  
				setupText(); 
				myOldRunOverlay.setLegIndex(legIndex); 
			}
   		}); 

		setupZoomButtons(); 
		setupZoomToRouteButton(); 
	}

	private void centerOnLeg(int index) {
		zoomToRoute(run.get(index)); 

	}

	private void zoomToRoute(OldLeg currentLeg) {
		final LatLng neBound = currentLeg.getNeBound();
		final LatLng swBound = currentLeg.getSwBound();
		final LatLng midPoint = new LatLng( (neBound.latitude + swBound.latitude ) / 2.0d, (neBound.longitude + swBound.longitude ) / 2.0d);

        GoogleMap googleMap = myMap.getMap();
        if (googleMap != null) {
            CameraUpdate updatePosition = CameraUpdateFactory.newLatLng(midPoint);
            googleMap.animateCamera(updatePosition);

            LatLngBounds latLngBounds = new LatLngBounds(swBound, neBound);
            CameraUpdate zoomToSpan = CameraUpdateFactory.newLatLngBounds(latLngBounds, 0);

            googleMap.animateCamera(zoomToSpan);
        }
	}

	private void setupMap() {
		myOldRunOverlay = new OldRunOverlay(myMap, run, null);
        GoogleMap googleMap = myMap.getMap();
        if (googleMap != null) {
            myOldRunOverlay.drawRoutes(googleMap, myMap);
        }
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
                    GoogleMap googleMap = myMap.getMap();
                    if (googleMap != null) {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomIn();
                        googleMap.animateCamera(cameraUpdate);
                    }
				}
			});	
		zoomOutButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
                    GoogleMap googleMap = myMap.getMap();
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
					zoomToRoute(run.get(legIndex)); 
				}
			});	
	} 

}
