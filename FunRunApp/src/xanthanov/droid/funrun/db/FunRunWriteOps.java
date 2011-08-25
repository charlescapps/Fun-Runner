//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun.db; 

import android.database.sqlite.SQLiteOpenHelper; 
import android.database.sqlite.SQLiteDatabase; 
import android.database.sqlite.SQLiteStatement; 

import java.util.Date; 
import java.text.SimpleDateFormat; 

import static DbInfo.*; 

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
	private int runId; 

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
													", " + NE_LAT + ", " + NE_LNG + ", " + SE_LAT + ", " + SE_LNG + 
													", " + GOT_TO_DEST + ", " + PLACE_LAT + ", " + PLACE_LNG + ") " + 
													"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 

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
	public boolean insertNewRun(GoogleDirections run) {
		Date runDate = run.getDate(); 
		String dateStr = dbDate.format(runDate);
		//Create a new SQLite statement for inserting a new run, and bind this specific date to it.  
		SQLiteStatement stmt = db.compileStatement(insertNewRun); 
		stmt.bindString(1, dateStr); 

		int rowId = stmt.executeInsert(); 
		this.runId = rowId; 

		if (rowId < 0) {
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
	public boolean insertLeg(GoogleLeg leg) {
		db.beginTransaction(); 

		SQLiteStatement legStmt = db.compileStatement(insertLegInfo); 

		db.endTransaction(); 
		
	}

}
