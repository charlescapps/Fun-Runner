//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.xantools; 

import android.view.LayoutInflater; 
import android.view.View; 
import android.content.Context; 

/**
*<h3>Convenience class for inflating views</h3>
* <b>A bit unnecessary, just used to save a few lines of code. May expand on this class later to make
* it more useful. </b>
*
*
* @version 0.9b
* @author Charles L. Capps
**/

public class DroidLayout {

	private Context context; 
	private LayoutInflater inflater; 

	public DroidLayout(Context c) {this.context = c; this.inflater = LayoutInflater.from(context);  }
	
	public View inflateView(int resId) {
		
		return inflater.inflate(resId, null); 
	}

}
