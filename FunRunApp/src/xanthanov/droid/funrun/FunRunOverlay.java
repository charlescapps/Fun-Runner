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
	private final static int[] ROUTE_COLOR = new int[] {200, 240, 100, 100};
	private final static int[] ACTUAL_COLOR = new int[] {200, 120, 180, 250};
	private final static int[] COMPLETED_COLOR = new int[] {200, 180, 240, 180};

	private final static Style ROUTE_STYLE = Style.STROKE; 
	private final static Style ACTUAL_STYLE = Style.STROKE; 
	private final static float STROKE_WIDTH = 4.0f; 

	private final static Point PIN_OFFSET = new Point(5,29);
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

		//Paint settings
		this.pathPaint.setAntiAlias(true);
		
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

	@Override
	public void draw(Canvas canvas, MapView map, boolean b) {
		super.draw(canvas, map, b);

		//Get a Projection object to convert between lat/lng --> x/y
		Projection pro = theMapView.getProjection(); 

		Point startCoords = new Point();
		Point endCoords = new Point();
		Point currentCoords = new Point();

		//If directions isn't null, draw the directions in red
		if (directions != null) {
			GeoPoint startPoint = directions.lastLeg().getFirstPoint(); //First point of the last leg
			GeoPoint endPoint = directions.lastLeg().getLastPoint();  //Last point of the last leg

			pro.toPixels(startPoint, startCoords); 
			pro.toPixels(endPoint, endCoords);

			//Draw end point
			canvas.drawBitmap(PIN_END, endCoords.x - PIN_OFFSET.x, endCoords.y - PIN_OFFSET.y, pathPaint);

			//Draw the directions for the current leg in ROUTE_COLOR
			drawAPath(directions.lastLeg().getPathPoints(), canvas, STROKE_WIDTH, ROUTE_STYLE, ROUTE_COLOR, pro);

			//Draw the current path you've ran for the leg in ACTUAL_COLOR
			drawAPath(directions.lastLeg().getActualPath(), canvas, STROKE_WIDTH, ACTUAL_STYLE, ACTUAL_COLOR, pro); 

			//Draw all your previous actual path's in COMPLETED_COLOR
			for (int i = 0; i < directions.size() - 1; i++) {
				drawAPath(directions.get(i).getActualPath(), canvas, STROKE_WIDTH, ACTUAL_STYLE, COMPLETED_COLOR, pro); 
			}
			
		}

		//Draw stick guy at current location
		if (currentLoc != null) {
			pro.toPixels(currentLoc, currentCoords);
		
			canvas.drawBitmap(STICK_GUY_BG, currentCoords.x - STICK_GUY_OFFSET.x, currentCoords.y - STICK_GUY_OFFSET.y, pathPaint);
			canvas.drawBitmap(STICK_GUY_RUN1, currentCoords.x - STICK_GUY_OFFSET.x, currentCoords.y - STICK_GUY_OFFSET.y, pathPaint);
		}

	}

	private void drawAPath(List<GeoPoint> path, Canvas canvas, float strokeWidth, Style paintStyle, int[] color, Projection pro) {

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
