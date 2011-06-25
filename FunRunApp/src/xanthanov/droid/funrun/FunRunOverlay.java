package xanthanov.droid.funrun;

import xanthanov.droid.gplace.*;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;

import java.util.List;

import com.google.android.maps.Overlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;
import com.google.android.maps.MapView;

public class FunRunOverlay extends Overlay {
	
	//********************CONSTANTS***************************
	private final static int[] DEFAULT_COLOR = new int[] {200, 220, 100, 100};

	private final static int alphaComplete = 200; 
	private final static int redComplete = 220; 
	private final static int greenComplete = 100; 
	private final static int blueComplete = 100; 

	private final static int alphaActual = 100; 
	private final static int redActual = 100; 
	private final static int greenActual = 120; 
	private final static int blueActual = 240; 

	private final static Paint.Style PAINT_STYLE = Paint.Style.STROKE; 
	private final static float STROKE_WIDTH = 4.0f; 

	private final static Point PIN_OFFSET = new Point(5,29);
	private final static Point STICK_GUY_OFFSET = new Point(15,27);

	private MapView theMapView = null;
	private Paint pathPaint = null;
	private GoogleDirections directions = null;
	private GeoPoint currentLoc = null;
	private GeoPoint currentStepPoint = null; 
	private List<GeoPoint> actualPath = null;
	
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

	public void updateCurrentLocation(GeoPoint loc) { this.currentLoc = loc; }	

	public void updateCurrentDirections(GoogleDirections directions) { this.directions = directions;}

	public void updateStepPoint(GeoPoint pt) { this.currentStepPoint = pt;}

	public void updateActualPath(List<GeoPoint> path) {actualPath = path; }

	@Override
	public void draw(Canvas canvas, MapView map, boolean b) {
		super.draw(canvas, map, b);

		//Get a Projection object to convert between lat/lng --> x/y
		Projection pro = theMapView.getProjection(); 

		//Reset paint
		this.pathPaint.setStrokeWidth(STROKE_WIDTH);
		this.pathPaint.setARGB(DEFAULT_COLOR[0], DEFAULT_COLOR[1], DEFAULT_COLOR[2], DEFAULT_COLOR[3]);
		//Use Stroke style
		this.pathPaint.setStyle(PAINT_STYLE);

		Point screenCoords = new Point();

		//If directions is null, return
		if (directions != null) {
			//**********************SET UP PATH***************************
			GeoPoint startPoint = directions.get(0).getFirstPoint(); //First point of the first leg
			GeoPoint endPoint = directions.get(directions.size()-1).getLastPoint();  //Last point of the last leg

			pro.toPixels(startPoint, screenCoords); 

			//Create the path
			Path thePath = new Path();
			thePath.moveTo((float)screenCoords.x, (float)screenCoords.y);
			
			//Retrieve distinct GeoPoints of the route, w/o the redundancies.
			List<GeoPoint> route = directions.getPathPoints();
			
			if(route == null || route.size() == 0) {
				return;
			}
			
			//Loop through all GeoPoints
			for (GeoPoint current : route) {
				//Convert GeoPoint to pixels and add to path
				if(current != null){
					pro.toPixels(current, screenCoords); 
					thePath.lineTo((float)screenCoords.x, (float)screenCoords.y);
				}
			}
			

			/* Draw the actual route to the canvas. */
			canvas.drawPath(thePath, this.pathPaint);
			
			/* Draw end of route */ 
			pro.toPixels(endPoint, screenCoords);
			
			canvas.drawBitmap(PIN_END, 
					screenCoords.x - PIN_OFFSET.x, 
					screenCoords.y - PIN_OFFSET.y, 
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

	}

	private void drawAPath(List<GeoPoint> path, float strokeWidth, Style paintStyle, int[] color) {

		if(path == null || path.size() == 0) {
			return;
		}

		//Get a Projection object to convert between lat/lng --> x/y
		Projection pro = theMapView.getProjection(); 

		//Add style and color to paint
		this.pathPaint.setStrokeWidth(strokeWidth);
		this.pathPaint.setARGB(color[0], color[1], color[2], color[3]);
		//Use Stroke style
		this.pathPaint.setStyle(PAINT_STYLE);

		//Generic Point used in this method
		Point screenCoords = new Point();

		//**********************SET UP PATH***************************
		GeoPoint startPoint = path.get(0); 
		pro.toPixels(startPoint, screenCoords); 

		//Create the path
		Path thePath = new Path();
		thePath.moveTo((float)screenCoords.x, (float)screenCoords.y);
		
		//Loop through all GeoPoints
		for (GeoPoint current : path) {
			//Convert GeoPoint to pixels and add to path
			if(current != null){
				pro.toPixels(current, screenCoords); 
				thePath.lineTo((float)screenCoords.x, (float)screenCoords.y);
			}
		}

		/* Draw the actual route to the canvas. */
		canvas.drawPath(thePath, this.pathPaint);
	}
	
}
