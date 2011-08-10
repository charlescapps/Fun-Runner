//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.xantools; 

import java.net.HttpURLConnection; 
import java.net.URL; 
import java.io.IOException;
import java.io.InputStream; 
import android.graphics.Bitmap; 
import android.graphics.BitmapFactory; 

/**
* <h3>Class to download a bitmap from a URL and store in android.graphics.Bitmap object.</h3>
*
* <b>Class not being used at the moment. Most Google Places icons just have the default graphic, and besides this
* would slow down the app. </b>
*
*@version 0.9b
*@author Charles L. Capps
**/

public class DroidBitmapDownload {

	public static Bitmap getBitmapFromURL(URL bitmapURL) {
		try {
			HttpURLConnection connection = (HttpURLConnection) bitmapURL.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
