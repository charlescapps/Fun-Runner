package xanthanov.droid.xantools; 

import android.view.LayoutInflater; 
import android.view.View; 
import android.content.Context; 

public class DroidLayout {

	private Context context; 
	private LayoutInflater inflater; 

	public DroidLayout(Context c) {this.context = c; this.inflater = LayoutInflater.from(context);  }
	
	public View inflateView(int resId) {
		
		return inflater.inflate(resId, null); 
	}

}
