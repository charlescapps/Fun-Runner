//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import xanthanov.droid.gplace.GoogleDirections;
import xanthanov.droid.gplace.GoogleLeg;
import xanthanov.droid.gplace.GooglePlace;
import xanthanov.droid.gplace.GoogleStep;
import xanthanov.droid.xantools.DroidDialogs;
import xanthanov.droid.xantools.DroidUnits;

import java.util.HashMap;

/**
* <h3>Activity for when user completes a step in the directions, or arrives at a place.</h3>
*
* <b>Some important things about this activity:</b>
* <ul>
* <li>It's started by FunRunActivity when the user completes a Google walking directions step, or arrives at a place.</li>
* <li>It receives an Intent &quot;Extra&quot; so it knows which step was completed. </li>
* <li> It speaks a message: &quot;Step Complete!&quot; or &quot;Congratulations! You ran to x!&quot; </li>
* <li>Displays your stats: total distance, overall avg. speed, distance this leg, avg. speed this leg.</li>
* <li>TODO: Add hotkey to hear next directions, so runner doesn't have to bust out phone a press a button on screen </li>
* <li>Simplify? I hate the amount of code required to add messages to text views, etc.</li>
* </ul>
*
* @author Charles L. Capps
* @version 0.9b
**/

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

	private TextView pointsText; 
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
	private final static long[] VIB_PATTERN = new long[] {500, 500, 500, 500, 500, 500, 500, 500}; 

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
			/*
			rootLayout = (LinearLayout) findViewById(R.id.stepCompleteLayout); 
			rootLayout.setBackgroundResource(R.drawable.congratulations2); 
			stepCompleteText = (TextView) findViewById(R.id.stepCompleteTextView); 
			stepCompleteText.setTextSize(40.0f); 
			stepCompleteText.setText(android.text.Html.fromHtml("<b>Starbucks</b>!"));  
			*/
			//finish(); 
			return; 
		}	  

		runDirections = funRunApp.getRunDirections(); 
		currentLeg = runDirections.lastLeg(); 
		runToPlace = currentLeg.getLegDestination(); 

		completedStep = currentLeg.get(completedStepIndex); 

		rootLayout = (LinearLayout) findViewById(R.id.stepCompleteLayout); 
		//stepCompleteTitle = (TextView) findViewById(R.id.stepCompleteTitle); 
		stepCompleteText = (TextView) findViewById(R.id.stepCompleteTextView); 

		legDistanceTitle = (TextView) findViewById(R.id.legDistanceTitle); 
		totalDistanceTitle = (TextView) findViewById(R.id.totalDistanceTitle); 
		avgSpeedTitle = (TextView) findViewById(R.id.avgSpeedTitle); 
		avgSpeedRunTitle = (TextView) findViewById(R.id.avgSpeedRunTitle); 
		elapsedTimeToPlaceTitle = (TextView) findViewById(R.id.elapsedTimeToPlaceTitle); 
		totalElapsedTimeTitle = (TextView) findViewById(R.id.totalElapsedTimeTitle); 

		pointsText = (TextView) findViewById(R.id.pointsTextView); 
		legDistanceText = (TextView) findViewById(R.id.legDistanceTextView); 
		totalDistanceText = (TextView) findViewById(R.id.totalDistanceTextView); 
		avgSpeedText = (TextView) findViewById(R.id.avgSpeedTextView); 
		avgSpeedRunText = (TextView) findViewById(R.id.avgSpeedRunTextView); 
		elapsedTimeToPlaceText = (TextView) findViewById(R.id.elapsedTimeToPlaceTextView); 
		totalElapsedTimeText = (TextView) findViewById(R.id.totalElapsedTimeTextView); 

		nextDirectionsButton = (Button) findViewById(R.id.nextDirectionsButton); 

		vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

		if ( currentLeg.gotToDestination()) {
			//stepCompleteTitle.setText("Congratulations!"); 
			rootLayout.setBackgroundResource(R.drawable.congratulations2); 
			stepCompleteText.setTextSize(25.0f); //Make text bigger if we arrived at a place, since it just needs to display the name
		}
		else {
			rootLayout.setBackgroundResource(R.drawable.congratulations); 
		}

		setNextStep(); 
		displayMsg();
		vibrate(vibe);  
		setupNextDirectionsButton(); 
	//	setTransparency(); 

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

			if (completedStepIndex < currentLeg.size() - 1) {
				HashMap<String,String> params = new HashMap<String,String> (); 
				params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FIRST_SPEECH"); 
				myTts.playSilence(300, TextToSpeech.QUEUE_ADD, null); 
				myTts.speak("Press volume up to get the next directions.", TextToSpeech.QUEUE_ADD, params); 
			}

		}
	}

	@Override
	public boolean onKeyDown( int keycode, KeyEvent e) {		
	//	super.onKeyDown(keycode, e); 

		if (keycode == KeyEvent.KEYCODE_VOLUME_UP || keycode == KeyEvent.KEYCODE_BACK) { //Go back to run screen and get the next directions
			finish(); 	
			return true; 
		}
		return false; 
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
		double legDistanceMeters = currentLeg.getActualDistanceRan(); 
		double totalDistanceMeters = runDirections.getDistanceSoFar(); 

		legDistance = DroidUnits.getDistanceStringV3(legDistanceMeters); 
		totalDistance = DroidUnits.getDistanceStringV3(totalDistanceMeters); 
		
		legElapsedTime = DroidUnits.msToStr(currentLeg.getEndTime() - currentLeg.getStartTime()); 
		totalElapsedTime = DroidUnits.msToStr(currentLeg.getEndTime() - runDirections.get(0).getStartTime()); 	

		//Points
		int pts = currentLeg.getLegPoints(); 
		java.text.NumberFormat nf = java.text.NumberFormat.getInstance(); 

		//Avg. speed in m/s 
		avgSpeed = DroidUnits.getSpeedStringV2(completedStep.getEndTime() - currentLeg.getStartTime(), legDistanceMeters); 
		avgSpeedRun = DroidUnits.getSpeedStringV2(completedStep.getEndTime() - runDirections.get(0).getStartTime(), totalDistanceMeters); 

		//Set text views.
		pointsText.setText(nf.format(pts) + " points!"); 
		stepCompleteText.setText(msg); 
		legDistanceText.setText(legDistance); 
		totalDistanceText.setText(totalDistance); 
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
