package xanthanov.droid.xantools; 

public class DroidTime {

public static String msToStr(long ms) {
	long secs = (ms / 1000) % 60; 
	long mins = (ms / 60000) % 60; 
	long hours = (ms / 60000*60); 

	return hours + " h, " + mins + " m, " + secs + " s";
}

}

