package com.kentvu.kanji_decomposition;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class KanjiPartsDbHelper extends SQLiteOpenHelper {
	// If you change the database schema, you must increment the database
	// version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "KanjiParts.db";

	public KanjiPartsDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(KanjiPartsDbContract.SQL_CREATE_KANJIPARTS);
		initDictTable(db);
	}

	private void initDictTable(SQLiteDatabase db) {
		// Read in each line of kradfile
		// open kradfile?
		
		// for each line check if it's comment or not
		
		// if not a comment line then parse it into the table
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(KanjiPartsDbContract.SQL_DELETE_KANJIPARTS);
        onCreate(db);

	}

}
