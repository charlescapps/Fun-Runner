package xanthanov.droid.xantools;

import android.app.Activity;
import android.content.Context; 
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface; 

public class ProgressRunnable implements Runnable {
	private ProgressDialog pd = null; 
	private Activity activity = null; 
	private String title = null; 
	private String msg = null; 

	public ProgressRunnable(Activity a, String t, String m) {
		super(); 
		this.activity = a; 
		this.title = t; 
		this.msg = m; 
	}

	@Override
	public void run() {
		pd = ProgressDialog.show(activity, title, msg, true, true); 
	}

	public boolean dismissDialog() {
		if (pd != null) {
			pd.dismiss();
			return true;  
		}
		return false; 
	}

}
