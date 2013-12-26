package com.snowball.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TaskTable {

	// Database table
	public static final String TABLE_TASK = "task";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_USERID = "userid";
	public static final String COLUMN_CALENDAR_ID = "calendar_id";
	public static final String COLUMN_TICKET_ID = "ticket_id";
	public static final String COLUMN_DEPARTMENT = "department";
	public static final String COLUMN_COMPANYNAME = "companyname";
	public static final String COLUMN_PHONENUMBER = "phonenumber";
	public static final String COLUMN_CLIENT_NAME = "client_name";	
	public static final String COLUMN_ADDRESS1 = "address1";
	public static final String COLUMN_ADDRESS2 = "address2";
	public static final String COLUMN_CITY = "city";
	public static final String COLUMN_START = "start";
	public static final String COLUMN_END = "stop";
	public static final String COLUMN_START_ACTUAL = "start_actual";
	public static final String COLUMN_END_ACTUAL = "stop_actual";
	// Status can be outstanding / started / paused / completed / cancelled / in progress
	public static final String COLUMN_STATUS = "status";

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " + TABLE_TASK
			+ "(" 
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_USERID + " integer not null, "
			+ COLUMN_CALENDAR_ID + " integer not null, "
			+ COLUMN_TICKET_ID + " integer not null, "
			+ COLUMN_DEPARTMENT + " text not null, "
			+ COLUMN_COMPANYNAME + " text, "
			+ COLUMN_PHONENUMBER + " text not null, "
			+ COLUMN_CLIENT_NAME + " text not null,"
			+ COLUMN_ADDRESS1 + " text not null,"
			+ COLUMN_ADDRESS2 + " text not null,"
			+ COLUMN_CITY + " text not null,"
			+ COLUMN_START + " int,"
			+ COLUMN_END + " int,"
			+ COLUMN_START_ACTUAL + " int,"
			+ COLUMN_END_ACTUAL + " int,"
			+ COLUMN_STATUS + " text default 'outstanding'"
			+ ");";

	public static void onCreate(SQLiteDatabase database) {
		Log.v("database", "onCreate execSQL start");
		database.execSQL(DATABASE_CREATE);
		Log.v("database", "onCreate execSQL end");
	}
	
	public static void delete(SQLiteDatabase database) {
		// TODO For now we drop the database every time the database is upgraded
		//database.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);		
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		//if (oldVersion < 10) {
			//onCreate(database);
		//}
		//
		Log.w("database", "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
		onCreate(database);
	}
}