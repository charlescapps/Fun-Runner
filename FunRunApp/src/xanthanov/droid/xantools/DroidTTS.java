package xanthanov.droid.xantools; 

import java.util.HashMap; 

public class DroidTTS {

	private static HashMap<String,String> abbrevToFull; 

	public DroidTTS() {
		abbrevToFull = new HashMap<String,String>(); 

		abbrevToFull.put("N", "North"); 	
		abbrevToFull.put("S", "South"); 	
		abbrevToFull.put("E", "East"); 	
		abbrevToFull.put("W", "West"); 	
		abbrevToFull.put("NE", "Northeast"); 	
		abbrevToFull.put("NW", "Northwest"); 	
		abbrevToFull.put("SE", "Southeast"); 	
		abbrevToFull.put("SW", "Southwest"); 	
		abbrevToFull.put("Ave", "Avenue"); 	
		abbrevToFull.put("St", "Street"); 	
		abbrevToFull.put("Ct", "Court"); 	
		abbrevToFull.put("Blvd", "Boulevard"); 	
	}


	public static String expandDirectionsString(String dir) {
		
		String[] tokens = dir.split(" "); 

		System.out.println("Directions tokens:"); 
		for (String tok: tokens) {
			System.out.println(tok); 
		}

		for (int i = 0; i < tokens.length; i++) {
			String fullWord = abbrevToFull.get(tokens[i]); 

			if (fullWord != null) {
				tokens[i] = fullWord; 
			}
		}

		String result = ""; 

		for (String s: tokens) {
			result += (s + " "); 
		}

		System.out.println("Result of expanding abbreviations; " + result); 

		return result; 
	}
	


}
