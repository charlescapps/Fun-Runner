package xanthanov.droid.xantools; 

import android.app.Activity;
import android.content.Context; 
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface; 

/**
*<p>
*Convenience class for working with dialogs. Allows you to start a dialog from anywhere, even 
*a non-UI thread doing some heavy-lifting. That is, provided you pass in the Activity so that it 
*can properly start the dialog on the UI thread. 
*
*Used to get around my frustrations when I wanted my AsyncTask code to determine where a dialog box 
*popped up--but you can't start a dialog outside of the UI thread! 
*
*</p>
*
*@author Charles L. Capps
*@version 0.9b
*
**/

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

	public static AlertDialog showPopup(Activity a, String title, String txt, String posText, String negText, 
											DialogInterface.OnClickListener posListen, DialogInterface.OnClickListener negListen) {
		AlertDialog.Builder myBuilder = new AlertDialog.Builder(a); 
		myBuilder.setMessage(txt); 
		myBuilder.setTitle(title); 
		myBuilder.setPositiveButton(posText, posListen); 
		myBuilder.setNegativeButton(negText, negListen); 

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

	public static AlertDialog showPopup(Activity a, boolean cancelable, String title, String txt, String posText, String negText, 
											DialogInterface.OnClickListener posListen, DialogInterface.OnClickListener negListen) {
		final AlertDialog.Builder myBuilder = new AlertDialog.Builder(a); 
		myBuilder.setMessage(txt); 
		myBuilder.setTitle(title); 
		myBuilder.setPositiveButton(posText, posListen); 
		myBuilder.setNegativeButton(negText, negListen); 
		myBuilder.setCancelable(cancelable); 

		a.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				currentDialog = myBuilder.create(); 
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
	
