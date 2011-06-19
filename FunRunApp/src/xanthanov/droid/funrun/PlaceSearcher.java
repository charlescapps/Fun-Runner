package xanthanov.droid.funrun; 

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

public class PlaceSearcher {

	private final static String mapsApiUrl = "https://maps.googleapis.com/maps/api/place/search/json";
	//TODO: Get a license so app can do > 1000 queries per day
	private final static String apiKey = "AIzaSyANveACY4fX2f9MXUHxbE6UbnCkHA0_C6Q";

	private HashMap<String, String> stringToQuery;

	public PlaceSearcher() {
		//Define a map between category strings (res/strings/strings.xml)) --> google map API URI parameters
		stringToQuery = new HashMap<String, String>();	
		stringToQuery.put("Cafe", "types=cafe");   
		stringToQuery.put("Point of Interest", "types=point_of_interest"); 
		stringToQuery.put("Smoothies","types=food|restaurant&name=smoothies"); 
		stringToQuery.put("Park", "types=park"); 
		stringToQuery.put("Bookstore", "types=book_store"); 
		stringToQuery.put("Clothing Store", "types=clothing_store"); 
		stringToQuery.put("Bicycle Store", "types=bicycle_store"); 
		stringToQuery.put("Florist", "types=florist"); 
		stringToQuery.put("Library", "types=library"); 
		stringToQuery.put("Food Cart", "types=food|restaurant&name=food%20cart"); 
		stringToQuery.put("Library", "types=library"); 
		stringToQuery.put("Museum", "types=museum"); 
		stringToQuery.put("Art Gallery", "types=art_gallery"); 
		stringToQuery.put("Pet Store", "types=pet_store"); 
		stringToQuery.put("Bar", "types=bar"); 
	}

	public List<GooglePlace> getNearbyPlaces(String search, GeoPoint currentLocation, int radiusMeters) {
		URL url= null; 
		HttpURLConnection conn= null; 

		try {
			url = new URL(mapsApiUrl + "?" + buildLocationString(currentLocation) 
								+ "&radius=" + radiusMeters 
								+ "&" + stringToQuery.get(search)
								+ "&sensor=true"
								+ "&key=" + apiKey);	

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

	private static String buildLocationString(GeoPoint pt) {
		String locStr = "location="; 
		
		locStr+=(pt.getLatitudeE6()*1E-6); 
		locStr+=",";
		locStr+=(pt.getLongitudeE6()*1E-6); 
	
		return locStr;
	}
	
	private List<GooglePlace> parseJsonResult(BufferedReader in) throws GmapException {
		
		String jsonString = new String(); 
		String aLine = null; 
		List<GooglePlace> foundPlaces = new ArrayList<GooglePlace>(); 
		String status = null; 
		JSONObject jObj = null; 

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
			JSONArray results = jObj.getJSONArray("results");

			if (status.equals("ZERO_RESULTS")) {
				return foundPlaces; //Return empty array if no results 
			}		
			else if (!status.equals("OK")) {
				throw new GmapException("Gmaps API returned error code: " + status); 
			}

			for (int i = 0; i < results.length(); i++) {
				foundPlaces.add(parseGmapResult(results.getJSONObject(i))); 
			} 	

		}
		catch (JSONException e) {
			System.err.println("ERROR Parsing JSON string from Google Maps API"); 
			e.printStackTrace(); 
			return null; 
		}

		return foundPlaces; 
	} 

	private GooglePlace parseGmapResult(JSONObject gmapResult) throws JSONException {
		String name = gmapResult.getString("name"); 
		String icon = gmapResult.getString("icon"); 

		JSONObject coords = gmapResult.getJSONObject("geometry").getJSONObject("location"); 

		String lat = coords.getString("lat");
		String lng = coords.getString("lng");
		
		int latMicroDegrees = (int) (Double.parseDouble(lat)*1E6); 
		int lngMicroDegrees = (int) (Double.parseDouble(lng)*1E6); 
		
		URL iconUrl = null; 

		try { 
			iconUrl = new URL(icon); 
		}
		catch (MalformedURLException e) {
			System.err.println("Google Maps API returned invalid URL string: " + icon); 
			e.printStackTrace(); 
			return null; 
		}

		return new GooglePlace(name, new GeoPoint(latMicroDegrees, lngMicroDegrees), iconUrl);	
	}
}
