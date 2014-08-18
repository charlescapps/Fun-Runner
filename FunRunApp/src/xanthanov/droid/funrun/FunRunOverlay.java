//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun;

import android.graphics.*;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.*;
import xanthanov.droid.gplace.GoogleDirections;
import xanthanov.droid.gplace.GoogleLeg;
import xanthanov.droid.xantools.DroidLoc;

import java.util.List;

/**
*
* <h3>Overlay class for drawing on top of MapView</h3>
* This class is used in every MapActivity class to draw various things on top of the map. <br/>
* Flags passed through the constructor determine what it draws. 
*<h3>Things this class draws:</h3>
* <ul>
*<li>The Google walking directions route of current leg. Obtained via HTTP request in ChoosePlaceActivity. (<b>in red</b>)</li>
*<li>The route the user actually ran for the current leg. (<b>in blue</b>)</li>
*<li>The route the user actually ran on previous legs. (<b>in green</b>)</li>
*<li>The runner animation. Uses an ImageView object holding the animation. AFAIK this is the only way to do it without using OpenGL. </li>
*<li>The start point (to be an animation)</li>
*<li>A trophy icon for the place the user is running to.</li>
*</ul>
*
*@author Charles L. Capps
*
*@version 0.9b
**/

public class FunRunOverlay {
	
	//********************CONSTANTS***************************
	private final static float STROKE_WIDTH = 4.0f;

	private final static Point RUNNER_OFFSET = new Point(15,40);

    private Marker routeStartMarker;
    private Marker routeFinishMarker;
    private Marker runnerMarker;
    private Polyline routeLine;

	private MapView theMapView = null;
	private Paint pathPaint = null;
	private GoogleDirections directions = null;
	private GoogleLeg specificLeg = null; 
	private boolean drawRoute = false; 
	private boolean drawSpecificRoute = false; 
	
	private Bitmap START;
    private BitmapDescriptor START_DESCRIPTOR;
	private Bitmap DESTINATION1;
    private BitmapDescriptor DESTINATION1_DESCRIPTOR;
	private Bitmap DESTINATION2;
    private BitmapDescriptor DESTINATION2_DESCRIPTOR;
	private Bitmap FLAG;
    private BitmapDescriptor FLAG_DESCRIPTOR;

    private Bitmap RUNNER1;
    private BitmapDescriptor RUNNER1_DESCRIPTOR;

	private boolean animateRunner; 
	private ImageView runnerImageView; 
	private LatLng prevRunnerPoint;
	private LatLng curRunnerLatLng;
	private RelativeLayout mapRelLayout; 

	public FunRunOverlay(MapView map, GoogleDirections directions, boolean drawRoute, boolean drawSpecificRoute, boolean animateRunner, RelativeLayout mapRelLayout) {
		this.theMapView = map;
		this.directions = directions;
		this.drawRoute = drawRoute; 
		this.drawSpecificRoute = drawSpecificRoute; 
		this.pathPaint = new Paint();
		this.animateRunner = animateRunner; 

		prevRunnerPoint = curRunnerLatLng = null;

		this.mapRelLayout = mapRelLayout; 

		if (animateRunner && mapRelLayout != null) {

			runnerImageView = new ImageView(map.getContext()); 
			runnerImageView.setBackgroundResource(R.drawable.runner_anim);
			runnerImageView.setAdjustViewBounds(true); 
			mapRelLayout.addView(runnerImageView); 

		}

		if (directions != null) {
			this.curRunnerLatLng = DroidLoc.degreesToLatLng(directions.getFirstPoint());
			curRunnerLatLng = prevRunnerPoint = curRunnerLatLng;
		}

		//Paint settings
		this.pathPaint.setAntiAlias(true);
		
		START = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.start); 
		DESTINATION1 = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.destination_icon1); 
		DESTINATION2 = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.destination_icon2); 

		RUNNER1 = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.runner1); 

		FLAG = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.chequered_flag_icon);

        MapsInitializer.initialize(map.getContext());
        START_DESCRIPTOR = BitmapDescriptorFactory.fromBitmap(START);

        DESTINATION1_DESCRIPTOR = BitmapDescriptorFactory.fromBitmap(DESTINATION1);
        DESTINATION2_DESCRIPTOR = BitmapDescriptorFactory.fromBitmap(DESTINATION2);

        RUNNER1_DESCRIPTOR = BitmapDescriptorFactory.fromBitmap(RUNNER1);
        FLAG_DESCRIPTOR = BitmapDescriptorFactory.fromBitmap(FLAG);

	}

	public void startRunAnimation() {
		AnimationDrawable runnerAnim = (AnimationDrawable)runnerImageView.getBackground(); 
		runnerAnim.start(); 
	}

	public void stopRunAnimation() {
		AnimationDrawable runnerAnim = (AnimationDrawable)runnerImageView.getBackground(); 
		runnerAnim.stop(); 
	}

	public void setSpecificLeg(GoogleLeg l) {this.specificLeg = l; }

	public void updateCurrentLocation(LatLng loc) {
		prevRunnerPoint = curRunnerLatLng;
		curRunnerLatLng = loc;

		theMapView.postInvalidate(); 
	}	

	public void updateCurrentDirections(GoogleDirections directions) { this.directions = directions;}

	public void drawOverlays(GoogleMap googleMap) {
        //googleMap.clear();

		if (drawSpecificRoute) { //Case for ChoosePlaceActivity. Just drawing the directions with start dot at beginning, flag at end
			if (specificLeg != null && specificLeg.size() > 0) {
				List<LatLng> directionsPath = specificLeg.getPathPoints();
				drawAPath(directionsPath, googleMap, STROKE_WIDTH, Color.RED);

				LatLng startLatLng = directionsPath.get(0); //First point of directions
				LatLng endLatLng = directionsPath.get(directionsPath.size() - 1); //Last point of directions

                routeStartMarker = drawStartMarker(startLatLng, googleMap);
                routeFinishMarker = drawFinishMarker(endLatLng, googleMap);
			}
		

		}

		else { //Case for FunRunActivity. Draw the newest leg directions, and the corresponding actual path ran
			if (directions!= null && directions.size() > 0) { //Stuff you can only draw if you have some directions

				GoogleLeg legToDraw = directions.lastLeg(); 

				//Draw the current path you've ran for the leg in ACTUAL_COLOR
				drawAPath(legToDraw.getActualPath(), googleMap, STROKE_WIDTH, Color.BLACK);

				List<LatLng> directionsPath = legToDraw.getPathPoints();

				if (directionsPath != null && directionsPath.size() > 0) {
					//Draw the directions for the appropriate leg in ROUTE_COLOR
					drawAPath(directionsPath, googleMap, STROKE_WIDTH, Color.BLUE);
					//Draw the little red circle at the start point on top of path
					LatLng startPoint = directionsPath.get(0); //First point of run
                    LatLng endPoint = directionsPath.get(directionsPath.size() - 1);  //Last point of the leg

                    routeStartMarker = drawStartMarker(startPoint, googleMap);
                    routeFinishMarker = drawFinishMarker(endPoint, googleMap);

				}
			}
		}

		//Draw stick guy at current location even if no directions exist
		
		if (curRunnerLatLng != null) {

	/*		if (animateRunner) {
				TranslateAnimation ta = new TranslateAnimation((int)runnerPoint.x - RUNNER_OFFSET.x, (int)runnerPoint.x - RUNNER_OFFSET.x, (int)runnerPoint.y - RUNNER_OFFSET.y, (int)runnerPoint.y - RUNNER_OFFSET.y); 
				runnerImageView.startAnimation(ta); 
			}
			else {*/

                runnerMarker = drawRunner(curRunnerLatLng, googleMap);
		//	}
		}
	}

    private Marker drawStartMarker(LatLng startLatLng, GoogleMap googleMap) {
        if (routeStartMarker == null) {

            MarkerOptions startBitmapOptions = new MarkerOptions()
                    .icon(START_DESCRIPTOR)
                    .position(startLatLng)
                    .anchor(0.5f, 0.5f);

            //Draw start dot
            return googleMap.addMarker(startBitmapOptions);
        }
        routeStartMarker.setPosition(startLatLng);
        return routeStartMarker;
    }

    private Marker drawFinishMarker(LatLng endLatLng, GoogleMap googleMap) {
        if (routeFinishMarker == null) {
            MarkerOptions flagBitmapOptions = new MarkerOptions()
                    .icon(FLAG_DESCRIPTOR)
                    .position(endLatLng)
                    .anchor(0.0f, 1.0f);

            return googleMap.addMarker(flagBitmapOptions);
        }
        routeFinishMarker.setPosition(endLatLng);
        return routeFinishMarker;
    }

    private Marker drawRunner(LatLng runnerLatLng, GoogleMap googleMap) {
        if (runnerMarker == null) {
            MarkerOptions runnerMarker = new MarkerOptions()
                    .icon(RUNNER1_DESCRIPTOR)
                    .position(runnerLatLng)
                    .anchor(0.5f, 0.5f);

            return googleMap.addMarker(runnerMarker);
        }
        runnerMarker.setPosition(runnerLatLng);
        return runnerMarker;
    }

	private void drawAPath(List<LatLng> path, GoogleMap googleMap, float strokeWidth, int color) {

		if(path == null || path.size() == 0) {
			return;
		}

        if (routeLine != null) {
            routeLine.remove();
        }

		//**********************SET UP PATH***************************
        LatLng startLatLng = path.get(0);

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(startLatLng);

        polylineOptions.color(color);
        polylineOptions.width(strokeWidth);
        polylineOptions.visible(true);

		//Loop through all GeoPoints
		for (LatLng current : path) {
			//Convert GeoPoint to pixels and add to path
			if(current != null){
                polylineOptions.add(current);
			}
		}

        routeLine = googleMap.addPolyline(polylineOptions);
	}
	
}
