//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun;

import android.graphics.*;
import android.graphics.Paint.Style;
import android.widget.RelativeLayout;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.*;
import xanthanov.droid.funrun.db.OldLeg;
import xanthanov.droid.funrun.db.OldRun;

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

public class OldRunOverlay {
	
	//********************CONSTANTS***************************
	private final static int[] ROUTE_COLOR = new int[] {220, 240, 20, 20};
	private final static int[] ACTUAL_COLOR = new int[] {200, 50, 50, 250};
	private final static int[] COMPLETED_COLOR = new int[] {200, 50, 240, 50};

	private final static Style ROUTE_STYLE = Style.STROKE; 
	private final static Style ACTUAL_STYLE = Style.STROKE; 
	private final static float STROKE_WIDTH = 4.0f; 

	private final static float[] ROUTE_DASHES = new float[] {10.0f, 5.0f, 3.0f, 5.0f};  
	private final static float[] ACTUAL_DASHES = new float[] {10.0f, 5.0f};  

	private final static Point START_OFFSET = new Point(35,0);
	private final static Point FLAG_OFFSET = new Point(0, 31); 

	private MapView theMapView;
	private Paint pathPaint;
	private OldRun run; 
	private int legIndex; 
	
	private Bitmap START;
    private BitmapDescriptor START_DESCRIPTOR;
	private Bitmap DESTINATION1;
    private BitmapDescriptor DESTINATION1_DESCRIPTOR;
	private Bitmap DESTINATION2;
    private BitmapDescriptor DESTINATION2_DESCRIPTOR;
	private Bitmap FLAG;
    private BitmapDescriptor FLAG_DESCRIPTOR;
	private Bitmap MAP_X;
    private BitmapDescriptor MAP_X_DESCRIPTOR;

	private RelativeLayout mapRelLayout; 

	public OldRunOverlay(MapView map, OldRun run, RelativeLayout mapRelLayout) {
		this.theMapView = map;
		this.run = run; 
		this.pathPaint = new Paint();
		this.mapRelLayout = mapRelLayout; 
		this.legIndex = 0; 

		//Paint settings
		this.pathPaint.setAntiAlias(true);
		
		START = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.start); 
		DESTINATION1 = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.destination_icon1); 
		DESTINATION2 = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.destination_icon2); 

		FLAG = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.chequered_flag_icon); 
		MAP_X = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.map_x);

        MapsInitializer.initialize(map.getContext());
        START_DESCRIPTOR = BitmapDescriptorFactory.fromBitmap(START);
        DESTINATION1_DESCRIPTOR = BitmapDescriptorFactory.fromBitmap(DESTINATION1);
        DESTINATION2_DESCRIPTOR = BitmapDescriptorFactory.fromBitmap(DESTINATION2);
        FLAG_DESCRIPTOR = BitmapDescriptorFactory.fromBitmap(FLAG);
        MAP_X_DESCRIPTOR = BitmapDescriptorFactory.fromBitmap(MAP_X);
		
	}

	public void setLegIndex(int index) {
		legIndex = index; 
	}

	public void drawRoutes(GoogleMap googleMap, MapView map) {

		//Get a Projection object to convert between lat/lng --> x/y
		Projection pro = googleMap.getProjection();

		OldLeg leg = run.get(legIndex);

		List<LatLng> runPath = leg.getRunPath();
		List<LatLng> directionsPath = leg.getPolylinePath();
		
		LatLng startPoint = null;

		if (runPath.size() > 0) {
			drawAPath(runPath, googleMap, STROKE_WIDTH, Color.BLACK);

			//Draw flag where runner ended up at the end of this leg
			LatLng endPoint = leg.getRunEnd();
			Point endCoords = pro.toScreenLocation(endPoint);
            LatLng endLatLng = pro.fromScreenLocation(new Point(endCoords.x - FLAG_OFFSET.x, endCoords.y - FLAG_OFFSET.y));

            MarkerOptions flagMarker = new MarkerOptions()
                    .icon(FLAG_DESCRIPTOR)
                    .position(endLatLng);

			googleMap.addMarker(flagMarker);

		}

		if (directionsPath.size() > 0) {
			//Draw the directions for the appropriate leg in ROUTE_COLOR
			drawAPath(directionsPath, googleMap, STROKE_WIDTH, Color.RED);

			startPoint = directionsPath.get(0); //First point of run
			Point startCoords = pro.toScreenLocation(startPoint);

            Point centerStartPoint = new Point(startCoords.x - START.getWidth() / 2, startCoords.y - START.getHeight() / 2);
            LatLng startLatLng = pro.fromScreenLocation(centerStartPoint);

            MarkerOptions drawCircleMarker = new MarkerOptions()
                    .icon(START_DESCRIPTOR)
                    .position(startLatLng);

			//Draw the little red circle at the start point
			googleMap.addMarker(drawCircleMarker);
		}

		LatLng placePoint = leg.getPlaceLatitudeLng();  //Location of place
		Point placeCoords = pro.toScreenLocation(placePoint);

		Bitmap bmp = null;
        BitmapDescriptor bitmapDescriptor;

		if (!leg.gotToPlace()) { //If runner didn't get to the place, draw an 'X'
			bmp = MAP_X;
            bitmapDescriptor = MAP_X_DESCRIPTOR;
		}
		else { //Else draw a trophy, which one depends on whether it's the last leg of the journey
			bmp = (legIndex == run.size() - 1 ? DESTINATION1 : DESTINATION2); //Draw different icon for end of run vs. just end of one leg
			bitmapDescriptor = (legIndex == run.size() - 1 ? DESTINATION1_DESCRIPTOR : DESTINATION2_DESCRIPTOR); //Draw different icon for end of run vs. just end of one leg
		}

        LatLng placeLatLng = pro.fromScreenLocation(new Point(placeCoords.x - bmp.getWidth() / 2, placeCoords.y - bmp.getHeight() / 2));

        MarkerOptions placeMarker = new MarkerOptions()
                .icon(bitmapDescriptor)
                .position(placeLatLng);

		googleMap.addMarker(placeMarker);
	}

	private void drawAPath(List<LatLng> path, GoogleMap googleMap, float strokeWidth, int color) {

        if(path == null || path.size() == 0) {
            return;
        }

        //**********************SET UP PATH***************************
        LatLng startPoint = path.get(0);

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(startPoint);

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
        googleMap.addPolyline(polylineOptions);
	}
	
}
