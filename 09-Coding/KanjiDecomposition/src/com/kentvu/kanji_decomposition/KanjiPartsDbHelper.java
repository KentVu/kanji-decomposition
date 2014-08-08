package com.kentvu.kanji_decomposition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import com.kentvu.kanji_decomposition.KanjiPartsDbHelper.KanjiPartsDbContract.KanjiParts;

public class KanjiPartsDbHelper extends SQLiteOpenHelper {
	public final class KanjiPartsDbContract {
		// To prevent someone from accidentally instantiating the contract
		// class,give it an empty constructor.

		public KanjiPartsDbContract() {
		}

		/* Inner class that defines the table contents */
		public abstract class KanjiParts implements BaseColumns {
			// public static abstract class KanjiParts {
			public static final String TABLE_NAME = "kanjiparts";
			public static final String COLUMN_NAME_UNICODE_VALUE = "unicode_value";
			public static final int COLUMN_ID_UNICODE_VALUE = 0;
			public static final String COLUMN_NAME_RADICALS = "radicals";
			public static final int COLUMN_ID_RADICALS = 1;
			public static final String COLUMN_NAME_PARTOF = "part_of";
			public static final int COLUMN_ID_PARTOF = 2;
			public static final String COLUMN_NAME_JIS212_CODE = "jis212_code";
			public static final int COLUMN_ID_JIS212_CODE = 3;
		}

		private static final String TEXT_TYPE = " TEXT";
		private static final String COMMA_SEP = ",";
		public static final String SQL_CREATE_KANJIPARTS = "CREATE TABLE "
				+ KanjiParts.TABLE_NAME + " ("
				+ KanjiParts.COLUMN_NAME_UNICODE_VALUE + " INTEGER PRIMARY KEY"
				+ COMMA_SEP + KanjiParts.COLUMN_NAME_RADICALS + TEXT_TYPE
				+ COMMA_SEP + KanjiParts.COLUMN_NAME_PARTOF + TEXT_TYPE
				+ COMMA_SEP + KanjiParts.COLUMN_NAME_JIS212_CODE + TEXT_TYPE
				// ... Any other options for the CREATE command
				+ " )";

		// public static final String SQL_

		public static final String SQL_DELETE_KANJIPARTS = "DROP TABLE IF EXISTS "
				+ KanjiParts.TABLE_NAME;

	}

	// If you change the database schema, you must increment the database
	// version.
	public static final int DATABASE_VERSION = 3;
	public static final String DATABASE_NAME = "KanjiParts.db";
	public boolean NeedUpdate = false;

	private Context context;

	public KanjiPartsDbHelper(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		context = ctx;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(KanjiPartsDbContract.SQL_CREATE_KANJIPARTS);
		// initDictTable(db);
		NeedUpdate = true;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade
		// policy is
		// to simply to discard the data and start over
		db.execSQL(KanjiPartsDbContract.SQL_DELETE_KANJIPARTS);
		onCreate(db);
	}

	public static String queryKanji(SQLiteDatabase db, int kanjival, int which_info) {
		// *Retrieve data from database
		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		String[] projection = { KanjiParts.COLUMN_NAME_UNICODE_VALUE,
				KanjiParts.COLUMN_NAME_RADICALS, KanjiParts.COLUMN_NAME_PARTOF };
		// long execution time when creating database (calling on create of
		// mDbHelper) (use some kind of parallelizing to overcome/deal with
		// this?)
		Cursor c = db.query(KanjiParts.TABLE_NAME, // The table to query
				projection, // The columns to return
				// The columns for the WHERE clause
				KanjiParts.COLUMN_NAME_UNICODE_VALUE + " = ?",
				// The values for the WHERE clause
				new String[] { Integer.toString(kanjival) },
				// don't group the rows
				null, null, // don't filter by row groups
				null // The sort order
				);
		String rsltInfo = null;
		if (c.moveToFirst()) {
			rsltInfo = c.getString(which_info);
		} else {
		}

		c.close();
		return rsltInfo;
	}
}
