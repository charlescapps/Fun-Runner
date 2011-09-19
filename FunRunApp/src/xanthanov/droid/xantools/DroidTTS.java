//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.xantools; 

import java.util.HashMap; 

/**
* <h3>Class to assist in using text-to-speech with Google Walking directions.</h3>
*
* Main purpose is to convert abbreviations into text-to-speach readable text. 
*
* May add more features later as needed. 
*
* @version 0.9b
* @author Charles L. Capps
**/

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
		abbrevToFull.put("Pl", "Place"); 	
		abbrevToFull.put("Hwy", "Highway"); 	
		abbrevToFull.put("Rd", "Road"); 	
		abbrevToFull.put("Aly", "Alley"); 	
		abbrevToFull.put("Dr", "Drive"); 	
		abbrevToFull.put("Gdns", "Gardens"); 	
		abbrevToFull.put("Trl", "Trail"); 	
		abbrevToFull.put("Pthwy", "Pathway"); 	
		abbrevToFull.put("Vw", "View"); 	
		abbrevToFull.put("Circ", "Circle"); 	
		abbrevToFull.put("Hts", "Heights"); 	
		abbrevToFull.put("Pkwy", "Parkway"); 	
		abbrevToFull.put("Plz", "Plaza"); 	
		abbrevToFull.put("Byp", "Bypass"); 	
		abbrevToFull.put("Hl", "Hill"); 	
		abbrevToFull.put("Mt", "Mountain"); 	
		abbrevToFull.put("Knl", "Knoll"); 	
		abbrevToFull.put("Stra", "Stravenue"); 	
		abbrevToFull.put("Cswy", "Causeway"); 	
		abbrevToFull.put("Grv", "Grove"); 	
		abbrevToFull.put("Cyn", "Canyon"); 	
		abbrevToFull.put("Cv", "Cove"); 	
		abbrevToFull.put("Sq", "Square"); 	
		abbrevToFull.put("Grn", "Green"); 	
		abbrevToFull.put("Mnr", "Manor"); 	
		abbrevToFull.put("Psge", "Passage"); 	
	}


	public static String expandDirectionsString(String dir) {
	
		//First replace slashes with blanks so tokenizing is successful
		dir.replace('/', ' '); 
		dir.replace('\n', ' '); 
		
		String[] tokens = dir.split(" "); 

		//System.out.println("Directions tokens:"); 
		/*for (String tok: tokens) {
			System.out.println(tok); 
		}*/

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

		//System.out.println("Result of expanding abbreviations; " + result); 

		return result; 
	}
	


}
