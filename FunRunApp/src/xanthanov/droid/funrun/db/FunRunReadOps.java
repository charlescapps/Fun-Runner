
package xanthanov.droid.funrun.db; 

import android.content.Context; 
import android.database.sqlite.SQLiteDatabase; 
import android.database.sqlite.SQLiteStatement; 
import android.database.Cursor;
import java.util.List;
import java.util.ArrayList; 
import java.util.Date; 
import java.sql.SQLException; 
import java.text.ParseException; 

import xanthanov.droid.gplace.LatLng;
import xanthanov.droid.gplace.GoogleLeg;  

public class FunRunReadOps {

	private FunRunDbHelper sqlHelper; 
	private SQLiteDatabase db; 

	public FunRunReadOps(Context c) {
		sqlHelper = new FunRunDbHelper(c); 
		db = sqlHelper.getReadableDatabase(); 
	}

	public long getTotalPoints() throws SQLException {
		SQLiteStatement pointsResult = db.compileStatement("SELECT SUM(" + DbInfo.LEG_POINTS + ") FROM " + DbInfo.LEG_TBL + ";"); 
		long points = pointsResult.simpleQueryForLong(); 
		return points; 
	}

	public List<OldRun> readOldRuns() throws SQLException {

		Cursor legResult = db.rawQuery("SELECT * FROM " + DbInfo.LEG_TBL + " ORDER BY " + DbInfo.RUN_ID + ", " + DbInfo.LEG_ID + ";", null); 
		List<OldLeg> legs; 
		List<OldRun> runs = new ArrayList<OldRun>(); 

		//Define all the col. indices. *Sigh* why doesn't android.database.Cursor have a simple operation to get a value based on column name?
		final int legIdCol = legResult.getColumnIndex(DbInfo.LEG_ID); 
		final int runIdCol = legResult.getColumnIndex(DbInfo.RUN_ID); 
		final int startTimeCol = legResult.getColumnIndex(DbInfo.START_TIME); 
		final int endTimeCol = legResult.getColumnIndex(DbInfo.END_TIME); 
		final int polyCol = legResult.getColumnIndex(DbInfo.POLYLINE); 
		final int neLatCol = legResult.getColumnIndex(DbInfo.NE_LAT); 
		final int neLngCol = legResult.getColumnIndex(DbInfo.NE_LNG); 
		final int swLatCol = legResult.getColumnIndex(DbInfo.SW_LAT); 
		final int swLngCol = legResult.getColumnIndex(DbInfo.SW_LNG); 
		final int gotToDestCol = legResult.getColumnIndex(DbInfo.GOT_TO_DEST); 
		final int placeNameCol = legResult.getColumnIndex(DbInfo.PLACE_NAME); 
		final int placeLatCol = legResult.getColumnIndex(DbInfo.PLACE_LAT); 
		final int placeLngCol = legResult.getColumnIndex(DbInfo.PLACE_LNG); 
		final int actualDistCol = legResult.getColumnIndex(DbInfo.DISTANCE_RAN); 
		final int pointsCol = legResult.getColumnIndex(DbInfo.LEG_POINTS); 

		//Create a new OldLeg object for each leg_id in the database. 
		//Also, query the RUN_PATH_TBL to get the path the user ran. 
		long runId = -1; 
		long currentRunId = -1; 
		long legId = -1; 

		legResult.moveToNext(); 

		while (!legResult.isAfterLast()) {
			runId = currentRunId = legResult.getLong(runIdCol); 
			legs = new ArrayList<OldLeg>(); //Start a new list of legs for this run

			//Populate a run until we hit a new run_id
			while (!legResult.isAfterLast() && runId == currentRunId) {
				legId = legResult.getLong(legIdCol);
				long startTime = legResult.getLong(startTimeCol); 
				long endTime = legResult.getLong(endTimeCol); 
				String polyline = legResult.getString(polyCol); 
				double neLat = legResult.getDouble(neLatCol); 
				double neLng = legResult.getDouble(neLngCol); 
				double swLat = legResult.getDouble(swLatCol); 
				double swLng = legResult.getDouble(swLngCol); 
				boolean gotToDest = (legResult.getInt(gotToDestCol) != 0); 
				String placeName = legResult.getString(placeNameCol); 
				double placeLat = legResult.getDouble(placeLatCol); 
				double placeLng = legResult.getDouble(placeLngCol); 
				double actualDist = legResult.getDouble(actualDistCol); 
				int legPoints = legResult.getInt(pointsCol); 

				//select path points for this leg
				Cursor pathPointsResult = db.rawQuery("SELECT * FROM " + DbInfo.RUN_PATH_TBL + " WHERE " + DbInfo.LEG_ID + " = " + legId + " ORDER BY " + DbInfo.PATH_ID + ";", null); 

				List<LatLng> pathPoints = new ArrayList<LatLng>(); 

				pathPointsResult.moveToNext(); 
				while (!pathPointsResult.isAfterLast()) {

					double lat = pathPointsResult.getDouble(pathPointsResult.getColumnIndex(DbInfo.PATH_LAT)); 
					double lng = pathPointsResult.getDouble(pathPointsResult.getColumnIndex(DbInfo.PATH_LNG)); 
					
					pathPoints.add(new LatLng(lat, lng)); //Add point to path

					pathPointsResult.moveToNext(); 
				}

				pathPointsResult.close(); 

				legs.add(new OldLeg(pathPoints, GoogleLeg.getPathPoints(polyline), startTime, endTime, actualDist, 
									new LatLng(neLat, neLng), new LatLng(swLat, swLng), legPoints, placeName, new LatLng(placeLat, placeLng), gotToDest));  	

				legResult.moveToNext(); 
				if (!legResult.isAfterLast()) {
					currentRunId = legResult.getLong(runIdCol); 
				}
			} 

			if (legs.size() > 0 ) {
				SQLiteStatement getDateStmt = db.compileStatement("SELECT " + DbInfo.RUN_DATE + " FROM " + DbInfo.RUN_TBL + " WHERE " + DbInfo.RUN_ID + " = " + runId); 
				String dateStr = getDateStmt.simpleQueryForString();
				Date date = null;   	
				try {
					date = DbInfo.dbDate.parse(dateStr); 
				}
				catch (ParseException e) {
					System.err.println("Error parsing date string: " + dateStr); 
					continue; 
				}

				runs.add(new OldRun(date, legs)); 
			}

		}

		legResult.close(); 

		return runs; 
 
	}

}
