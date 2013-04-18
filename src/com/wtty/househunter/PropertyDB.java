package com.wtty.househunter;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PropertyDB {
	public static final String KEY_ROWID = "_id";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_NOTES = "notes";
	public static final String KEY_LAT = "lat";
	public static final String KEY_LONG = "long";
	public static final String KEY_SQFT = "sqft";
	public static final String KEY_BEDROOMS = "bedrooms";
	public static final String KEY_BATHROOMS = "bathrooms";
	public static final String KEY_POOL = "pool";
	public static final String KEY_HOA = "hoa";
	public static final String KEY_STATE = "state";
	
	private static final String LOG_TAG = "PropertyDB";
	public static final String SQLITE_TABLE = "Properties";
	
	private static final String DATABASE_CREATE =
			  "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
			   KEY_ROWID + " integer PRIMARY KEY autoincrement," +
			   KEY_ADDRESS + "," +
			   KEY_NOTES + "," +
			   KEY_LAT + "," +
			   KEY_LONG + "," +
			   KEY_SQFT + "," +
			   KEY_BEDROOMS + "," +
			   KEY_BATHROOMS + "," +
			   KEY_POOL + "," +
			   KEY_HOA + "," +
			   KEY_STATE + ");";
	 
	public static void onCreate(SQLiteDatabase db) {
	  Log.w(LOG_TAG, DATABASE_CREATE);
	  db.execSQL(DATABASE_CREATE);
	}
	
	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
		onCreate(db);
	}
}
