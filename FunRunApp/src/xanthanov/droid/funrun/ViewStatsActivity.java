package xanthanov.droid.funrun; 

import xanthanov.droid.funrun.adapters.*; 
import xanthanov.droid.funrun.persist.*; 

import android.app.Activity; 
import android.os.Bundle; 
import android.widget.Gallery; 
import android.widget.AdapterView.OnItemClickListener; 
import android.widget.AdapterView; 
import android.widget.Toast; 
import android.view.View; 

public class ViewStatsActivity extends Activity {

	private Gallery statsGallery; 
	private ViewStatsAdapter myAdapter; 
	private FunRunData state; 

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b); 
		setContentView(R.layout.view_stats); 

		//Get the gallery from the layout 
		statsGallery = (Gallery) findViewById(R.id.statsGallery);
		state = ((FunRunApplication) getApplicationContext()).getState(); 

		myAdapter = new ViewStatsAdapter(this, state, statsGallery); 
		statsGallery.setAdapter(myAdapter); 
		statsGallery.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView parent, View v, int position, long id) {
					Toast.makeText(ViewStatsActivity.this, "" + position, Toast.LENGTH_SHORT).show();
				}
			});
	}
}
