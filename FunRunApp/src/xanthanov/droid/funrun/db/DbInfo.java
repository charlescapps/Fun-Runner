//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun.db; 

/**
*
*<h3>Dummy class to store database metadata such as table names and queries.</h3>
*
*@author Charles L. Capps
*@version 0.9b
**/

public class DbInfo {

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

public final static String CREATE_RUN_TBL = 
	"CREATE TABLE " + RUN_TBL + " (INTEGER PRIMARY KEY " + RUN_ID + ", " +
		"TEXT " + RUN_DATE + ");"
	;

}
