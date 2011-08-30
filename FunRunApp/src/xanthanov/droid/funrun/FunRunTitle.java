//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun;

import xanthanov.droid.gplace.*;
import xanthanov.droid.funrun.db.FunRunReadOps; 

import java.sql.SQLException; 

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button; 
import android.widget.TextView; 
import android.content.Intent; 
import android.content.Context; 
import android.view.View;
import android.view.MotionEvent;

/**
* <h3>Activity for Title Screen</h3>
*
* Simplest activity. Just sets up its buttons to call ChoosePlaceActivity and ViewStatsActivity. <br/>
* On destroy it calls TextToSpeech.shutdown(), because the app is destroyed if the opening activity is destroyed. <br/>
* As a note, the Android API specifically says there's no way to run arbitrary user code when the app is destroyed. <br/>
* So it only makes sense to do cleanup when the base activity is destroyed. TODO: see if there's a better solution. <br />
* Might add an &quot;about&quot; button to give Copyright info, etc. but not strictly necessary. <br />
* Might add some other button I come up with, e.g. link to Facebook. 
* 
* @author Charles L. Capps
* @version 0.9b
**/

public class FunRunTitle extends Activity
{
	Button newRunButton; 
	Button viewStatsButton; 
	long totalPoints; 

	TextView pointsTextView; 

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.title);

		totalPoints = 0; 
		
		//****************Get views by ID**********************
		newRunButton= (Button) findViewById(R.id.newRunButton);
		viewStatsButton = (Button) findViewById(R.id.viewStatsButton); 
		pointsTextView = (TextView) findViewById(R.id.titlePoints); 
		//****************End Get Views************************ 

		setupButtons(); 

		FunRunReadOps dbReader = new FunRunReadOps(this); 

		try {
			totalPoints = dbReader.getTotalPoints(); 
		}
		catch (SQLException e) {
			System.err.println("ERROR getting total points from database."); 
			e.printStackTrace(); 
		}

		pointsTextView.setText(" " + totalPoints); 
    }

	@Override
	public void onDestroy() {
		super.onDestroy(); 
		FunRunApplication app = (FunRunApplication)getApplicationContext();
		app.getTextToSpeech().shutdown(); 
	}

	@Override 
	public void onStop() {
		super.onStop(); 
		
	}

	private void setupButtons() {
		
		final Intent startRunIntent = new Intent(this, ChoosePlaceActivity.class); 

		newRunButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(startRunIntent); 
				}
			});	

		final Intent viewStatsIntent = new Intent(this, ViewStatsActivity.class); 
		viewStatsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(viewStatsIntent); 				
			}
		}); 

	}
}
