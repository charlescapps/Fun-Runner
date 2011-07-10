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
import android.widget.LinearLayout; 
import android.content.Intent;
import android.text.Spanned;
import android.view.animation.Animation; 
import android.view.animation.AlphaAnimation;

import android.speech.tts.TextToSpeech; 

public class StepCompleteActivity extends Activity {

	private GoogleStep completedStep = null; 
	private GoogleDirections runDirections;
	private GoogleLeg currentLeg; 
	private GooglePlace runToPlace; 
	private FunRunApplication funRunApp; 
	private int completedStepIndex = -1; 

	//private TextView stepCompleteTitle;
	private TextView stepCompleteText; 

	private TextView legDistanceTitle; 
	private TextView totalDistanceTitle; 
	private TextView avgSpeedTitle; 
	private TextView avgSpeedRunTitle; 
	private TextView elapsedTimeToPlaceTitle; 
	private TextView totalElapsedTimeTitle; 

	private TextView legDistanceText; 
	private TextView totalDistanceText; 
	private TextView avgSpeedText; 
	private TextView avgSpeedRunText; 
	private TextView elapsedTimeToPlaceText; 
	private TextView totalElapsedTimeText; 

	private Button nextDirectionsButton; 
	private LinearLayout rootLayout; 

	private TextToSpeech myTts; 

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

		rootLayout = (LinearLayout) findViewById(R.id.stepCompleteLayout); 
		//stepCompleteTitle = (TextView) findViewById(R.id.stepCompleteTitle); 
		stepCompleteText = (TextView) findViewById(R.id.stepCompleteTextView); 

		legDistanceTitle = (TextView) findViewById(R.id.legDistanceTitle); 
		totalDistanceTitle = (TextView) findViewById(R.id.totalDistanceTitle); 
		avgSpeedTitle = (TextView) findViewById(R.id.avgSpeedTitle); 
		avgSpeedRunTitle = (TextView) findViewById(R.id.avgSpeedRunTitle); 
		elapsedTimeToPlaceTitle = (TextView) findViewById(R.id.elapsedTimeToPlaceTitle); 
		totalElapsedTimeTitle = (TextView) findViewById(R.id.totalElapsedTimeTitle); 

		legDistanceText = (TextView) findViewById(R.id.legDistanceTextView); 
		totalDistanceText = (TextView) findViewById(R.id.totalDistanceTextView); 
		avgSpeedText = (TextView) findViewById(R.id.avgSpeedTextView); 
		avgSpeedRunText = (TextView) findViewById(R.id.avgSpeedRunTextView); 
		elapsedTimeToPlaceText = (TextView) findViewById(R.id.elapsedTimeToPlaceTextView); 
		totalElapsedTimeText = (TextView) findViewById(R.id.totalElapsedTimeTextView); 

		nextDirectionsButton = (Button) findViewById(R.id.nextDirectionsButton); 

		vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

		if (completedStepIndex >= currentLeg.size() - 1) {
			//stepCompleteTitle.setText("Congratulations!"); 
			rootLayout.setBackgroundResource(R.drawable.congratulations2); 
			currentLeg.setGotToDestination(true); 
		}
		else {
			rootLayout.setBackgroundResource(R.drawable.congratulations); 
		}

		completedStep.completeStep(); 
		setNextStep(); 
		displayMsg();
		vibrate(vibe);  
		setupNextDirectionsButton(); 
		setTransparency(); 

		myTts = funRunApp.getTextToSpeech(); 

		if (funRunApp.isTtsReady()) {
			String toSpeak = null; 

			if (completedStepIndex >= currentLeg.size() - 1) {
				toSpeak = "Congratulations! You ran to " + currentLeg.getLegDestination().getName() + "!"; 
			}
			else {
				toSpeak = "Step complete!"; 
			}			
			myTts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null); 

		}
	}

	private void setTransparency() {
		Animation animation = new AlphaAnimation(1.0f, 0.7f);
		animation.setFillAfter(true);

		legDistanceTitle.getBackground().setAlpha(100);
		totalDistanceTitle.getBackground().setAlpha(100);
		avgSpeedTitle.getBackground().setAlpha(100);
		avgSpeedRunTitle.getBackground().setAlpha(100);
		elapsedTimeToPlaceTitle.getBackground().setAlpha(100);
		totalElapsedTimeTitle.getBackground().setAlpha(100);

		legDistanceText.getBackground().setAlpha(100);
		totalDistanceText.getBackground().setAlpha(100);
		avgSpeedText.getBackground().setAlpha(100);
		avgSpeedRunText.getBackground().setAlpha(100);
		elapsedTimeToPlaceText.getBackground().setAlpha(100);
		totalElapsedTimeText.getBackground().setAlpha(100);
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

	private void displayMsg() {
		Spanned msg = null,
			legDistance = null, 
			totalDistance = null, 
			avgSpeed= null, 
			avgSpeedRun = null, 
			legElapsedTime = null, 
			totalElapsedTime = null;

		if (completedStepIndex>=currentLeg.size() - 1) {
			msg = android.text.Html.fromHtml("<b>" + runToPlace.getName() + "</b>"); 
		}
		else {
			msg = android.text.Html.fromHtml("You completed step " + (completedStepIndex + 1) + " on the way to <b>" + runToPlace.getName() + "</b>!");  
		}

		//Distance this leg and total distance overall
		int legDistanceMeters = currentLeg.getDistanceSoFar(); 
		int totalDistanceMeters = runDirections.getDistanceSoFar(); 

		legDistance = DroidUnits.getDistanceStringV3(legDistanceMeters); 
		totalDistance = DroidUnits.getDistanceStringV3(totalDistanceMeters); 
		
		legElapsedTime = DroidUnits.msToStr(currentLeg.getEndTime() - currentLeg.getStartTime()); 
		totalElapsedTime = DroidUnits.msToStr(currentLeg.getEndTime() - runDirections.get(0).getStartTime()); 	


		//Avg. speed in m/s 
		avgSpeed = DroidUnits.getSpeedStringV2(completedStep.getEndTime() - currentLeg.getStartTime(), legDistanceMeters); 
		avgSpeedRun = DroidUnits.getSpeedStringV2(completedStep.getEndTime() - runDirections.get(0).getStartTime(), totalDistanceMeters); 

		//Set text views.
		legDistanceText.setText(legDistance); 
		totalDistanceText.setText(totalDistance); 
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
