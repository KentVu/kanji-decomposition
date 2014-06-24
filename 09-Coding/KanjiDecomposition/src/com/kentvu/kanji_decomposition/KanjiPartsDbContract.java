package com.kentvu.kanji_decomposition;

import android.provider.BaseColumns;

public final class KanjiPartsDbContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public KanjiPartsDbContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class KanjiParts implements BaseColumns {
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
