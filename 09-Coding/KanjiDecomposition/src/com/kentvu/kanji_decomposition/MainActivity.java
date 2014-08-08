package com.kentvu.kanji_decomposition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.ClipboardManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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

/* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
interface SettingsDialogListener {
	public void onDialogPositiveClick(DialogFragment dialog);

	public void onDialogNegativeClick(DialogFragment dialog);
}

// Container Activity must implement this interface
interface PlaceHolderFragmentMessages {
	public void onSearchKanjiSubmitted(View view);
}

public class MainActivity extends ActionBarActivity implements
		PlaceHolderFragmentMessages {

	private static final String KEY_LAST_SEARCH_KANJIS = "last_search_kanji";
	// private static final int MAX_LINE_LENGTH = 150;
	KanjiPartsDbHelper mDbHelper;
	SQLiteDatabase db;
	// class DialogSettings{
	// mCopyOnClick,
	// mBrowseOnClick;
	// }
	private boolean mCopyOnClick = false;
	private boolean mBrowseOnClick = false;
	private boolean mShowToasts = false;
	private DatabaseOpenTask databaseOpenTask;

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
		// mCheckItemSettings = new boolean[getResources().getStringArray(
		// R.array.options).length];
		reloadPreferences();
	}

	/**
	 * 
	 */
	private void reloadPreferences() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		mCopyOnClick = sharedPref.getBoolean(
				SettingsActivity.KEY_PREF_COPY_ON_CLICK, false);
		mBrowseOnClick = sharedPref.getBoolean(
				SettingsActivity.KEY_PREF_BROWSE_ON_CLICK, false);
		mShowToasts = sharedPref.getBoolean(
				SettingsActivity.KEY_PREF_SHOW_TOASTS, false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(KEY_LAST_SEARCH_KANJIS,
				((TextView) findViewById(R.id.SearchQueue)).getText()
						.toString());
		editor.commit();
	}

	@Override
	protected void onStart() {
		super.onStart();
		reloadPreferences();
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		TextView searchQueue = (TextView) findViewById(R.id.SearchQueue);
		SpannableString ss = makeClickableSpanString(searchQueue, ' ',
				sharedPref.getString(KEY_LAST_SEARCH_KANJIS, ""));
		searchQueue.setText(ss);
		databaseOpenTask = new DatabaseOpenTask();
	}

	// @Override
	// protected void onDestroy() {
	// super.onDestroy();
	// }

	class MyClickableSpan extends ClickableSpan {

		String content = null;

		public MyClickableSpan(String _content) {
			super();
			content = _content;
		}

		@Override
		public void onClick(View widget) {
			if (mCopyOnClick) {
				// Gets a handle to the clipboard service.
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				// Creates a new text clip to put on the clipboard
				// uncomment the following 2 lines if only support API 11
				// (Android 3.0) and later
				// ClipData clip = ClipData.newPlainText("Kanji", content);
				// clipboard.setPrimaryClip(clip);
				clipboard.setText(content);
				if (mShowToasts) {
					Toast.makeText(getBaseContext(),
							"'" + content + "'" + " copied", Toast.LENGTH_SHORT)
							.show();
				}
			}
			if (mBrowseOnClick) {
				EditText kanjiInput = (EditText) findViewById(R.id.KanjiInput);
				try {
					kanjiInput.setText(content);
				} catch (NullPointerException e) {
					// on some occasion (such as screen orientation change), the
					// layout gets recreated so the View cannot be retrieved,
					// catch
					// it just in case
					e.printStackTrace();
				}
				SearchButton_onClick(widget);
			} else if ((widget.getId() == R.id.SearchQueue)) {
				// always browse on SearchQueue clicked
				SearchButton_onClick(widget);
			}

		}

		@Override
		public void updateDrawState(TextPaint ds) {
			// overide but don't call super -> ignore link-like formating
			// super.updateDrawState(ds);
			// ds.setColor(Color.BLACK);//set text color
			// ds.setUnderlineText(false); // set to false to remove underline
		}
	}

	public void SearchButton_onClick(View view) {
		// get the edittext control containing the searching kanji
		EditText searchKanjiCtrl = (EditText) findViewById(R.id.KanjiInput);
		// get the searching kanji as string
		String searchKanjis = searchKanjiCtrl.getText().toString();
		// put into the search queue ONLY after the user actually inputed into
		// the EditText
		if (view.getId() == R.id.SearchButton
				|| view.getId() == R.id.KanjiInput) {
			TextView searchQueue = (TextView) findViewById(R.id.SearchQueue);
			// make the text clickable
			// XXX Check if character is available in database
			String spacedString = insertSpaceBetweenCharacters(searchKanjis,
					' ');
			SpannableString ss = makeClickableSpanString(searchQueue, ' ',
					spacedString.toString());
			searchQueue.setText(ss);
		}
		largeMojiDisplay(searchKanjis);
		// "Waits if necessary for the computation to complete, and then retrieves its result."
//		SQLiteDatabase db;
		// db = databaseOpenTask.get();
		// mDbHelper.getReadableDatabase().beginTransaction();
		if (databaseOpenTask.getStatus() == Status.FINISHED) {
			db = mDbHelper.getReadableDatabase();
			KanjiComponentsDisplay(searchKanjis);
			includingKanjisDisplay(searchKanjis);
			db.close();
		} else {
			databaseOpenTask.execute(mDbHelper);
		}
		// close soft keyboard
		// InputMethodManager imm = (InputMethodManager)
		// getSystemService(Context.INPUT_METHOD_SERVICE);
		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(view.getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private void largeMojiDisplay(String kanji) {
		// display to the largeMojiDisp
		TextView largeMojiDispCtrl = (TextView) findViewById(R.id.LargeMojiDisp);
		// display only the first kanji in string
		SpannableString ss = new SpannableString(kanji.substring(0, 1));
		ss.setSpan(new MyClickableSpan(ss.toString()), 0, 1,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		largeMojiDispCtrl.setText(ss);

		if (BuildConfig.DEBUG) {
			// for debug purpose:
			TextView debugDisp = (TextView) findViewById(R.id.DebugDisp);
			debugDisp.setText(Integer.toString(kanji.charAt(0)));
		}
	}

	private void KanjiComponentsDisplay(String kanji) {
		// get the display control
		TextView partsDispCtrl = (TextView) findViewById(R.id.PartsDisp);

		String parts = KanjiPartsDbHelper.queryKanji(db, kanji.charAt(0),
				KanjiParts.COLUMN_ID_RADICALS);

		// *display to user
		if (parts != null) {
			SpannableString ss = makeClickableSpanString(partsDispCtrl, ' ',
					parts);
			partsDispCtrl.setText(ss);
		} else {
			// searching kanji is not exists in database
			partsDispCtrl.setText("");
			partsDispCtrl.setHint(getResources().getString(
					R.string.parts_not_found));
		}
	}

	private void includingKanjisDisplay(String kanji) {
		// get the display control
		TextView partOfDispCtrl = (TextView) findViewById(R.id.PartOfDisp);
		// clear hint text
		partOfDispCtrl.setHint("");
		// clear text
		partOfDispCtrl.setText("");
		partOfDispCtrl.scrollTo(0, 0);

		String includingKanjis = mDbHelper.queryKanji(db, kanji.charAt(0),
				KanjiParts.COLUMN_ID_PARTOF);

		if (includingKanjis != null) {
			// build a string which adds a space after each character
			String spacedString = insertSpaceBetweenCharacters(includingKanjis,
					' ');
			SpannableString ss = makeClickableSpanString(partOfDispCtrl, ' ',
					spacedString.toString());
			// since PartOfDisp has been set to LinkMovementMethod, this is
			// okay!
			partOfDispCtrl.append(ss); // 「append」だから、clearを忘れないようね
			// partOfDispCtrl.append("　");
			// partOfDispCtrl.setText(sb);
		} else {
			// this kanji do not have any other kanji including it
			partOfDispCtrl.setText("");
		}
	}

	/**
	 * Builds a string which adds a space after each character
	 * 
	 * @param s
	 * @return
	 */
	private String insertSpaceBetweenCharacters(String s, char spaceType) {
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (c != spaceType) {
				sb.append(c);
				// sb.append('　'); // "Japanese" space
				sb.append(' ');
			}
		}
		return sb.toString();
	}

	/**
	 * Make each visible character to be spannable (ignoring spaces)
	 * 
	 * @param displayView
	 *            TextView to be displayed in (for measuring purpose)
	 * @param separatorSpace
	 *            space type to be considered (Japanese space or ordinary space
	 *            (0xa)
	 * @param displayString
	 *            String to make spannable
	 * @return Spannable string
	 */
	private SpannableString makeClickableSpanString(TextView displayView,
			char separatorSpace, String displayString) {
		// get maximum number of character that can be fit into a line
		// XXX sometimes (in my case, it's when the displayView's height is set
		// to match_parent) this may return 0, which result in a forever loop
		// later, what better should be done here?
		int totalCharstoFit = displayView.getPaint().breakText(displayString,
				0, displayString.length(), true, displayView.getWidth(), null);
		// beware of totalCharstoFit, if a line started by a space then
		// delete that space
		int charCount = 0;
		StringBuilder sb = new StringBuilder(displayString);
		if (totalCharstoFit > 0) {
			while (charCount < sb.length()) {
				if (sb.charAt(charCount) == separatorSpace) {
					sb.deleteCharAt(charCount);
				}
				charCount += totalCharstoFit;
			}
		}
		// if this kanji has part_of information
		// make every displayed kanji clickable:
		SpannableString ss = new SpannableString(sb);
		for (int i = 0; i < sb.length(); i++) {
			if (sb.charAt(i) != separatorSpace) {
				ss.setSpan(new MyClickableSpan(sb.substring(i, i + 1)), i,
						i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return ss;
	}

	public void onScrollBtnClick(View view) {
		TextView partOfCtrl = (TextView) findViewById(R.id.PartOfDisp);
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		int yScrollAmount = sharedPref.getInt(
				SettingsActivity.KEY_PREF_SCROLL_AMOUNT, 50);
		if (view.getId() == R.id.ScrollUpBtn) {
			// XXX check if partOf view is overloaded (scrolling is available)
			partOfCtrl.scrollBy(0, -yScrollAmount);
		} else if (view.getId() == R.id.ScrollDownBtn) {
			partOfCtrl.scrollBy(0, yScrollAmount);
		}
	}

	/**
	 * Handle PlaceholderFragment's messages
	 */
	@Override
	public void onSearchKanjiSubmitted(View view) {
		SearchButton_onClick(view);
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
			// Start the SettingsActivity
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			settingsIntent.setAction(Intent.ACTION_VIEW);
			startActivity(settingsIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Handles SettingsDialog's callbacks
	 */
	// @Override
	// public void onDialogPositiveClick(DialogFragment dialog) {
	// get checked items
	// SettingsDialog settingsDialog = (SettingsDialog) dialog;
	// ArrayList<String> checkedItemNames = new ArrayList<String>();

	// for (int checkedItemId : settingsDialog.selectedItems) {
	// checkedItemNames
	// .add(getResources().getStringArray(R.array.options)[checkedItemId]);
	// mCheckItemSettings[checkedItemId] = true;
	// TODO The following equals() check seems to be CPU expensive, but
	// there's no other way :(
	// if (getResources().getStringArray(R.array.options)[checkedItemId]
	// .equals(getResources().getString(R.string.copy_on_click))) {
	// mCopyOnClick = true;
	// } else if (getResources().getStringArray(R.array.options)[checkedItemId]
	// .equals(getResources().getString(R.string.copy_on_click))) {
	// mBrowseOnClick = true;
	// }
	// }

	// if (BuildConfig.DEBUG) {
	// Toast.makeText(getBaseContext(),
	// "Checked items: " + checkedItemNames
	// // + "\n" + mCheckItemSettings
	// , Toast.LENGTH_SHORT).show();
	// }
	// }

	// @Override
	// public void onDialogNegativeClick(DialogFragment dialog) {
	// // TODO Auto-generated method stub
	// if (BuildConfig.DEBUG) {
	// Toast.makeText(getBaseContext(), "'Cancel' clicked",
	// Toast.LENGTH_SHORT).show();
	// }
	// }

	/**
	 * Handles long time execution tasks in separate thread
	 */
	private class DatabaseOpenTask extends
			AsyncTask<KanjiPartsDbHelper, Integer, SQLiteDatabase> {
		SQLiteDatabase mDb;

		@Override
		protected SQLiteDatabase doInBackground(KanjiPartsDbHelper... params) {
			// TODO Auto-generated method stub
			mDb = params[0].getWritableDatabase();
			publishProgress(1, 100, 100);
			if (params[0].NeedUpdate)
				initDictTable(mDb);
			params[0].NeedUpdate = false;
			return mDb;
		}

		/**
		 * @param values
		 *            : 1: step, 2: rowInserted, 3: rowUpdated
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			// super.onProgressUpdate(values);
			TextView partsCtrl = (TextView) findViewById(R.id.PartsDisp);
			TextView partOfCtrl = (TextView) findViewById(R.id.PartOfDisp);
			try {
				partsCtrl.setHint("RowInserted: " + values[1]
						+ "\nRowUpdated: " + values[2]);
				switch (values[0]) {
				case 0:
					partOfCtrl.setHint("Reading in kradfile...");
					break;
				case 1:
					partOfCtrl.setHint("Reading in radkfile...");
					break;
				default:
					partOfCtrl.setHint("Read completed!");
					break;
				}
			} catch (NullPointerException e) {
				// TODO: handle exception
//				e.printStackTrace();
				Log.w("DbCreateTask", "My activity has been navigated away?");
			}
		}

		/**
		 * See activity chart (astah file) for algorithm
		 * 
		 * @param db
		 */
		private void initDictTable(SQLiteDatabase db) {
			Integer step = 0;
			Integer rowInserted = 0;
			Integer rowUpdated = 0;

			// Read in each line of kradfile
			// open kradfile?
			BufferedReader br = new BufferedReader(new InputStreamReader(
					getResources().openRawResource(R.raw.kradfile),
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
						// Insert the new row, returning the primary key value
						// of the new row
						long newRowId;
						newRowId = db.insert(KanjiParts.TABLE_NAME, null,
								values);
						Log.w(getString(R.string.app_name),
								"Inserted new row : " + newRowId);
						rowInserted++;
						if ((rowInserted % 10) == 0) {
							// Try to keep this parameter order
							publishProgress(step, rowInserted, rowUpdated);
						}
					}
					line = br.readLine();
				}
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				step = 1;
				br = new BufferedReader(new InputStreamReader(getResources()
						.openRawResource(R.raw.radkfile),
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
						// last time stored includingKanji information
						// available?
						if (!"".equals(includingKanji)
								&& currentKanji != '\u0000') {
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
							int count = db.update(KanjiParts.TABLE_NAME,
									values, selection, selectionArgs);
							Log.i(getString(R.string.app_name),
									"Updated part_of info : unival " + unival
											+ ", " + count + " row affected");
							if (count == 0) {
								// if kanji is not available, insert
								values.put(
										KanjiParts.COLUMN_NAME_UNICODE_VALUE,
										unival);
								long newRowId = db.insert(
										KanjiParts.TABLE_NAME, null, values);
								Log.w(getString(R.string.app_name),
										"Kanji is not available, inserted new row : "
												+ newRowId);
								rowInserted++;
								publishProgress(step, rowInserted, rowUpdated);
							} else {
								// rowUpdated++;
								rowUpdated += count;
								// if ((rowUpdated % 50) == 0) {
								publishProgress(step, rowInserted, rowUpdated);
								// }
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
							includingKanji += line; // remember to clear it when
													// done appending!
						}
					}
					line = br.readLine();
				}
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Toast.makeText(context, "Database Created",
			// Toast.LENGTH_LONG).show();
			step++;
			publishProgress(step, rowInserted, rowUpdated);
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		PlaceHolderFragmentMessages mCallbacks;
		private MainActivity mainActivity;

		private OnEditorActionListener searchKanjiInputActionListener = new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					// notify mainActivity
					// mainActivity.SearchButton_onClick(v);
					mCallbacks.onSearchKanjiSubmitted(v);
					// hide softIme
					InputMethodManager imm = (InputMethodManager) mainActivity
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
					handled = true;
				}
				return handled;
			}
		};

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
					.setOnEditorActionListener(searchKanjiInputActionListener);
			TextView largeMojiCtrl = (TextView) rootView
					.findViewById(R.id.LargeMojiDisp);
			largeMojiCtrl.setMovementMethod(LinkMovementMethod.getInstance());
			TextView partOfDispCtrl = (TextView) rootView
					.findViewById(R.id.PartOfDisp);
			partOfDispCtrl.setMovementMethod(LinkMovementMethod.getInstance());
			TextView partsDispCtrl = (TextView) rootView
					.findViewById(R.id.PartsDisp);
			partsDispCtrl.setMovementMethod(LinkMovementMethod.getInstance());
			TextView searchQueue = (TextView) rootView
					.findViewById(R.id.SearchQueue);
			searchQueue.setMovementMethod(LinkMovementMethod.getInstance());

			// notify MainActivity
			// mCallbacks.onFragmentViewCreated(rootView);

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
