//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun.db; 

import xanthanov.droid.gplace.GooglePlace; 
import xanthanov.droid.gplace.GoogleDirections; 
import xanthanov.droid.gplace.GoogleLeg; 
import xanthanov.droid.gplace.LatLng; 

import android.database.sqlite.SQLiteOpenHelper; 
import android.database.sqlite.SQLiteDatabase; 
import android.database.sqlite.SQLiteStatement; 
import android.content.Context; 

import java.util.Date; 
import java.util.List; 
import java.text.SimpleDateFormat; 
import java.sql.SQLException; 

import static xanthanov.droid.funrun.db.DbInfo.*; 

/**
*<h3>Performs SQLite queries to insert run data into the DB</h3>
* 
*
*@author Charles L. Capps
*@version 0.9b
**/

public class FunRunWriteOps {

	private SQLiteOpenHelper sqlHelper; 
	private SQLiteDatabase db; 
	private long runId; 

	//Query String to insert a new run. Just inserts the date and auto-generates the new row id. 
	private final static String insertNewRun = "INSERT INTO " + RUN_TBL + 
													" (" + RUN_DATE + ") " + 
													"VALUES (?);";

	//Query to insert a point on a path
	private final static String insertPathPoint = "INSERT INTO " + RUN_PATH_TBL + 
													" (" + RUN_ID + ", " + LEG_ID + ", " + PATH_LAT + ", " + PATH_LNG  + ") " + 
													"VALUES (?, ?, ?, ?);";

	//Query to insert leg data into table. See DbInfo for more info. Includes start, end times, bounds of route for centering on route, etc. 
	private final static String insertLegInfo = "INSERT INTO " + LEG_TBL + 
													" (" + RUN_ID + ", " + START_TIME + ", " + END_TIME + ", " + POLYLINE + 
													", " + NE_LAT + ", " + NE_LNG + ", " + SW_LAT + ", " + SW_LNG + 
													", " + GOT_TO_DEST + ", " + PLACE_LAT + ", " + PLACE_LNG + ", " + LEG_POINTS + ") " + 
													"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 


	public FunRunWriteOps(Context c) {

		sqlHelper = new FunRunDbHelper(c); 
		db = sqlHelper.getWritableDatabase(); 

	}

	/**
	* Method to insert a new run. Just creates a new row in run_tbl with the date. 
	* Also stores the run_id of the new row in the internal state of this class. 
	* Accordingly, the lifetime of this class is for one run, wouldn't make sense otherwise. <br /> 
	* <b>Really, truly want to avoid anything outside of DB classes dealing with row ID's !!! 
	* It would be horrendous to store row id's somewhere in my activity class. </b>  
	*
	**/
	public boolean insertNewRun(GoogleDirections run) throws java.sql.SQLException {
		Date runDate = run.getDate(); 
		String dateStr = dbDate.format(runDate);
		//Create a new SQLite statement for inserting a new run, and bind this specific date to it.  
		SQLiteStatement stmt = db.compileStatement(insertNewRun); 
		stmt.bindString(1, dateStr); 

		this.runId = stmt.executeInsert(); 

		if (runId < 0) {
			System.err.println("Pwn. Failed to insert new run into database in FunRunWriteOps.insertNewRun method\n"); 
			return false; 
		}

		return true; 
		
	}

	/**
	* Method to insert a leg. 
	* If this is too slow, will instead insert data as it's obtained. 
	* The latter solution would be a bit obnoxious, since we'd have to clean up 
	* if the runner ran too little. 
	* Using a transaction, since according to sqlite.org, this is far faster. 
	*
	**/
	public void insertLeg(GoogleLeg leg) throws SQLException {

		//Get appropraite data from GoogleLeg object in memory --> SQLite insert statement
		SQLiteStatement legStmt = db.compileStatement(insertLegInfo); 

		legStmt.bindLong(1, this.runId); 
		legStmt.bindLong(2, leg.getStartTime()); 		
		legStmt.bindLong(3, leg.getEndTime()); 		
		legStmt.bindString(4, leg.getOverviewPolyline()); 

		double[] swBound = leg.getSwBound(); 
		double[] neBound = leg.getNeBound(); 

		legStmt.bindDouble(5, neBound[0]); 
		legStmt.bindDouble(6, neBound[1]); 
		legStmt.bindDouble(7, swBound[0]); 
		legStmt.bindDouble(8, swBound[1]); 

		legStmt.bindLong(9, leg.gotToDestination() ? 1 : 0); 

		GooglePlace dest = leg.getLegDestination(); 
		double[] placeCoords = dest.getLatLng(); 

		legStmt.bindDouble(10, placeCoords[0]); 
		legStmt.bindDouble(11, placeCoords[1]); 

		legStmt.bindLong(12, leg.getLegPoints()); 

		//Insert the info for this leg, and get the new leg ID
		long legRowId = legStmt.executeInsert(); 

		//Get compiled statement for inserting a point of the user's path into DB
		SQLiteStatement insertPathPt = db.compileStatement(insertPathPoint); 
		insertPathPt.bindLong(1, this.runId); 
		insertPathPt.bindLong(2, legRowId); 

		//Start a transaction to insert the many points of the user's run path as pairs of latitude / longitude doubles
		db.beginTransaction(); 

		List<LatLng> actualPath = leg.getActualPath(); 
		
		LatLng pathCoords; 

		for (int i = 0; i < actualPath.size(); i++) {
			pathCoords = actualPath.get(i); 			
			insertPathPt.bindDouble(3, pathCoords.lat); 
			insertPathPt.bindDouble(4, pathCoords.lng); 
			insertPathPt.executeInsert(); 
		}

		db.endTransaction(); 
		
	}

	public void deleteRun() throws SQLException {

		db.execSQL("DELETE FROM " + LEG_TBL + " WHERE " + RUN_ID + " = " + runId + ";"); 
		db.execSQL("DELETE FROM " + RUN_PATH_TBL + " WHERE " + RUN_ID + " = " + runId + ";"); 
		db.execSQL("DELETE FROM " + RUN_TBL + " WHERE " + RUN_ID + " = " + runId + ";"); 

	}


}
