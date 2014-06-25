package com.kentvu.kanji_decomposition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.kentvu.kanji_decomposition.KanjiPartsDbHelper.KanjiPartsDbContract.KanjiParts;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.OpenableColumns;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.widget.Toast;

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
		}

		private static final String TEXT_TYPE = " TEXT";
		private static final String COMMA_SEP = ",";
		public static final String SQL_CREATE_KANJIPARTS = "CREATE TABLE "
				+ KanjiParts.TABLE_NAME + " ("
				+ KanjiParts.COLUMN_NAME_UNICODE_VALUE + " INTEGER PRIMARY KEY"
				+ COMMA_SEP + KanjiParts.COLUMN_NAME_RADICALS + TEXT_TYPE +
				// ... Any other options for the CREATE command
				" )";

		// public static final String SQL_

		public static final String SQL_DELETE_KANJIPARTS = "DROP TABLE IF EXISTS "
				+ KanjiParts.TABLE_NAME;

	}

	// If you change the database schema, you must increment the database
	// version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "KanjiParts.db";

	private Context context;

	public KanjiPartsDbHelper(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		context = ctx;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(KanjiPartsDbContract.SQL_CREATE_KANJIPARTS);
		initDictTable(db);
	}

	private void initDictTable(SQLiteDatabase db) {
		Toast.makeText(context, "Creating database", Toast.LENGTH_LONG).show();
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
