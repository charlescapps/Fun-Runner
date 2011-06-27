package xanthanov.droid.xantools; 

import android.app.Activity;
import android.content.Context; 
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface; 

public class DroidDialogs {
	private static AlertDialog currentDialog = null; 
	private static ProgressDialog currentProgressDialog = null; 

	public static AlertDialog showPopup(Activity a, String title, String txt) {
		AlertDialog.Builder myBuilder = new AlertDialog.Builder(a); 
		myBuilder.setMessage(txt); 
		myBuilder.setTitle(title); 
		myBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
		   public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
		   }
	   }); 

		final AlertDialog.Builder finalBuilder = myBuilder; 

		a.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				currentDialog = finalBuilder.create(); 
				currentDialog.show(); 
			}
		});

		return currentDialog; 
	}

	public static ProgressRunnable showProgressDialog(Activity activity, String title, String msg) {

		ProgressRunnable pr = new ProgressRunnable(activity, title, msg); 
		activity.runOnUiThread(pr); 

		return pr; 
	}

}
	
