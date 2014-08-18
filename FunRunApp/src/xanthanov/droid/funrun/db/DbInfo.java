//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun.db;

import java.text.SimpleDateFormat;

/**
*
*<h3>Dummy class to store database metadata such as table names and queries.</h3>
*
*@author Charles L. Capps
*@version 0.9b
**/

public class DbInfo {

	public static final int DB_VERSION = 1; 

	public final static SimpleDateFormat dbDate= new SimpleDateFormat("MM-dd-yyyy_HH:mm:ss"); 

	public final static String DB_NAME = "funrun_db";

	//Table names
	public final static String RUN_PATH_TBL = "run_path"; 
	public final static String RUN_TBL = "run_info"; 
	public final static String LEG_TBL = "leg_info"; 

	//Shared column names. Primary/foreign keys
	public final static String RUN_ID = "run_id"; 
	public final static String LEG_ID = "leg_id"; 

	//Exclusively Run path tbl columns
	public final static String PATH_ID = "path_id"; 
	public final static String PATH_LAT = "path_lat"; 
	public final static String PATH_LNG = "path_lng"; 

	//Exclusively run info columns
	public final static String RUN_DATE = "run_date"; 

	//Exclusively leg info columns
	public final static String START_TIME = "start_time"; 
	public final static String END_TIME = "end_time"; 
	public final static String POLYLINE = "polyline"; 
	public final static String NE_LAT = "ne_bound_lat"; 
	public final static String NE_LNG = "ne_bound_lng"; 
	public final static String SW_LAT = "sw_bound_lat"; 
	public final static String SW_LNG = "sw_bound_lng"; 
	public final static String GOT_TO_DEST = "got_to_dest"; 
	public final static String PLACE_NAME = "place_name"; 
	public final static String PLACE_LAT = "place_lat"; 
	public final static String PLACE_LNG = "place_lng"; 
	public final static String DISTANCE_RAN = "distance_ran"; 
	public final static String LEG_POINTS = "leg_points"; 

	public final static String CREATE_RUN_TBL = 
		"CREATE TABLE " + RUN_TBL + " (" + 
			RUN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			RUN_DATE + " TEXT);";

	public final static String CREATE_RUN_PATH_TBL = 
		"CREATE TABLE " + RUN_PATH_TBL + " (" + 
			PATH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
			RUN_ID + " INTEGER, " +
			LEG_ID + " INTEGER, " + 
			PATH_LAT + " REAL, " + 
			PATH_LNG + " REAL);"; 

	public final static String CREATE_LEG_TBL = 
		"CREATE TABLE " + LEG_TBL + " (" + 
			LEG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
			RUN_ID + " INTEGER, " + 
			START_TIME + " INTEGER, " + 
			END_TIME + " INTEGER, " + 
			POLYLINE + " TEXT, " + 
			NE_LAT + " REAL, " + 
			NE_LNG + " REAL, " + 
			SW_LAT + " REAL, " + 
			SW_LNG + " REAL, " +
			GOT_TO_DEST + " INTEGER, " +
			PLACE_NAME + " TEXT, " +
			PLACE_LAT + " REAL, " + 
			PLACE_LNG + " REAL, " + 
			DISTANCE_RAN + " REAL, " + 
			LEG_POINTS + ");"; 
}
