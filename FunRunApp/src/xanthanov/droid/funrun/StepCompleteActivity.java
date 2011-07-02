package xanthanov.droid.funrun; 

import xanthanov.droid.gplace.*;
import xanthanov.droid.xantools.*; 

import android.app.Activity; 
import android.os.Bundle;
import android.os.Vibrator; 
import android.content.Context; 
import android.app.Application;
import android.view.View;
import android.widget.TextView;
import android.widget.Button; 
import android.content.Intent;
import android.text.Spanned;

public class StepCompleteActivity extends Activity {

	private GoogleStep completedStep = null; 
	private GoogleDirections runDirections;
	private GoogleLeg currentLeg; 
	private GooglePlace runToPlace; 
	private FunRunApplication funRunApp; 
	private int completedStepIndex = -1; 

	private TextView stepCompleteTitle;
	private TextView stepCompleteText; 
	private TextView legDistanceText; 
	private TextView totalDistanceText; 
	private TextView avgSpeedText; 
	private TextView avgSpeedRunText; 
	private TextView elapsedTimeToPlaceText; 
	private TextView totalElapsedTimeText; 
	private Button nextDirectionsButton; 

	private Vibrator vibe; 
	private final static long[] VIB_PATTERN = new long[] {500, 100, 500, 100, 500, 100, 500, 100, 500, 100, 500, 100, 1000, 100, 1000, 100, 1000, 100}; 

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.step_complete); 
		System.out.println("In step complete activity..."); 

		funRunApp = (FunRunApplication) getApplicationContext(); 
		Intent startIntent = this.getIntent();
		completedStepIndex = startIntent.getIntExtra(FunRunActivity.STEP_EXTRA, -1); 
		System.out.println("Completed step index: " + completedStepIndex); 

		if (completedStepIndex == -1) {
			String msg = "Step completed, but stepNo was -1 in IntExtra"; 
			System.err.println(msg); 
			DroidDialogs.showPopup(this,"Error--Debugging", msg); 
			//finish(); 
			return; 
		}	  

		runDirections = funRunApp.getRunDirections(); 
		currentLeg = runDirections.lastLeg(); 
		runToPlace = currentLeg.getLegDestination(); 

		completedStep = currentLeg.get(completedStepIndex); 

		//If everything goes well, remove all the proximity listeners for this step and prior
		currentLeg.removeProximityAlerts(completedStep, getApplicationContext()); 

		stepCompleteTitle = (TextView) findViewById(R.id.stepCompleteTitle); 
		stepCompleteText = (TextView) findViewById(R.id.stepCompleteTextView); 

		legDistanceText = (TextView) findViewById(R.id.legDistanceTextView); 
		totalDistanceText = (TextView) findViewById(R.id.totalDistanceTextView); 
		avgSpeedText = (TextView) findViewById(R.id.avgSpeedTextView); 
		avgSpeedRunText = (TextView) findViewById(R.id.avgSpeedRunTextView); 
		elapsedTimeToPlaceText = (TextView) findViewById(R.id.elapsedTimeToPlaceTextView); 
		totalElapsedTimeText = (TextView) findViewById(R.id.totalElapsedTimeTextView); 


		nextDirectionsButton = (Button) findViewById(R.id.nextDirectionsButton); 

		vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		long stepEndTime = System.currentTimeMillis(); 

		if (completedStepIndex >= currentLeg.size() - 1) {
			stepCompleteTitle.setText("Congratulations!"); 
		}
		completedStep.completeStep(); 
		setStopTime(stepEndTime); 
		setNextStep(); 
		displayMsg(stepEndTime);
		vibrate(vibe);  
		setupNextDirectionsButton(); 

	}

	private void setStopTime(long endTime) {
		completedStep.setEndTime(endTime); 
		currentLeg.setEndTime(endTime); 
	}

	private void setNextStep() {
		//If we've finished the leg, set current step to null
		if (completedStepIndex >= currentLeg.size() - 1) {
			funRunApp.setCurrentStep(null); 
		}
		else {
			int nextIndex = completedStepIndex + 1; 
			funRunApp.setCurrentStep(currentLeg.get(nextIndex)); 
		}
	}

	private void displayMsg(long stepEndTime) {
		Spanned msg = null,
			legDistanceText = null, 
			totalDistanceText = null, 
			avgSpeed= null, 
			avgSpeedRun = null, 
			legElapsedTime = null, 
			totalElapsedTime = null;

		if (completedStepIndex>=currentLeg.size() - 1) {
			msg = android.text.Html.fromHtml("You ran to <b>" + runToPlace.getName() + "</b>!"); 
		}
		else {
			msg = android.text.Html.fromHtml("You completed step " + (completedStepIndex + 1) + " on your way to <b>" + runToPlace.getName() + "</b>!");  
		}

		//Distance this leg and total distance overall
		int legDistance = currentLeg.getDistanceSoFar(); 
		int totalDistance = runDirections.getDistanceSoFar(); 

		legDistanceText = DroidTime.getDistanceString(legDistance); 
		totalDistanceText = DroidTime.getDistanceString(totalDistance); 
		
		legElapsedTime = DroidTime.msToStr(stepEndTime - currentLeg.getStartTime()); 
		totalElapsedTime = DroidTime.msToStr(stepEndTime - runDirections.get(0).getStartTime()); 	


		//Avg. speed in m/s 
		avgSpeed = DroidTime.getSpeedString(stepEndTime - currentLeg.getStartTime(), legDistance); 
		avgSpeedRun = DroidTime.getSpeedString(stepEndTime - runDirections.get(0).getStartTime(), totalDistance); 

		//Set text views.
		stepCompleteText.setText(msg); 
		elapsedTimeToPlaceText.setText(legElapsedTime); 
		totalElapsedTimeText.setText(totalElapsedTime);
		avgSpeedText.setText(avgSpeed); 
		avgSpeedRunText.setText(avgSpeedRun); 
	}

	private void setupNextDirectionsButton() {
		//If we've finished the leg, change the text to "Choose next destination" 
		if (completedStepIndex >= currentLeg.size() - 1) {
			nextDirectionsButton.setText("Choose next destination"); 
		}
		else {
			nextDirectionsButton.setText("Get next directions"); 
		}
		//Finish the activity to return to the FunRunActivity upon click
		nextDirectionsButton.setOnClickListener(new View.OnClickListener() {
			@Override 
			public void onClick(View v) {
				finish(); 
			}
		});
	}

	private static void vibrate(Vibrator v) {
		v.vibrate(VIB_PATTERN, -1);  
	}
}
