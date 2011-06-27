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

public class StepCompleteActivity extends Activity {

	private GoogleDirections runDirections;
	private GoogleStep completedStep;
	private GoogleLeg currentLeg; 
	private GooglePlace runToPlace; 
	private int stepNo;  
	private FunRunApplication funRunApp; 

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
		completedStep = funRunApp.getCurrentStep(); 
		runToPlace = funRunApp.getRunPlace(); 
		runDirections = funRunApp.getRunDirections(); 
		currentLeg = runDirections.lastLeg(); 
		stepNo = currentLeg.getSteps().indexOf(completedStep) + 1; 

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
		currentLeg.setEndTime(endTime); 
	}

	private void setNextStep() {
		if (completedStep.equals(currentLeg.finalStep())) {
			funRunApp.setCurrentStep(null); 
			funRunApp.setRunPlace(null); 
		}
		else {
			int nextIndex = currentLeg.getSteps().indexOf(completedStep) + 1; 
			funRunApp.setCurrentStep(currentLeg.get(nextIndex)); 
		}
	}

	private void displayMsg(long stepEndTime) {
		String msg = null, stepTime= null, legElapsedTime = null, totalElapsedTime = null;

		if (completedStep.equals(currentLeg.finalStep())) {
			msg = "You've arrived at " + runToPlace.getName() + "!"; 
		}
		else {
			msg = "You completed step " + stepNo + " on your way to " + runToPlace.getName() + "!";  
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
		if (completedStep.equals(currentLeg.finalStep())) {
			nextDirectionsButton.setText("Choose next destination"); 

			nextDirectionsButton.setOnClickListener(new View.OnClickListener() {
				@Override 
				public void onClick(View v) {
					funRunApp.setKillRunActivity(true); 
					finish(); 
				}
			});
			//Go back to place selection screen	

		}
		else {
			nextDirectionsButton.setText("Get next directions"); 

			nextDirectionsButton.setOnClickListener(new View.OnClickListener() {
				@Override 
				public void onClick(View v) {
					finish(); 
				}
			});
		}
	}

	private static void vibrate(Vibrator v) {
		v.vibrate(VIB_PATTERN, -1);  
	}
}
