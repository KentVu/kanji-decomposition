package com.kentvu.kanji_decomposition;

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
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
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

	private void mojiPartsDisplay(String kanji) {
		// get the display control
		TextView partsDispCtrl = (TextView) findViewById(R.id.PartsDisp);
		partsDispCtrl.setMovementMethod(new ScrollingMovementMethod());

		// Get kanji unicode value
		char[] kanjiArray = kanji.toCharArray();
		int unival = kanjiArray[0];
		// partsDispCtrl.setText(Integer.toHexString(unival));
	}

	private void includingKanjisDisplay() {
		// get the display control
		TextView partOfDispCtrl = (TextView) findViewById(R.id.PartOfDisp);
		partOfDispCtrl.setMovementMethod(new ScrollingMovementMethod());

	}

	private void largeMojiDisplay(String kanji) {
		// display to the largeMojiDisp
		TextView largeMojiDispCtrl = (TextView) findViewById(R.id.LargeMojiDisp);
		// display only the first kanji in string
		largeMojiDispCtrl.setText(kanji.substring(0, 1));
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
