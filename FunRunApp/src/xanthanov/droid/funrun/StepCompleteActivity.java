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

public class StepCompleteActivity extends Activity {

	private GoogleStep completedStep = null; 
	private GoogleDirections runDirections;
	private GoogleLeg currentLeg; 
	private GooglePlace runToPlace; 
	private FunRunApplication funRunApp; 
	private int completedStepIndex = -1; 

	private TextView stepCompleteText; 
	private TextView stepTimeText; 
	private TextView elapsedTimeToPlaceText; 
	private TextView totalElapsedTimeText; 
	private Button nextDirectionsButton; 

	private Vibrator vibe; 
	private final static long[] VIB_PATTERN = new long[] {500, 100, 500, 100, 500, 100, 500, 100, 500, 100, 500, 100, 1000, 100, 1000, 100, 1000, 100}; 

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.step_complete); 

		funRunApp = (FunRunApplication) getApplicationContext(); 
		Intent startIntent = this.getIntent();
		int lat = startIntent.getIntExtra("lat", -1);
		int lng = startIntent.getIntExtra("lng", -1); 

		if (lat == -1 || lng == -1) {
			System.err.println("Step completed, but no latitude / longitude was stored in the intent!"); 
			finish(); 
			return; 
		}	  

		runDirections = funRunApp.getRunDirections(); 
		currentLeg = runDirections.lastLeg(); 
		runToPlace = currentLeg.getLegDestination(); 

		//Find the index of the step that ends on the lat / lng passed to this activity by its starting Intent
		for (int i = 0; i < currentLeg.size(); i++) {
			GoogleStep gs = currentLeg.get(i); 
			if (gs.getEnd().getLatitudeE6() == lat && gs.getEnd().getLongitudeE6() == lng) {
				completedStepIndex = i; 
				break; 				
			}
		}

		if (completedStepIndex == -1) {
			System.err.println("Step completed, but there was no step in current leg for the given lat / lng!"); 
			finish(); 
			return; 
		}	  

		completedStep = currentLeg.get(completedStepIndex); 

		//If everything goes well, remove all the proximity listeners for this step and prior
		currentLeg.removeProximityAlerts(completedStep, this); 

		stepCompleteText = (TextView) findViewById(R.id.stepCompleteTextView); 
		stepTimeText = (TextView) findViewById(R.id.stepTimeTextView); 
		elapsedTimeToPlaceText = (TextView) findViewById(R.id.elapsedTimeToPlaceTextView); 
		totalElapsedTimeText = (TextView) findViewById(R.id.totalElapsedTimeTextView); 

		nextDirectionsButton = (Button) findViewById(R.id.nextDirectionsButton); 

		vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		long stepEndTime = System.currentTimeMillis(); 

		completedStep.completeStep(); 
		setStopTime(stepEndTime); 
		setNextStep(); 
		displayMsg(stepEndTime);
		vibrate(vibe);  
		setupNextDirectionsButton(); 

	}

	private void setStopTime(long endTime) {
		completedStep.setEndTime(endTime); 
		if (completedStepIndex >= currentLeg.size() - 1) {
			currentLeg.setEndTime(endTime); 
		}
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
		String msg = null, stepTime= null, legElapsedTime = null, totalElapsedTime = null;

		if (completedStep.equals(currentLeg.finalStep())) {
			msg = "You've arrived at " + runToPlace.getName() + "!"; 
		}
		else {
			msg = "You completed step " + (completedStepIndex + 1) + " on your way to " + runToPlace.getName() + "!";  
		}
		
		legElapsedTime = DroidTime.msToStr(stepEndTime - currentLeg.getStartTime()); 
		totalElapsedTime = DroidTime.msToStr(stepEndTime - runDirections.get(0).getStartTime()); 	
		stepTime = DroidTime.msToStr(stepEndTime - completedStep.getStartTime()); 	

		stepCompleteText.setText(msg); 
		stepTimeText.setText(stepTime); 
		elapsedTimeToPlaceText.setText(legElapsedTime); 
		totalElapsedTimeText.setText(totalElapsedTime);
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
