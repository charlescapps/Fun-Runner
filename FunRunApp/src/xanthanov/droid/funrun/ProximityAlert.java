package xanthanov.droid.funrun; 

import android.content.Context; 
import android.content.BroadcastReceiver;
import android.content.Intent; 

public class ProximityAlert extends BroadcastReceiver {

	public static final String STEP_EXTRA = "stepno"; 
	@Override
	public void onReceive(Context c, Intent i) {
		int stepNo = i.getIntExtra(STEP_EXTRA, -1); 
		System.out.println("IntExtra inside ProximityAlert: " + stepNo); 

		Intent stepCompleteIntent = new Intent(c, StepCompleteActivity.class); 
		stepCompleteIntent.putExtra(STEP_EXTRA, stepNo); 
		c.startActivity(stepCompleteIntent); 
	}

}
