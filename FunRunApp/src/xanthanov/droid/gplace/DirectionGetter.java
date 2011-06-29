package xanthanov.droid.gplace; 

import xanthanov.droid.gplace.*;
import xanthanov.droid.funrun.exceptions.GmapException;

import java.util.HashMap; 
import java.util.List; 
import java.util.ArrayList; 

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.URL; 
import java.net.HttpURLConnection; 
import java.net.MalformedURLException;

import com.google.android.maps.GeoPoint; 

import org.json.*; 

public class DirectionGetter {

	private final static String mapsApiUrl = "https://maps.googleapis.com/maps/api/directions/json";
	//TODO: Get a license so app can do > 1000 queries per day
	private final static String apiKey = "AIzaSyANveACY4fX2f9MXUHxbE6UbnCkHA0_C6Q";

	private HashMap<String, String> stringToQuery;

	public DirectionGetter() {
		//Meh?
	}

	public GoogleDirections getDirections(GeoPoint currentLocation, GeoPoint destination) {
		URL url= null; 
		HttpURLConnection conn= null; 

		try {
			url = new URL(mapsApiUrl + "?origin=" + buildLatlngString(currentLocation) 
								+ "&destination=" + buildLatlngString(destination) 
								+ "&mode=walking"
								+ "&avoid=highways"
								+ "&sensor=true");
			//					+ "&key=" + apiKey);	

			conn = (HttpURLConnection) url.openConnection(); 	

			return parseJsonResult(new BufferedReader(new InputStreamReader(conn.getInputStream()))); 
		}
		catch (Exception e) {
			System.err.println("Error creating java.net.URL or opening connection or reading input stream:"); 
			e.printStackTrace(); 
			return null; 
		}
		finally {
			if (conn != null) conn.disconnect(); 
		}

	} 

	private static String buildLatlngString(GeoPoint pt) {
		String locStr = ""; 
		
		locStr+=(pt.getLatitudeE6()*1E-6); 
		locStr+=",";
		locStr+=(pt.getLongitudeE6()*1E-6); 
	
		return locStr;
	}
	
	private GoogleDirections parseJsonResult(BufferedReader in) throws GmapException {
		
		String jsonString = new String(); 
		String aLine = null; 
		GoogleDirections directions = new GoogleDirections(); 
		String status = null; 
		JSONObject jObj = null; 
		//The bounds returned by Google for the route
		double swLat = 0.0; 
		double swLng = 0.0; 
		double neLat = 0.0; 
		double neLng = 0.0; 

		try {//Try block for reading JSON String from HTTP Request. IOException likely means something went awry with your connection
			while ((aLine = in.readLine()) != null) {
				System.out.println(aLine);
				jsonString+=aLine;  
			}
		}
		catch (IOException e) {
			System.err.println("Error reading Google Maps API HTTP output"); 
			e.printStackTrace(); 
			return null; 
		}

		try {//Try block for parsing the returned JSON String...seems unlikely google would return an invalid string if the first try block succeeded
			jObj = (JSONObject) new JSONTokener(jsonString).nextValue();	
			status = jObj.getString("status"); 
			JSONArray routes = jObj.getJSONArray("routes");

			if (status.equals("ZERO_RESULTS")) {
				return directions; //Return empty array if no results 
			}		
			else if (!status.equals("OK")) {
				throw new GmapException("Gmaps API returned error code: " + status); 
			}

			//Get the array of routes--should only have 1 entry
			JSONObject theRoute = routes.getJSONObject(0); 
			JSONArray legs = theRoute.getJSONArray("legs");
			JSONObject currentLeg = null;  
			JSONArray currentStepsArray = null; 
			JSONObject currentStep = null; 
			JSONObject legDistance = null; 
			JSONObject stepDistance = null; 
			String legDistanceStr = null; 
			int legDistanceMeters = 0; 
			String stepDistanceStr = null; 
			int stepDistanceMeters = 0;
			double startLat = 0.0; 
			double startLng = 0.0;  
			double endLat = 0.0; 
			double endLng = 0.0;  

			swLat = theRoute.getJSONObject("bounds").getJSONObject("southwest").getDouble("lat"); 
			swLng = theRoute.getJSONObject("bounds").getJSONObject("southwest").getDouble("lng"); 
			neLat = theRoute.getJSONObject("bounds").getJSONObject("northeast").getDouble("lat"); 
			neLng = theRoute.getJSONObject("bounds").getJSONObject("northeast").getDouble("lng"); 

			System.out.println("Southwest bound: " + swLat + "," + swLng); 	
			System.out.println("Northeast bound: " + neLat + "," + neLng); 	
		

			for (int i = 0; i < legs.length(); i++) {
				currentLeg = legs.getJSONObject(i);  //Get the current leg, the steps it contains, and the aggregate distance info for the leg
				currentStepsArray = currentLeg.getJSONArray("steps"); 
				legDistance = currentLeg.getJSONObject("distance"); 
				legDistanceStr = legDistance.getString("text"); 
				legDistanceMeters = legDistance.getInt("value"); 

				directions.add(new GoogleLeg(legDistanceStr, legDistanceMeters));	//Add the leg to our List<GoogleLeg> of directions				

				for (int j = 0; j < currentStepsArray.length(); j++) {
					//Process each step in the leg
					currentStep = currentStepsArray.getJSONObject(j); 
					stepDistance = currentStep.getJSONObject("distance"); 
					stepDistanceStr = stepDistance.getString("text"); 
					stepDistanceMeters = stepDistance.getInt("value"); 
					startLat = currentStep.getJSONObject("start_location").getDouble("lat"); 
					startLng = currentStep.getJSONObject("start_location").getDouble("lng"); 
					endLat = currentStep.getJSONObject("end_location").getDouble("lat"); 
					endLng = currentStep.getJSONObject("end_location").getDouble("lng"); 
					GeoPoint start = new GeoPoint((int) (startLat*1E6), (int) (startLng*1E6)); 
					GeoPoint end = new GeoPoint((int) (endLat*1E6), (int) (endLng*1E6)); 
					String htmlDirections = currentStep.getString("html_instructions");

					directions.get(directions.size()-1).add(new GoogleStep(new double[] {startLat, startLng}, new double[] {endLat, endLng}, stepDistanceMeters, stepDistanceStr, htmlDirections ));
				}
			} 	

		}
		catch (JSONException e) {
			System.err.println("ERROR Parsing JSON string from Google Maps API"); 
			e.printStackTrace(); 
			return null; 
		}
		directions.get(0).setSwBound(new GeoPoint((int) (swLat*1E6), (int) (swLng*1E6))); 
		directions.get(0).setNeBound(new GeoPoint((int) (neLat*1E6), (int) (neLng*1E6))); 

		return directions; 
	} 



}
