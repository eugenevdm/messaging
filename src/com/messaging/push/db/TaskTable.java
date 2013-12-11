package com.messaging.push.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TaskTable {

	// Database table
	public static final String TABLE_TASK = "task";
	public static final String COLUMN_ID = "_id";
	// Task ID corresponds with ticket ID in WHMCS
	public static final String COLUMN_TICKET_ID = "ticket_id";
	public static final String COLUMN_DEPARTMENT = "department";
	public static final String COLUMN_CLIENT = "client";
	public static final String COLUMN_ADDRESS = "address";
	public static final String COLUMN_START = "start_actual";
	public static final String COLUMN_STOP = "stop_actual";

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " + TABLE_TASK
			+ "(" 
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_TICKET_ID + " integer, "
			+ COLUMN_DEPARTMENT + " text not null, "
			+ COLUMN_CLIENT + " text not null,"
			+ COLUMN_ADDRESS + " text not null,"
			+ COLUMN_START + " int,"
			+ COLUMN_STOP + " int"
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