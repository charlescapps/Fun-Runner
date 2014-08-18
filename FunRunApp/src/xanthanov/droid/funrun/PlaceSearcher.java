//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun;

import android.content.res.Resources;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import xanthanov.droid.funrun.exceptions.GmapException;
import xanthanov.droid.gplace.GooglePlace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
*<h3>Class to make HTTP request to Google Places API to get nearby places.</h3>
*
*Notes about this class: 
*<ul>
*<li>Maps resource strings in res/values/strings.xml into GET params for the HTTP request.</li>
*<li>Parses JSON result</li>
*<li>Returns a List of GooglePlace objects </li>
*<li>TODO: pay Google for upgraded Google Places account to raise limit on no. of requests / day.</li>
*<li>Google API Key is hard-coded into the source. Didn't see any reason not to do so.</li>
*<li>TODO: Improve queries, make more categories. Maybe allow for a custom search? </li>
*<li>TODO: Is it possible to do a search &quot;just like typing into search box&quot; at maps.google.com?</li>
*<li>Note: Maps API has a fixed list of categories. Name search appears to be very strict, search str must be substring of the place's name.</li>
*</ul>
*
*@see xanthanov.droid.gplace 
*
*@author Charles L. Capps
*@version 0.9b
**/

public class PlaceSearcher {

	private final static String mapsApiUrl = "https://maps.googleapis.com/maps/api/place/search/json";
	//TODO: Get a license so app can do > 1000 queries per day
	private final static String apiKey = "AIzaSyANveACY4fX2f9MXUHxbE6UbnCkHA0_C6Q";

	private HashMap<CharSequence, CharSequence> stringToQuery;

	public PlaceSearcher(Resources res) {
		//Define a map between category strings (res/strings/strings.xml)) --> google map API URI parameters
		stringToQuery = new HashMap<CharSequence, CharSequence>();	
		stringToQuery.put(res.getText(R.string.cafe), "types=cafe");   
		stringToQuery.put(res.getText(R.string.school), "types=school|university");   
		stringToQuery.put(res.getText(R.string.zoo), "types=zoo&name=zoo");   
		stringToQuery.put(res.getText(R.string.point_of_interest), "types=point_of_interest"); 
		stringToQuery.put(res.getText(R.string.smoothies),"name=smoothie"); 
		stringToQuery.put(res.getText(R.string.juice_bar),"name=juice"); 
		stringToQuery.put(res.getText(R.string.park), "types=park&name=park"); 
		stringToQuery.put(res.getText(R.string.book_store), "types=book_store"); 
		stringToQuery.put(res.getText(R.string.clothing_store), "types=clothing_store"); 
		stringToQuery.put(res.getText(R.string.bicycle_store), "types=bicycle_store"); 
		stringToQuery.put(res.getText(R.string.florist), "types=florist"); 
		stringToQuery.put(res.getText(R.string.library), "types=library"); 
		stringToQuery.put(res.getText(R.string.food_cart), "types=food|restaurant&name=food+cart"); 
		stringToQuery.put(res.getText(R.string.museum), "types=museum"); 
		stringToQuery.put(res.getText(R.string.art_gallery), "types=art_gallery"); 
		stringToQuery.put(res.getText(R.string.pet_store), "types=pet_store"); 
		stringToQuery.put(res.getText(R.string.bar), "types=bar"); 

		stringToQuery.put(res.getText(R.string.aquarium), "types=aquarium");   
		stringToQuery.put(res.getText(R.string.amusement_park), "types=amusement_park"); 
		stringToQuery.put(res.getText(R.string.movie_theater),"types=movie_theater"); 
		stringToQuery.put(res.getText(R.string.bowling_alley), "types=bowling_alley"); 
		stringToQuery.put(res.getText(R.string.shopping_mall), "types=shopping_mall"); 
		stringToQuery.put(res.getText(R.string.natural_feature), "types=natural_feature"); 
		stringToQuery.put(res.getText(R.string.pizza), "types=food|bar|restaurant|meal_takeaway|meal_delivery&name=pizza"); 
		stringToQuery.put(res.getText(R.string.brew_pub), "types=bar|food|restaurant&name=brew+pub"); 
	}

	public List<GooglePlace> getNearbyPlaces(String search, LatLng currentLocation, int radiusMeters) throws UnknownHostException, GmapException, JSONException, MalformedURLException, java.io.UnsupportedEncodingException {
		URL url= null; 
		HttpURLConnection conn= null; 
		String urlString = null; 
		CharSequence queryStr = stringToQuery.get(search); 
		if (queryStr == null) {//If we didn't get a fixed category, must have been passed a custom search string, so search by name. 
			queryStr = "name=" + java.net.URLEncoder.encode(search.toString(), "UTF-8"); 
			System.out.println("Custom query string: " + queryStr); 
		}

		try {
			urlString = mapsApiUrl + "?" + buildLocationString(currentLocation) 
								+ "&radius=" + radiusMeters 
								+ "&" + queryStr
								+ "&sensor=true"
								+ "&key=" + apiKey;

			url = new URL(urlString); 	

			conn = (HttpURLConnection) url.openConnection(); 	

			return parseJsonResult(new BufferedReader(new InputStreamReader(conn.getInputStream()))); 
		}
		catch (MalformedURLException e) {
			throw new MalformedURLException("Invalid URL: " + urlString + "\nPlease check your internet connection and try again."); 
		}
		catch (UnknownHostException e) {
			throw new UnknownHostException("Unable to connect to Google Maps.\nPlease check your internet connection and try again."); 
		}
		catch(JSONException e) {
			throw new JSONException("Failure parsing place info retreived from Google Maps.\nPlease try again later.");
		}
		catch(IOException e) {
			throw new JSONException("Error downloading info from Google Maps.\nPlease check your internet connection and try again.");
		}
		catch (GmapException e) {
			throw new GmapException(e.getMessage() + "\nPlease check your internet connection and try again.");  
		}
		finally {
			if (conn != null) conn.disconnect(); 
		}

	} 

	private static String buildLocationString(LatLng pt) {
		String locStr = "location="; 
		
		locStr+=(pt.latitude);
		locStr+=",";
		locStr+=(pt.longitude);
	
		return locStr;
	}
	
	private List<GooglePlace> parseJsonResult(BufferedReader in) throws GmapException, IOException, JSONException {
		
		String jsonString = new String(); 
		String aLine = null; 
		List<GooglePlace> foundPlaces = new ArrayList<GooglePlace>(); 
		String status = null; 
		JSONObject jObj = null; 

		while ((aLine = in.readLine()) != null) {
		//	System.out.println(aLine);
			jsonString+=aLine;  
		}

		jObj = (JSONObject) new JSONTokener(jsonString).nextValue();	
		status = jObj.getString("status"); 
		JSONArray results = jObj.getJSONArray("results");

		if (status.equals("ZERO_RESULTS")) {
			return foundPlaces; //Return empty array if no results 
		}		
		else if (!status.equals("OK")) {
			throw new GmapException("Google Maps returned an error code:\n" + status); 
		}

		for (int i = 0; i < results.length(); i++) {
			foundPlaces.add(parseGmapResult(results.getJSONObject(i))); 
		} 	


		return foundPlaces; 
	} 

	private GooglePlace parseGmapResult(JSONObject gmapResult) throws JSONException {
		String name = gmapResult.getString("name"); 
		String icon = gmapResult.getString("icon"); 

		JSONObject coords = gmapResult.getJSONObject("geometry").getJSONObject("location"); 

		String lat = coords.getString("lat");
		String lng = coords.getString("lng");
		
		double[] latLng = new double[] {Double.parseDouble(lat), Double.parseDouble(lng)};
		
		URL iconUrl = null; 

		try { 
			iconUrl = new URL(icon); 
		}
		catch (MalformedURLException e) {
			System.err.println("Google Maps API returned invalid URL string: " + icon); 
			e.printStackTrace(); 
			return null; 
		}

		return new GooglePlace(name, latLng, iconUrl);	
	}

	public static void printListOfPlaces(List<GooglePlace> places) {
		if (places == null) {
			return;
		}
		for (GooglePlace gp: places) {
			System.out.println(gp); 
		}
	}

}
