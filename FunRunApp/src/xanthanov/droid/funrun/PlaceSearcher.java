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
import java.net.UnknownHostException;

import android.content.res.Resources; 

import com.google.android.maps.GeoPoint; 
import org.json.*; 

public class PlaceSearcher {

	private final static String mapsApiUrl = "https://maps.googleapis.com/maps/api/place/search/json";
	//TODO: Get a license so app can do > 1000 queries per day
	private final static String apiKey = "AIzaSyANveACY4fX2f9MXUHxbE6UbnCkHA0_C6Q";

	private HashMap<CharSequence, CharSequence> stringToQuery;

	public PlaceSearcher(Resources res) {
		//Define a map between category strings (res/strings/strings.xml)) --> google map API URI parameters
		stringToQuery = new HashMap<CharSequence, CharSequence>();	
		stringToQuery.put(res.getText(R.string.cafe), "types=cafe");   
		stringToQuery.put(res.getText(R.string.point_of_interest), "types=point_of_interest"); 
		stringToQuery.put(res.getText(R.string.smoothies),"types=food|restaurant|bar|meal_takeaway|meal_delivery|shopping_mall&name=smoothie"); 
		stringToQuery.put(res.getText(R.string.juice_bar),"types=food|restaurant|bar|meal_takeaway|meal_delivery|shopping_mall&name=juice"); 
		stringToQuery.put(res.getText(R.string.park), "types=park"); 
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
		stringToQuery.put(res.getText(R.string.brew_pub), "types=bar|food|restaurant&name=brew%20pub"); 
	}

	public List<GooglePlace> getNearbyPlaces(String search, GeoPoint currentLocation, int radiusMeters) throws UnknownHostException, GmapException, JSONException, MalformedURLException {
		URL url= null; 
		HttpURLConnection conn= null; 
		String urlString = null; 

		try {
			urlString = mapsApiUrl + "?" + buildLocationString(currentLocation) 
								+ "&radius=" + radiusMeters 
								+ "&" + stringToQuery.get(search)
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

	private static String buildLocationString(GeoPoint pt) {
		String locStr = "location="; 
		
		locStr+=(pt.getLatitudeE6()*1E-6); 
		locStr+=",";
		locStr+=(pt.getLongitudeE6()*1E-6); 
	
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

	public static void printListOfPlaces(List<GooglePlace> places) {
		if (places == null) {
			return;
		}
		for (GooglePlace gp: places) {
			System.out.println(gp); 
		}
	}

}
