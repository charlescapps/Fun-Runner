package xanthanov.droid.xantools; 

import android.content.Context; 
import android.app.AlertDialog;
import android.content.DialogInterface; 

public class DroidDialogs {

	public static void showPopup(Context c, String title, String txt) {
		AlertDialog.Builder myBuilder = new AlertDialog.Builder(c); 
		myBuilder.setMessage(txt); 
		myBuilder.setTitle(title); 
		myBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
           }
       }); 

		AlertDialog popup = myBuilder.create(); 
		popup.show(); 
	}

}
	
