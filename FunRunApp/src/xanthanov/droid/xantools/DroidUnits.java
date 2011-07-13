package xanthanov.droid.xantools; 

import android.text.Spanned; 
import java.text.DecimalFormat; 

public class DroidUnits {

	public final static double metersPerMile = 1609.344; 

	public static Spanned msToStr(long ms) {
		long secs = (ms / 1000) % 60; 
		long mins = (ms / 60000); 
		//long hours = (ms / (60000*60)); 

		return android.text.Html.fromHtml(mins + " <b>m</b>, " + secs + " <b>s</b>");
	}

	public static Spanned msToStrV2(long ms) {
		if (ms <= 0 ) {
			if (ms < 0 ) System.err.println("Error: time was < 0"); 
			return android.text.Html.fromHtml("0 <b>mins</b>, 0 <b>secs</b>"); 
		}
		long secs = (ms / 1000) % 60; 
		long mins = (ms / 60000); 
		//long hours = (ms / (60000*60)); 

		return android.text.Html.fromHtml(mins + " <b>mins</b>, " + secs + " <b>secs</b>");
	}

	public static Spanned getSpeedString(long ms, double distanceMeters) {

		if (ms <= 0 || distanceMeters <= 0.0) {
			return android.text.Html.fromHtml("<b>n/a</b>"); 
		}

		DecimalFormat df = new DecimalFormat("#.##"); 
		double mPerS = distanceMeters / (((double)ms)/1000.0); 

		String html = df.format(mPerS) + " <b>m/s</b>"; 

		return android.text.Html.fromHtml(html); 
	}

	public static Spanned getSpeedStringV2(long ms, double distanceMeters) {
		if (ms <= 0 || distanceMeters <= 0.0) {
			if (ms < 0) System.err.println("Time in ms = " + ms + " was < 0 in getSpeedStringV2"); 

			if (distanceMeters < 0.0) System.err.println("distanceMeters = " + distanceMeters + " was < 0 in getSpeedStringV2"); 
			return android.text.Html.fromHtml("<b>n/a</b>"); 
		}

		DecimalFormat df = new DecimalFormat("#.##"); 
		double mPerS = distanceMeters / (((double)ms)/1000.0); 
		double minPerMile = (1/mPerS)*(1.0/60.0)*metersPerMile; 
		int mins = (int) minPerMile; 
		int secs = (int) (( minPerMile - (double) mins ) * 60.0) % 60; 

		String secsString = (secs < 10 ? "0" + Integer.toString(secs) : Integer.toString(secs)); 

		String html = df.format(mPerS) + " <b>m/s</b>, " + mins + ":" + secsString + " <b>mile</b>"; 

		return android.text.Html.fromHtml(html); 
	}

	public static Spanned getDistanceString(int distanceMeters) {
		double distanceMiles = ((double)distanceMeters) / metersPerMile; 
		DecimalFormat df = new DecimalFormat("#.##"); 
		String html = distanceMeters + " <b>meters</b>, " + df.format(distanceMiles) + " <b>miles</b>"; 
		return android.text.Html.fromHtml(html); 
	}

	public static Spanned getDistanceStringV2(int distanceMeters) {
		double distanceMiles = ((double)distanceMeters) / metersPerMile; 
		DecimalFormat df = new DecimalFormat("#.##"); 
		String html = distanceMeters + " <b>meters</b>,<br/>" + df.format(distanceMiles) + " <b>miles</b>"; 
		return android.text.Html.fromHtml(html); 
	}

	public static Spanned getDistanceStringV3(double distanceMeters) {
		if (distanceMeters < 0.0) {
			System.err.println("Negative distanceMeters in getDistanceStringV3: " + distanceMeters); 
			return android.text.Html.fromHtml("<b>n/a</b>"); 
		}
		double distanceMiles = distanceMeters / metersPerMile; 
		DecimalFormat df = new DecimalFormat("#.##"); 
		String html = df.format(distanceMeters) + " <b>m</b>, " + df.format(distanceMiles) + " <b>mi</b>"; 
		return android.text.Html.fromHtml(html); 
	}
}

