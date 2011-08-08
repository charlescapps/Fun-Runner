//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun;

import xanthanov.droid.gplace.*;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button; 
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
//	Button aboutButton; 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.title);
		
		//****************Get views by ID**********************
		newRunButton= (Button) findViewById(R.id.newRunButton);
		viewStatsButton = (Button) findViewById(R.id.viewStatsButton); 
//		aboutButton = (Button) findViewById(R.id.aboutButton); 
		//****************End Get Views************************ 

		setupButtons(); 

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
