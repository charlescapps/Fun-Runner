//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

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

/**
* <h3>Class to get Google Walking Directions via HTTP request.</h3>
* <b>Some things to note:</b>
* <ul>
* <li>AFAIK from Google's documentation, unlimited directions requests are allowed per day. Doesn't even pass in the API key in GET request.</li>
* <li>Avoid highways = true</li>
* <li>Alternatives = false. Just gets one possible route to simplify parsing. </li>
* <li>Parses JSON, and returns result as a GoogleLeg. Each leg of a GoogleDirections object corresponds to the user running to one place. </li>
* <li>In retrospect, it's a bit misleading that I call the container &quot;GoogleDirections&quot;. I did so because initially I thought Google 
* would return multiple legs, but it turns out this is only the case if you set waypoints. </li>
* 
*</ul>
*
*
*@author Charles L. Capps
*@version 0.9b
*@see xanthanov.droid.funrun.PlaceSearcher
**/

public class DirectionGetter {

	private final static String mapsApiUrl = "https://maps.googleapis.com/maps/api/directions/json";
	//TODO: Get a license so app can do > 1000 queries per day
	private final static String apiKey = "AIzaSyANveACY4fX2f9MXUHxbE6UbnCkHA0_C6Q";

	private HashMap<String, String> stringToQuery;

	public DirectionGetter() {
		//Meh?
	}

	public GoogleLeg getDirections(GeoPoint currentLocation, GeoPoint destination) {
		URL url= null; 
		HttpURLConnection conn= null; 

		try {
			url = new URL(mapsApiUrl + "?origin=" + buildLatlngString(currentLocation) 
								+ "&destination=" + buildLatlngString(destination) 
								+ "&mode=walking"
								+ "&avoid=highways"
								+ "&sensor=true"
								+ "&alternatives=false");
			//					+ "&key=" + apiKey);	//Key only for places search afaik

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
	
	private GoogleLeg parseJsonResult(BufferedReader in) throws GmapException {
		
		String jsonString = new String(); 
		String aLine = null; 
		GoogleLeg directions = null; 
		String status = null; 
		JSONObject jObj = null; 
		//The bounds returned by Google for the route
		double swLat = 0.0; 
		double swLng = 0.0; 
		double neLat = 0.0; 
		double neLng = 0.0; 

		try {//Try block for reading JSON String from HTTP Request. IOException likely means something went awry with your connection
			while ((aLine = in.readLine()) != null) {
			//	System.out.println(aLine);
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
			JSONArray warningsArray = null;  
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
			String copyright = "";
			String warnings = ""; 

			String overviewPolyline = theRoute.getJSONObject("overview_polyline").getString("points"); 

			swLat = theRoute.getJSONObject("bounds").getJSONObject("southwest").getDouble("lat"); 
			swLng = theRoute.getJSONObject("bounds").getJSONObject("southwest").getDouble("lng"); 
			neLat = theRoute.getJSONObject("bounds").getJSONObject("northeast").getDouble("lat"); 
			neLng = theRoute.getJSONObject("bounds").getJSONObject("northeast").getDouble("lng"); 

			//System.out.println("Southwest bound: " + swLat + "," + swLng); 	
			//System.out.println("Northeast bound: " + neLat + "," + neLng); 	
		
			

			int i = 0; //Should only have 1 leg, no waypoints specified
			//For-loop removed. Google Directions API returns 1 leg if no waypoints are specified. Also makes for sense for the structure of my app to return a GoogleLeg
				currentLeg = legs.getJSONObject(i);  //Get the current leg, the steps it contains, and the aggregate distance info for the leg
				currentStepsArray = currentLeg.getJSONArray("steps"); 
				legDistance = currentLeg.getJSONObject("distance"); 
				legDistanceStr = legDistance.getString("text"); 
				legDistanceMeters = legDistance.getInt("value"); 
				copyright = theRoute.getString("copyrights"); 
				warningsArray = theRoute.getJSONArray("warnings"); 
		
				for (int k = 0; k < warningsArray.length(); k++) {
					warnings += warningsArray.getString(k) + "<br/>"; 
				}
				

				directions = new GoogleLeg(legDistanceStr, legDistanceMeters);	//Add the leg to our List<GoogleLeg> of directions				
				directions.setOverviewPolyline(overviewPolyline); //Should only have 1 entry--add the polyline info to the google leg
				directions.setCopyright(copyright); 
				directions.setWarnings(warnings); 

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

					directions.add(new GoogleStep(new double[] {startLat, startLng}, new double[] {endLat, endLng}, stepDistanceMeters, stepDistanceStr, htmlDirections ));
				}
			 	

		}
		catch (JSONException e) {
			System.err.println("ERROR Parsing JSON string from Google Maps API"); 
			e.printStackTrace(); 
			return null; 
		}
		directions.setSwBound(new double[]{swLat, swLng}); 
		directions.setNeBound(new double[]{neLat, neLng}); 

		return directions; 
	} 



}
