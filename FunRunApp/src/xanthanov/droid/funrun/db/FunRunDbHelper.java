//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.furun.db; 

import android.database.sqlite.SQLiteOpenHelper; 
import android.database.sqlite.SQLiteDatabase; 
import android.content.Context; 

public class FunRunDbHelper extends SQLiteOpenHelper {

	public static final int DB_VERSION = 1; 

	FunRunDbHelper(Context c) {
		super(c, DbInfo.DB_NAME, null, DB_VERSION); 
		
	}
	

	@Override 
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DbInfo.CREATE_RUN_TBL); 
		db.execSQL(DbInfo.CREATE_RUN_PATH_TBL); 
		db.execSQL(DbInfo.CREATE_LEG_TBL); 

	}
	
	@Override
	public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
		return; 
	}

}
