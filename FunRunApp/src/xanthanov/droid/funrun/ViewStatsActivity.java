//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun; 

import xanthanov.droid.funrun.adapters.*; 
import xanthanov.droid.funrun.persist.*; 
import xanthanov.droid.xantools.DroidLayout; 

import android.app.Activity; 
import android.os.Bundle; 
import android.widget.Gallery; 
import android.widget.AdapterView.OnItemClickListener; 
import android.widget.AdapterView; 
import android.widget.Toast; 
import android.widget.TextView; 
import android.widget.Button; 
import android.widget.LinearLayout; 
import android.view.View; 
import android.content.Context; 
import android.content.Intent; 

/**
*<h3>Activity for viewing old runs in a gallery view. </h3>
*<ul>
*<li>Has a &quot;Load Map&quot; button to view the run on a map -> ViewOldRunActivity. </li>
*<li>Uses Intent &quot;Extras&quot; to send the index of the run we're viewing to the ViewOldRunActivity.</li> 
*</ul>
*
*<h3>TODO</h3>
*<ul>
*<li>Figure out how to send any data type through an intent. Not really necessary but a good idea.</li>
*<li>Give an option to send a run to your email, or to post on Facebook (Google+?)</li>
*<li>Figure out how to customize the gallery view background more. May be difficult since it magically highlights when you touch it, etc.</li>
*<li>Obviously fix the disappearing arrow bug. Been lazy about this, it's probably just due to the state drawable.</li>
*</ul>
*
*@author Charles L. Capps
*
*@version 0.9b
**/

public class ViewStatsActivity extends Activity {

	private Gallery statsGallery; 
	private ViewStatsAdapter myAdapter; 
	private FunRunData state; 
	private DroidLayout droidLay; 
	private Button returnButton; 
	private Button loadOnMapButton; 
	private Toast toastRunNumber; 

	public static final String RUN_INDEX_EXTRA = "RUN_INDEX_EXTRA"; 

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b); 
		setContentView(R.layout.view_stats); 

		droidLay = new DroidLayout(this); 

		returnButton = (Button)findViewById(R.id.backToTitle); 
		loadOnMapButton = (Button)findViewById(R.id.loadRunOnMap); 


		//Get the gallery from the layout 
		statsGallery = (Gallery) findViewById(R.id.statsGallery);
		state = ((FunRunApplication) getApplicationContext()).getState(); 

		myAdapter = new ViewStatsAdapter(this, state, statsGallery); 

		toastRunNumber = Toast.makeText(this, "Run " + (1)  + " of " + myAdapter.getCount(), Toast.LENGTH_SHORT); 

		statsGallery.setAdapter(myAdapter); 
		statsGallery.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView parent, View v, int position, long id) {
					toastRunNumber.setText("Run " + (position + 1)  + " of " + myAdapter.getCount()); 
					toastRunNumber.show(); 
				}
			});
		
		setupButtons(); 
		
	}

	private void setupButtons() {

		returnButton.setOnClickListener(new ClickReturn()); 
		if (myAdapter.isEmpty()) {
			loadOnMapButton.setEnabled(false); 
		}
		else {		
			loadOnMapButton.setOnClickListener(new ClickLoadOnMap()); 
		}
	}

	//Nested classes for buttons
	class ClickReturn implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			ViewStatsActivity.this.finish(); 
		}
	}

	class ClickLoadOnMap implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Intent showMap = new Intent(ViewStatsActivity.this, ViewOldRunActivity.class); 
			showMap.putExtra(RUN_INDEX_EXTRA, statsGallery.getLastVisiblePosition()); 
			ViewStatsActivity.this.startActivity(showMap); 

		}
	}
}
