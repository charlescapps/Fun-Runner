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

public class ViewStatsActivity extends Activity {

	private Gallery statsGallery; 
	private ViewStatsAdapter myAdapter; 
	private FunRunData state; 
	private DroidLayout droidLay; 
	private Button returnButton; 
	private Button loadOnMapButton; 
	private Toast toastRunNumber; 

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b); 
		setContentView(R.layout.view_stats); 

		droidLay = new DroidLayout(this); 

		returnButton = (Button)findViewById(R.id.backToTitle); 
		loadOnMapButton = (Button)findViewById(R.id.loadRunOnMap); 

		setupButtons(); 

		//Get the gallery from the layout 
		statsGallery = (Gallery) findViewById(R.id.statsGallery);
		state = ((FunRunApplication) getApplicationContext()).getState(); 

		myAdapter = new ViewStatsAdapter(this, state, statsGallery); 

		toastRunNumber = Toast.makeText(this, "Run " + (1)  + " of " + myAdapter.getCount(), Toast.LENGTH_SHORT); 

		statsGallery.setAdapter(myAdapter); 
		statsGallery.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView parent, View v, int position, long id) {
		/*			Toast tmp = new Toast(theContext); 
					tmp.setView(droidLay.inflateView(R.layout.toast_view)); 
					TextView tv = (TextView)tmp.getView(); 
					tv.setText("Run " + (position + 1)  + " of " + myAdapter.getCount() + "\nScroll <-->"); 
					tmp.show(); 
		*/
					
					toastRunNumber.setText("Run " + (position + 1)  + " of " + myAdapter.getCount()); 
					toastRunNumber.show(); 
				}
			});
	}

	private void setupButtons() {
		returnButton.setOnClickListener(new ClickReturn()); 
		loadOnMapButton.setOnClickListener(new ClickLoadOnMap()); 
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
			ViewStatsActivity.this.startActivity(showMap); 

		}
	}
}
