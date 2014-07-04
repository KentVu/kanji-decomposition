package com.kentvu.kanji_decomposition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import com.kentvu.kanji_decomposition.KanjiPartsDbHelper.KanjiPartsDbContract.KanjiParts;

public class KanjiPartsDbHelper extends SQLiteOpenHelper {
	public final class KanjiPartsDbContract {
		// To prevent someone from accidentally instantiating the contract
		// class,
		// give it an empty constructor.
		public KanjiPartsDbContract() {
		}

		/* Inner class that defines the table contents */
		public abstract class KanjiParts implements BaseColumns {
			// public static abstract class KanjiParts {
			public static final String TABLE_NAME = "kanjiparts";
			public static final String COLUMN_NAME_UNICODE_VALUE = "unicode_value";
			public static final String COLUMN_NAME_RADICALS = "radicals";
			public static final String COLUMN_NAME_PARTOF = "part_of";
			public static final String COLUMN_NAME_JIS212_CODE = "jis212_code";
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
//	public boolean DatabaseCreated = false;

	private Context context;

	public KanjiPartsDbHelper(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		context = ctx;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(KanjiPartsDbContract.SQL_CREATE_KANJIPARTS);
		initDictTable(db);
//		DatabaseCreated = true;
	}

	private void initDictTable(SQLiteDatabase db) {
		// Read in each line of kradfile
		// open kradfile?
		BufferedReader br = new BufferedReader(new InputStreamReader(context
				.getResources().openRawResource(R.raw.kradfile),
				Charset.forName("EUC-JP")));

		// for each line
		try {
			String line = br.readLine();
			while (line != null) {
				// check if it's comment or not
				if (!line.trim().startsWith("#")) {
					// if not a comment line then parse it into the table
					char kanji = line.trim().charAt(0);
					String parts = line.substring(line.indexOf(':') + 2);
					// Get kanji unicode value
					int unival = kanji;
					// insert to the table
					ContentValues values = new ContentValues();
					values.put(KanjiParts.COLUMN_NAME_UNICODE_VALUE, unival);
					values.put(KanjiParts.COLUMN_NAME_RADICALS, parts);
					// Insert the new row, returning the primary key value of
					// the new row
					long newRowId;
					newRowId = db.insert(KanjiParts.TABLE_NAME, null, values);
					 Log.w(context.getString(R.string.app_name),
					 "Inserted new row : " + newRowId);
				}
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			br = new BufferedReader(new InputStreamReader(context
					.getResources().openRawResource(R.raw.radkfile),
					Charset.forName("EUC-JP")));
			String line = br.readLine().trim();
			char currentKanji = '\u0000';
			@SuppressWarnings("unused")
			int kanjiStroke;
			String includingKanji = "";
			String jis212code = "";
			// for each line
			while (line != null) {
				// check if it's started with a "$"
				if (line.startsWith("$")) {
					// last time stored includingKanji information available?
					if (!"".equals(includingKanji) && currentKanji != '\u0000') {
						// update database's table
						ContentValues values = new ContentValues();
						int unival = currentKanji;
						// values.put(KanjiParts.COLUMN_NAME_UNICODE_VALUE,
						// unival);
						values.put(KanjiParts.COLUMN_NAME_PARTOF,
								includingKanji);
						values.put(KanjiParts.COLUMN_NAME_JIS212_CODE,
								jis212code);
						// Which row to update, based on the ID
						String selection = KanjiParts.COLUMN_NAME_UNICODE_VALUE
								+ " = ?";
						String[] selectionArgs = { String.valueOf(unival) };
						// update
						int count = db.update(KanjiParts.TABLE_NAME, values,
								selection, selectionArgs);
						Log.i(context.getString(R.string.app_name),
								"Updated part_of info : unival " + unival
										+ ", " + count + " row affected");
						if (count == 0) {
							// if kanji is not available, insert
							values.put(KanjiParts.COLUMN_NAME_UNICODE_VALUE,
									unival);
							long newRowId = db.insert(KanjiParts.TABLE_NAME,
									null, values);
							Log.w(context.getString(R.string.app_name),
									"Kanji is not available, inserted new row : " + newRowId);
						}
						// clear values
						includingKanji = "";
					}
					String tokens[] = line.split(" ");
					currentKanji = tokens[1].charAt(0);
					kanjiStroke = Integer.parseInt(tokens[2]);
					if (tokens.length >= 4) {
						jis212code = tokens[3];
					}
				} else {
					// check if it's not started with a "#"
					if (!line.startsWith("#")) {
						// append the including Kanji list
						includingKanji += line;	// remember to clear it when done appending!
					}
				}
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Toast.makeText(context, "Database Created", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade
		// policy is
		// to simply to discard the data and start over
		db.execSQL(KanjiPartsDbContract.SQL_DELETE_KANJIPARTS);
		onCreate(db);

	}

}
