package xanthanov.droid.funrun; 

import xanthanov.droid.funrun.adapters.*; 
import xanthanov.droid.funrun.persist.*; 

import android.app.Activity; 
import android.os.Bundle; 
import android.widget.Gallery; 

public class ViewStatsActivity extends Activity {

	private Gallery statsGallery; 
	private ViewStatsAdapter myAdapter; 
	private FunRunData state; 

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b); 
		setContentView(R.layout.view_stats); 

		//Get the gallery from the layout 
		statsGallery = (Gallery) findViewById(R.id.statsGalleryView);
		state = ((FunRunApplication) getApplicationContext()).getState(); 

		myAdapter = new ViewStatsAdapter(this, state); 
		statsGallery.setAdapter(myAdapter); 
	}
}
