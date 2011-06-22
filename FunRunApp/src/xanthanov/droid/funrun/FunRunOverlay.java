// Created by plusminus on 14:00:27 - 30.01.2008
package xanthanov.droid.funrun;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import java.util.List;

import com.google.android.maps.Overlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;
import com.google.android.maps.MapView;

public class FunRunOverlay extends Overlay {
	
	//********************CONSTANTS***************************
	private final static int paintAlpha = 200; 
	private final static int paintRed = 220; 
	private final static int paintGreen = 100; 
	private final static int paintBlue = 100; 
	private final static Point PIN_HOTSPOT = new Point(5,29);
	private final static Point STICK_GUY_OFFSET = new Point(15,27);

	private MapView theMapView = null;
	private Paint pathPaint = null;
	private GoogleDirections directions = null;
	private GeoPoint currentLoc = null;
	
	private Bitmap PIN_START = null;
	private Bitmap PIN_END = null;
	private Bitmap INFO_LOWER_LEFT = null;
	private Bitmap STICK_GUY_RUN1 = null; 
	private Bitmap STICK_GUY_RUN2 = null; 
	private Bitmap STICK_GUY_BG = null; 
	private Bitmap CURRENT_STICK_GUY = null; 
	private int frameNo; 
	
	public FunRunOverlay(MapView map, GoogleDirections directions) {
		this.theMapView = map;
		this.directions = directions;
		this.pathPaint = new Paint();
		if (directions != null) {
			this.currentLoc = directions.getFirstPoint();
		}
		else {
			this.currentLoc = null; 
		}
		this.frameNo = 0; 

		//Paint settings
		this.pathPaint.setAntiAlias(true);
	
		//System.out.println(map.getContext()); 
		
		PIN_START = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.mappin_blue);
		PIN_END = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.mappin_red);
		INFO_LOWER_LEFT = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.lower_left_info);

		STICK_GUY_RUN1 = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.stick_guy_run1);
		STICK_GUY_RUN2 = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.stick_guy_run2);
		CURRENT_STICK_GUY = STICK_GUY_RUN1; 
		STICK_GUY_BG = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.stick_guy_bg);
	}

	public void updateCurrentLocation(GeoPoint loc) {
		this.currentLoc = loc; 
	}	

	public void updateCurrentDirections(GoogleDirections directions) {
		this.directions = directions;
	}

	@Override
	public void draw(Canvas canvas, MapView map, boolean b) {
		super.draw(canvas, map, b);
		
		if (++frameNo %10 == 0) { //Every 20 times draw is called alternate the stick figure we draw
			CURRENT_STICK_GUY = (CURRENT_STICK_GUY == STICK_GUY_RUN1 ? STICK_GUY_RUN2 : STICK_GUY_RUN1);  
		}

		//System.out.println("In draw method..."); 
		//Get a Projection object to convert between lat/lng --> x/y
		Projection pro = theMapView.getProjection(); 

		/* Reset our paint. */
		this.pathPaint.setStrokeWidth((float)4.0);
		this.pathPaint.setARGB(paintAlpha, paintRed, paintGreen, paintBlue);

		// holders of mapped coords...
		Point screenCoords = new Point();

		//If directions is null, return
		if (directions != null) {
			//**********************SET UP PATH***************************

			// method in the custom map view to return the DrivingDirection object.
			/* First get Start end End Point of the route. */
			GeoPoint startPoint = directions.get(0).getFirstPoint(); //First point of the first leg
			GeoPoint endPoint = directions.get(directions.size()-1).getLastPoint();  //Last point of the last leg

			pro.toPixels(startPoint, screenCoords); 

			/* Create a path-that will be filled with map-points 
			 * and will be drawn to the canvas in the end*/
			Path thePath = new Path();
			thePath.moveTo((float)screenCoords.x, (float)screenCoords.y);
			
			/* Retrieve distinct GeoPoints of the route, w/o the redundancies. */
			List<GeoPoint> route = directions.getPathPoints();
			
			if(route == null || route.size() == 0)
				return;
			
			/* Loop through all MapPoints returned. */
			for (GeoPoint current : route) {
				/* Transform current MapPoint's Lat/Lng 
				 * into corresponding point on canvas 
				 * using the pixelCalculator. */
				if(current != null){
					pro.toPixels(current, screenCoords); 
					/* Add point to path. */
					thePath.lineTo((float)screenCoords.x, (float)screenCoords.y);
				}
			}
			
			//Use Stroke style
			this.pathPaint.setStyle(Paint.Style.STROKE);

			/* Draw the actual route to the canvas. */
			canvas.drawPath(thePath, this.pathPaint);
			
			/* Draw start of route.*/
			/*
			pro.toPixels(startPoint, screenCoords);
			
			canvas.drawBitmap(PIN_START, 
					screenCoords.x - PIN_HOTSPOT.x, 
					screenCoords.y - PIN_HOTSPOT.y, 
					pathPaint);
			*/

			/* Draw end of route */ 
			pro.toPixels(endPoint, screenCoords);
			
			canvas.drawBitmap(PIN_END, 
					screenCoords.x - PIN_HOTSPOT.x, 
					screenCoords.y - PIN_HOTSPOT.y, 
					pathPaint);
		}

		//Draw stick guy
		if (currentLoc != null) {
			pro.toPixels(currentLoc, screenCoords);
		
			canvas.drawBitmap(STICK_GUY_BG, 
				screenCoords.x - STICK_GUY_OFFSET.x, 
				screenCoords.y - STICK_GUY_OFFSET.y, 
				pathPaint);

			canvas.drawBitmap(STICK_GUY_RUN1, 
				screenCoords.x - STICK_GUY_OFFSET.x, 
				screenCoords.y - STICK_GUY_OFFSET.y, 
				pathPaint);
		}

		/* Get the height of the underlying MapView.*/
		int mapViewHeight = theMapView.getHeight();
		
		/* Now some real fancy stuff !*/
		/* Draw a info-menu for the route.*/
		//canvas.drawBitmap(this.INFO_LOWER_LEFT, 0, mapViewHeight - this.INFO_LOWER_LEFT.getHeight(), pathPaint);
		
		/* And draw i.e.the distance and time left to the info-menu.*/
		//String distance = directions.getDistanceString();
		//String time = dd.getFormattedTime().replace("Time: ", "");
		/*pathPaint.setARGB(255,255,255,255);
		this.pathPaint.setStrokeWidth(1);
		this.pathPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		pathPaint.setTextSize(24);
		canvas.drawText(time, 5, mapViewHeight - 5, pathPaint);
		pathPaint.setTextSize(16);
		canvas.drawText(distance, 4, mapViewHeight - 35, pathPaint); // 2, 271
		*/
		/* These methods are to illustrate some 
		 * of what you can get out of the system. */
//			String startName = dd.getRouteStartLocation();
//			String info = dd.getRouteInfoDescriptor();
	}
	
}
