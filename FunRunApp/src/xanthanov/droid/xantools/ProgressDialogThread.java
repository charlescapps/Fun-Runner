//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.xantools;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
*<h3>Helper class for DroidDialogs. </h3>
*
*<p>
*The main purpose of DroidDialogs is to allow the client program to start popups from any thread. 
* For example, in the Fun Runner ChoosePlaceActivity, there are some dialogs a non-UI thread needs to display--
* the UI thread can't really know when to show them. 
* So we pass in the Activity to the DroidDialogs class and call the android runOnUIThread method. 
* This is just the thread that displays the dialog. 
* </p>
*
*@version 0.9b
*@author Charles L. Capps
**/


public class ProgressDialogThread extends Thread {

	private ProgressDialog pd = null; 
	private Context c = null;
	private String title; 
	private String msg; 
	private boolean indeterminate;  

	public ProgressDialogThread(Context c, String title, String msg, boolean indeterminate) {
		this.c = c; 
		this.title = title; 
		this.msg = msg; 
		this.indeterminate = indeterminate; 
	}

	@Override 
	public void run() {
		pd = ProgressDialog.show(c, title, msg, indeterminate); 
	}

	public void dismissDialog() {handler.sendEmptyMessage(0); }
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pd.dismiss();

	}
};

}
