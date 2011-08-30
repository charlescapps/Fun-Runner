package xanthanov.droid.funrun.pref; 

import android.preference.PreferenceActivity; 
import android.os.Bundle; 

import xanthanov.droid.funrun.R; 

public class FunRunPref extends PreferenceActivity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setContentView(R.layout.preference_screen); 
	}

	

}
