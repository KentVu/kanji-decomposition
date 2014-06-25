package com.kentvu.kanji_decomposition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.kentvu.kanji_decomposition.KanjiPartsDbHelper.KanjiPartsDbContract.KanjiParts;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	private static final int MAX_LINE_LENGTH = 150;
	KanjiPartsDbHelper mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		// check if the database have been already created
		// SQLiteOpenHelperを用いる為、必要ないとする

		mDbHelper = new KanjiPartsDbHelper(getBaseContext());
		// Gets the data repository in write mode
	}

	public void SearchButton_onClick(View view) {
		// get the edittext control containing the searching kanji
		EditText searchKanjiCtrl = (EditText) findViewById(R.id.InputText);
		// get the searching kanji as string
		String searchKanji = searchKanjiCtrl.getText().toString();

		largeMojiDisplay(searchKanji);
		mojiPartsDisplay(searchKanji);
		includingKanjisDisplay();
	}

	private void largeMojiDisplay(String kanji) {
		// display to the largeMojiDisp
		TextView largeMojiDispCtrl = (TextView) findViewById(R.id.LargeMojiDisp);
		// display only the first kanji in string
		largeMojiDispCtrl.setText(kanji.substring(0, 1));
	}

	private void mojiPartsDisplay(String kanji) {
		// get the display control
		TextView partsDispCtrl = (TextView) findViewById(R.id.PartsDisp);
		partsDispCtrl.setMovementMethod(new ScrollingMovementMethod());

		// prepare data
		// get kanji unicode value
		int unival = kanji.toCharArray()[0];
		// retrieve data from database
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		String[] projection = { KanjiParts.COLUMN_NAME_UNICODE_VALUE,
				KanjiParts.COLUMN_NAME_RADICALS };
		Cursor c = db.query(KanjiParts.TABLE_NAME, // The table to query
				projection, // The columns to return
				KanjiParts.COLUMN_NAME_UNICODE_VALUE, // The columns for the
														// WHERE clause
				new String[]{ Integer.toString(unival)}, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				null // The sort order
				);
		c.moveToFirst();
		String parts = c.getString(1);
		
		// display to user
		partsDispCtrl.setText(parts);
	}

	private void includingKanjisDisplay() {
		// get the display control
		TextView partOfDispCtrl = (TextView) findViewById(R.id.PartOfDisp);
		partOfDispCtrl.setMovementMethod(new ScrollingMovementMethod());

		// prepare data
		// BufferedReader br = new BufferedReader(new InputStreamReader(
		// getResources().openRawResource(R.raw.kradfile),
		// Charset.forName("EUC-JP")));

		// display on the control

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

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
