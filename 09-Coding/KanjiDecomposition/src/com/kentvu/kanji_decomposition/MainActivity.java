package com.kentvu.kanji_decomposition;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.kentvu.kanji_decomposition.KanjiPartsDbHelper.KanjiPartsDbContract.KanjiParts;

public class MainActivity extends ActionBarActivity implements
		PlaceHolderFragmentMessages {

	// private static final int MAX_LINE_LENGTH = 150;
	KanjiPartsDbHelper mDbHelper;
	SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			PlaceholderFragment placeholderFragment = new PlaceholderFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, placeholderFragment).commit();
		}

		mDbHelper = new KanjiPartsDbHelper(getBaseContext());
		// notify the user of creating database may take time
		// TODO: wrap these inside kinda parallelizing code?
		try {
			SQLiteDatabase dbe = SQLiteDatabase
					.openDatabase(
							getDatabasePath(KanjiPartsDbHelper.DATABASE_NAME)
									.getPath(), null, 0);
			Log.i("sqlite", getDatabasePath(KanjiPartsDbHelper.DATABASE_NAME)
					.getPath() + " exists");
			dbe.close();
		} catch (SQLiteException e) {
			// If not created, notify the user (via Toast)
			Log.w("sqlite", getDatabasePath(KanjiPartsDbHelper.DATABASE_NAME)
					.getPath() + " NOT exists");
			Toast.makeText(
					this,
					"Creating database may take time on first search, please be patient!",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/**
	 * Handle PlaceholderFragment's messages
	 */
	@Override
	public void onFragmentViewCreated(View view) {
		// placeholder
	}

	public void SearchButton_onClick(View view) {
		// get the edittext control containing the searching kanji
		EditText searchKanjiCtrl = (EditText) findViewById(R.id.KanjiInput);
		// get the searching kanji as string
		String searchKanji = searchKanjiCtrl.getText().toString();

		db = mDbHelper.getReadableDatabase();
		largeMojiDisplay(searchKanji);
		// mDbHelper.getReadableDatabase().beginTransaction();
		mojiPartsDisplay(searchKanji);
		includingKanjisDisplay(searchKanji);
		db.close();
	}

	private void largeMojiDisplay(String kanji) {
		// display to the largeMojiDisp
		TextView largeMojiDispCtrl = (TextView) findViewById(R.id.LargeMojiDisp);
		// display only the first kanji in string
		largeMojiDispCtrl.setText(kanji.substring(0, 1));

		// for debug purpose:
		TextView debugDisp = (TextView) findViewById(R.id.DebugDisp);
		debugDisp.setText(Integer.toString(kanji.charAt(0)));
	}

	private void mojiPartsDisplay(String kanji) {
		// get the display control
		TextView partsDispCtrl = (TextView) findViewById(R.id.PartsDisp);
		partsDispCtrl.setMovementMethod(new ScrollingMovementMethod());

		// prepare data
		// get kanji unicode value
		int unival = kanji.charAt(0);
		// *Retrieve data from database
		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		String[] projection = { KanjiParts.COLUMN_NAME_UNICODE_VALUE,
				KanjiParts.COLUMN_NAME_RADICALS };
		// long execution time when creating database (calling on create of
		// mDbHelper) (use some kind of parallelizing to overcome/deal with
		// this?)
		Cursor c = db.query(KanjiParts.TABLE_NAME, // The table to query
				projection, // The columns to return
				KanjiParts.COLUMN_NAME_UNICODE_VALUE + " = ?", // The
																// columns
																// for
																// the WHERE
																// clause
				new String[] { Integer.toString(unival) }, // The values for the
															// WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				null // The sort order
				);
		String parts = null;

		// *display to user
		if (c.moveToFirst()) {
			parts = c.getString(1);
			partsDispCtrl.setText(parts);
		} else {
			// searching kanji is not exists in database
			partsDispCtrl.setHint(getResources().getString(
					R.string.character_not_found));
		}

		// if (parts != null) {
		// } else {
		// }
		c.close();
	}

	private void includingKanjisDisplay(String kanji) {
		// get the display control
		TextView partOfDispCtrl = (TextView) findViewById(R.id.PartOfDisp);
		partOfDispCtrl.setMovementMethod(new ScrollingMovementMethod());
		// clear hint text
		partOfDispCtrl.setHint("");

		// prepare data
		int unival = kanji.charAt(0);
		String[] projection = { KanjiParts.COLUMN_NAME_UNICODE_VALUE,
				KanjiParts.COLUMN_NAME_PARTOF };
		Cursor c = db.query(KanjiParts.TABLE_NAME, // The table to query
				projection, // The columns to return
				KanjiParts.COLUMN_NAME_UNICODE_VALUE + " = ?", // The
																// columns
																// for
																// the WHERE
																// clause
				new String[] { Integer.toString(unival) }, // The values for the
															// WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				null // The sort order
				);
		if (c.moveToFirst()) {
			String includingKanji = c.getString(1);
			// display on the control
			partOfDispCtrl.setText(includingKanji);
		} else {
			// searching kanji is not exists in database
			// donothing!
		}
		c.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		PlaceHolderFragmentMessages mCallbacks;
		private MainActivity mainActivity;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			mainActivity = (MainActivity) getActivity();

			// get the edittext control containing the searching kanji
			EditText searchKanjiCtrl = (EditText) rootView
					.findViewById(R.id.KanjiInput);
			searchKanjiCtrl
					.setOnEditorActionListener(new OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView v, int actionId,
								KeyEvent event) {
							boolean handled = false;
							if (actionId == EditorInfo.IME_ACTION_SEARCH) {
								mainActivity.SearchButton_onClick(v);
								InputMethodManager imm = (InputMethodManager) mainActivity
										.getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(v.getWindowToken(),
										InputMethodManager.HIDE_NOT_ALWAYS);
								handled = true;
							}
							return handled;
						}
					});
			// notify MainActivity
			mCallbacks.onFragmentViewCreated(rootView);

			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);

			// This makes sure that the container activity has implemented
			// the callback interface. If not, it throws an exception
			try {
				mCallbacks = (PlaceHolderFragmentMessages) activity;
			} catch (ClassCastException e) {
				throw new ClassCastException(activity.toString()
						+ " must implement PlaceHolderFragmentMessages");
			}
		}

	}
}
