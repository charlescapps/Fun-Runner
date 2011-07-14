package xanthanov.droid.funrun;

import xanthanov.droid.gplace.*;

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

public class FunRunOverlay extends Overlay {
	
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
	private final static Point RUNNER_OFFSET = new Point(15,40);

	private MapView theMapView = null;
	private Paint pathPaint = null;
	private GoogleDirections directions = null;
	private GoogleLeg specificLeg = null; 
	private boolean drawRoute = false; 
	private boolean drawSpecificRoute = false; 
	
	private Bitmap START = null; 
	private Bitmap DESTINATION1 = null; 
	private Bitmap DESTINATION2 = null; 

	private Bitmap RUNNER1; 

	private boolean animateRunner; 
	private ImageView runnerImageView; 
	private GeoPoint prevRunnerPoint; 
	private GeoPoint curRunnerPoint;
	private RelativeLayout mapRelLayout; 

	public FunRunOverlay(MapView map, GoogleDirections directions, boolean drawRoute, boolean drawSpecificRoute, boolean animateRunner, RelativeLayout mapRelLayout) {
		System.out.println("***********ENTERING FUN RUN OVERLAY CONSTRUCTOR**************"); 
		this.theMapView = map;
		this.directions = directions;
		this.drawRoute = drawRoute; 
		this.drawSpecificRoute = drawSpecificRoute; 
		this.pathPaint = new Paint();
		this.animateRunner = animateRunner; 

		prevRunnerPoint = curRunnerPoint = null;

		this.mapRelLayout = mapRelLayout; 
		System.out.println("Map rel layout: " + mapRelLayout); 

		if (animateRunner && mapRelLayout != null) {

			runnerImageView = new ImageView(map.getContext()); 
			runnerImageView.setBackgroundResource(R.drawable.runner_anim);
			runnerImageView.setAdjustViewBounds(true); 
			mapRelLayout.addView(runnerImageView); 

		}

		if (directions != null) {
			this.curRunnerPoint = DroidLoc.degreesToGeoPoint(directions.getFirstPoint()); 
			curRunnerPoint = prevRunnerPoint = curRunnerPoint; 
		}

		//Paint settings
		this.pathPaint.setAntiAlias(true);
		
		START = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.start); 
		DESTINATION1 = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.destination_icon1); 
		DESTINATION2 = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.destination_icon2); 

		RUNNER1 = BitmapFactory.decodeResource(this.theMapView.getContext().getResources(), R.drawable.runner1); 

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

	public void updateCurrentLocation(GeoPoint loc) { 
		prevRunnerPoint = curRunnerPoint; 
		curRunnerPoint = loc; 

		theMapView.postInvalidate(); 
	}	

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
		if (directions != null && directions.size() > 0) {

			GoogleLeg legToDraw = null; 
			if (drawSpecificRoute && (specificLeg != null )) { //Either draw a specific leg, or the last leg by default
				legToDraw = specificLeg; 
			}
			else { //default = last leg
				legToDraw = directions.lastLeg(); 
			}

			int indexOfLeg = directions.getLegs().indexOf(specificLeg); //Get index of the currently selected leg

			GeoPoint startPoint = DroidLoc.degreesToGeoPoint(legToDraw.getFirstPoint()); //First point of the last leg
			GeoPoint endPoint = DroidLoc.degreesToGeoPoint(legToDraw.getLastPoint());  //Last point of the last leg

			pro.toPixels(startPoint, startCoords); 
			pro.toPixels(endPoint, endCoords);

			if (drawRoute || drawSpecificRoute) {
			//Draw the directions for the current leg in ROUTE_COLOR
				drawAPath(legToDraw.getPathPoints(), canvas, STROKE_WIDTH, ROUTE_STYLE, ROUTE_COLOR, pro, ROUTE_DASHES);
			}

			//Draw the current path you've ran for the leg in ACTUAL_COLOR
			drawAPath(legToDraw.getActualPath(), canvas, STROKE_WIDTH, ACTUAL_STYLE, ACTUAL_COLOR, pro, ACTUAL_DASHES); 

			//Draw all your previous actual path's other than the current one in COMPLETED_COLOR
			for (int i = 0; i < directions.size() - 1; i++) {
				if (i != indexOfLeg) {
					drawAPath(directions.get(i).getActualPath(), canvas, STROKE_WIDTH, ACTUAL_STYLE, COMPLETED_COLOR, pro, ACTUAL_DASHES); 
				}
			}

			//Draw start graphic at initial point
			Point journeyStartPoint = new Point(); 
			pro.toPixels(DroidLoc.degreesToGeoPoint(directions.getFirstPoint()), journeyStartPoint); 
			Bitmap bmp = START; 
			canvas.drawBitmap(bmp, journeyStartPoint.x - bmp.getWidth() / 2, journeyStartPoint.y - bmp.getHeight() / 2, pathPaint); 

			//Draw destination image at end point
			//Bitmap bmp = legToDraw.getLegDestination().getIconBmp(); 
			bmp = (indexOfLeg == directions.size() - 1 ? DESTINATION1 : DESTINATION2); //Draw different icon for end of run vs. just end of one leg 
			canvas.drawBitmap(bmp, endCoords.x - bmp.getWidth() / 2 , endCoords.y - bmp.getHeight() / 2, pathPaint);
		}

		//Draw stick guy at current location even if no directions exist
		
		if (curRunnerPoint != null) {

			Point runnerPoint = new Point(); 
			pro.toPixels(curRunnerPoint, runnerPoint); 

			if (animateRunner) {
				TranslateAnimation ta = new TranslateAnimation((int)runnerPoint.x - RUNNER_OFFSET.x, (int)runnerPoint.x - RUNNER_OFFSET.x, (int)runnerPoint.y - RUNNER_OFFSET.y, (int)runnerPoint.y - RUNNER_OFFSET.y); 
				runnerImageView.startAnimation(ta); 
			}
			else {
				canvas.drawBitmap(RUNNER1, runnerPoint.x - RUNNER_OFFSET.x, runnerPoint.y - RUNNER_OFFSET.y, pathPaint);
			}
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
