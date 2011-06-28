package xanthanov.droid.xantools; 

import java.net.HttpURLConnection; 
import java.net.URL; 
import java.io.IOException;
import java.io.InputStream; 
import android.graphics.Bitmap; 
import android.graphics.BitmapFactory; 

//Credit jwei512 from thinkandroid.wordpress.com (though I really just needed to know that BitmapFactory.decodeStream existed!) 

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
