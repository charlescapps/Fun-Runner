//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun;

import xanthanov.droid.gplace.*;
import xanthanov.droid.funrun.db.OldRun; 
import xanthanov.droid.funrun.db.OldLeg; 

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.DashPathEffect; 

import android.widget.ImageView; 
import android.widget.RelativeLayout;  
import android.view.animation.TranslateAnimation; 
import android.graphics.drawable.AnimationDrawable; 

import java.util.List;

import com.google.android.maps.Overlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;
import com.google.android.maps.MapView;

import xanthanov.droid.xantools.DroidLoc; 

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

public class OldRunOverlay extends Overlay {
	
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
	private Bitmap DESTINATION1; 
	private Bitmap DESTINATION2; 
	private Bitmap FLAG; 
	private Bitmap MAP_X; 

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
		
	}

	public void setLegIndex(int index) {
		legIndex = index; 
	}

	@Override
	public void draw(Canvas canvas, MapView map, boolean b) {
		super.draw(canvas, map, b);

		//Get a Projection object to convert between lat/lng --> x/y
		Projection pro = theMapView.getProjection(); 

		Point startCoords = new Point(); //Coords where runner started this leg
		Point endCoords = new Point(); //Coords where runner ended this leg

		Point placeCoords = new Point(); //Coords of place

		OldLeg leg = run.get(legIndex); 

		List<LatLng> runPath = leg.getRunPath(); 
		List<LatLng> directionsPath = leg.getPolylinePath(); 
		
		GeoPoint startPoint = null; 

		if (runPath.size() > 0) {
			//Draw flag where runner ended up at the end of this leg
			GeoPoint endPoint = DroidLoc.latLngToGeoPoint(leg.getRunEnd()); 
			pro.toPixels(endPoint, endCoords); 

			canvas.drawBitmap(FLAG, endCoords.x - FLAG_OFFSET.x , endCoords.y - FLAG_OFFSET.y, pathPaint);

			drawAPath(runPath, canvas, STROKE_WIDTH, ACTUAL_STYLE, ACTUAL_COLOR, pro, ACTUAL_DASHES); 
		}

		if (directionsPath.size() > 0) {
			startPoint = DroidLoc.latLngToGeoPoint(directionsPath.get(0)); //First point of run
			pro.toPixels(startPoint, startCoords); 
			//Draw the little red circle at the start point
			canvas.drawBitmap(START, startCoords.x - START.getWidth() / 2, startCoords.y - START.getHeight() / 2, pathPaint); 

			//Draw the directions for the appropriate leg in ROUTE_COLOR
			drawAPath(directionsPath, canvas, STROKE_WIDTH, ROUTE_STYLE, ROUTE_COLOR, pro, ROUTE_DASHES);

		}

		//Draw all your previous actual path's other than the current one in COMPLETED_COLOR
		for (int i = 0; i < run.size(); i++) {
			if (i != legIndex) {
				runPath = run.get(i).getRunPath(); 
				if (runPath.size() > 0) {
					drawAPath(runPath, canvas, STROKE_WIDTH, ACTUAL_STYLE, COMPLETED_COLOR, pro, ACTUAL_DASHES); 
				}
			}

			GeoPoint placePoint = DroidLoc.latLngToGeoPoint(run.get(i).getPlaceLatLng());  //Location of place
			pro.toPixels(placePoint, placeCoords); 

			Bitmap bmp = null; 

			if (!run.get(i).gotToPlace()) { //If runner didn't get to the place, draw an 'X'
				bmp = MAP_X; 
			}
			else { //Else draw a trophy, which one depends on whether it's the last leg of the journey
				bmp = (i == run.size() - 1 ? DESTINATION1 : DESTINATION2); //Draw different icon for end of run vs. just end of one leg 
			}

			canvas.drawBitmap(bmp, placeCoords.x - bmp.getWidth() / 2 , placeCoords.y - bmp.getHeight() / 2, pathPaint);
		}

	}

	private void drawAPath(List<LatLng> path, Canvas canvas, float strokeWidth, Style paintStyle, int[] color, Projection pro, float[] dashes) {

		if(path == null || path.size() == 0) {
			return;
		}

		//Add style and color to paint
		this.pathPaint.setStrokeWidth(strokeWidth);
		this.pathPaint.setARGB(color[0], color[1], color[2], color[3]);
		//Use Stroke style
		this.pathPaint.setStyle(paintStyle);

		//Generic Point used in this method
		Point screenCoords = new Point();

		//**********************SET UP PATH***************************
		GeoPoint startPoint = DroidLoc.latLngToGeoPoint(path.get(0)); 
		pro.toPixels(startPoint, screenCoords); 

		//Create the path
		Path thePath = new Path();

		//Create PathDash effect
		DashPathEffect pathDash = new DashPathEffect(dashes, 0); 
		pathPaint.setPathEffect(pathDash); 

		thePath.moveTo((float)screenCoords.x, (float)screenCoords.y);
		
		//Loop through all GeoPoints
		for (LatLng current : path) {
			//Convert GeoPoint to pixels and add to path
			if(current != null){
				pro.toPixels(DroidLoc.latLngToGeoPoint(current), screenCoords); 
				thePath.lineTo((float)screenCoords.x, (float)screenCoords.y);
			}
		}

		/* Draw the actual route to the canvas. */
		canvas.drawPath(thePath, this.pathPaint);
	}
	
}
