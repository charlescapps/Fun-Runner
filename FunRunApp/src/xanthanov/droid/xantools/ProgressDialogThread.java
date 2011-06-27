package xanthanov.droid.xantools; 

import android.app.ProgressDialog; 
import android.content.Context; 
import android.os.Handler;
import android.os.Message;


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
