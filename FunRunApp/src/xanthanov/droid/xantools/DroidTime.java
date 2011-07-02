package xanthanov.droid.xantools; 

import android.text.Spanned; 
import java.text.DecimalFormat; 

public class DroidTime {

	public final static double metersPerMile = 609.344; 

	public static Spanned msToStr(long ms) {
		long secs = (ms / 1000) % 60; 
		long mins = (ms / 60000); 
		//long hours = (ms / (60000*60)); 

		return android.text.Html.fromHtml(mins + " <b>m</b>, " + secs + " <b>s</b>");
	}

	public static Spanned getSpeedString(long ms, int distanceMeters) {
		DecimalFormat df = new DecimalFormat("#.##"); 
		double mPerS = ((double)distanceMeters) / (((double)ms)/1000); 

		String html = df.format(mPerS) + " <b>m/s</b>"; 

		return android.text.Html.fromHtml(html); 

	}

	public static Spanned getDistanceString(int distanceMeters) {
		double distanceMiles = ((double)distanceMeters) / metersPerMile; 
		DecimalFormat df = new DecimalFormat("#.##"); 
		String html = distanceMeters + " <b>meters</b>, " + df.format(distanceMiles) + " <b>miles</b>"; 
		return android.text.Html.fromHtml(html); 
	}
}

