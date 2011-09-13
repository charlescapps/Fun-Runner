package xanthanov.droid.funrun.pref; 

import android.preference.PreferenceActivity; 
import android.os.Bundle; 
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

	

}
