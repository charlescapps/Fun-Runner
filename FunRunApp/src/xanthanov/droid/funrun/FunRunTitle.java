package xanthanov.droid.funrun;

import xanthanov.droid.gplace.*;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button; 
import android.content.Intent; 
import android.content.Context; 
import android.view.View;

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

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

}
