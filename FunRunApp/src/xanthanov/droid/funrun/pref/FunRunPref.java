package xanthanov.droid.funrun.pref;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import xanthanov.droid.funrun.R; 

public class FunRunPref extends PreferenceActivity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_screen);

	}

	@Override
	public void onDestroy() {
		super.onDestroy(); 

		Toast.makeText(this, "Preferences saved" , Toast.LENGTH_SHORT).show(); 
		
	}
/*
	@Override
	public void onContentChanged() {
		super.onContentChanged(); 
		Toast.makeText(this, "Preferences saved" , Toast.LENGTH_SHORT).show(); 
		

	}
*/
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.simple_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_back:
				this.finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
