package com.kentvu.kanji_decomposition;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.ClipboardManager;
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
	public void onFragmentViewCreated(View view);
}

public class MainActivity extends ActionBarActivity implements
		PlaceHolderFragmentMessages, SettingsDialogListener {

	// private static final int MAX_LINE_LENGTH = 150;
	KanjiPartsDbHelper mDbHelper;
	SQLiteDatabase db;
	// private boolean[] mCheckItemSettings;
	private boolean mCopyOnClick = false;

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
		// TODO: wrap these inside somewhat parallelizing code?
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
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

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
				// ClipData clip = ClipData.newPlainText("Kanji", content);
				// clipboard.setPrimaryClip(clip);
				clipboard.setText(content);
				Toast.makeText(getBaseContext(),
						"'" + content + "'" + " copied", Toast.LENGTH_SHORT)
						.show();
			}

			EditText kanjiInput = (EditText) findViewById(R.id.KanjiInput);
			try {
				kanjiInput.setText(content);
			} catch (NullPointerException e) {
				// on some occasion (such as screen orientation change), the
				// layout gets recreated so the View cannot be retrieved, catch
				// it just in case
				e.printStackTrace();
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
		String searchKanji = searchKanjiCtrl.getText().toString();

		db = mDbHelper.getReadableDatabase();
		largeMojiDisplay(searchKanji);
		// mDbHelper.getReadableDatabase().beginTransaction();
		KanjiComponentsDisplay(searchKanji);
		includingKanjisDisplay(searchKanji);
		db.close();
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
		largeMojiDispCtrl.setText(kanji.substring(0, 1));

		// for debug purpose:
		TextView debugDisp = (TextView) findViewById(R.id.DebugDisp);
		debugDisp.setText(Integer.toString(kanji.charAt(0)));
	}

	private void KanjiComponentsDisplay(String kanji) {
		// get the display control
		TextView partsDispCtrl = (TextView) findViewById(R.id.PartsDisp);

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
		if ((c.moveToFirst()) && ((parts = c.getString(1)) != null)) {
			SpannableString ss = makeClickableSpanString(partsDispCtrl, ' ',
					parts);
			partsDispCtrl.setText(ss);

		} else {
			// searching kanji is not exists in database
			partsDispCtrl.setText("");
			partsDispCtrl.setHint(getResources().getString(
					R.string.parts_not_found));
		}

		// if (parts != null) {
		// } else {
		// }
		c.close();
	}

	private void includingKanjisDisplay(String kanji) {
		// get the display control
		TextView partOfDispCtrl = (TextView) findViewById(R.id.PartOfDisp);
		// clear hint text
		partOfDispCtrl.setHint("");
		// clear text
		partOfDispCtrl.setText("");

		// prepare data
		int unival = kanji.charAt(0);
		String[] projection = { KanjiParts.COLUMN_NAME_UNICODE_VALUE,
				KanjiParts.COLUMN_NAME_PARTOF };
		Cursor sqlCur = db.query(
				KanjiParts.TABLE_NAME, // The table to query
				projection, // The columns to return
				// The columns for the WHERE clause
				KanjiParts.COLUMN_NAME_UNICODE_VALUE + " = ?",
				new String[] { Integer.toString(unival) }, // The values for the
															// WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				null // The sort order
				);
		if (sqlCur.moveToFirst()) {
			String includingKanjis = sqlCur.getString(1);
			// display on the control
			if (includingKanjis != null) {
				// if totalCharstoFit is odd
				// build a string which adds a space after each character
				StringBuilder sb = new StringBuilder();
				for (char c : includingKanjis.toCharArray()) {
					sb.append(c);
					sb.append('　'); // "Japanese" space
				}

				SpannableString ss = makeClickableSpanString(partOfDispCtrl,
						'　', sb.toString());
				// since PartOfDisp has been set to LinkMovementMethod, this is
				// okay!
				partOfDispCtrl.append(ss); // 「append」だから、clearを忘れないようね
				// partOfDispCtrl.append("　");
				// partOfDispCtrl.setText(sb);
			} else {
				// this kanji do not have any other kanji including it
				partOfDispCtrl.setText("");
			}
		} else {
			// searching kanji is not exists in database
			partOfDispCtrl.setText("");
		}
		sqlCur.close();
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
		int totalCharstoFit = displayView.getPaint().breakText(displayString,
				0, displayString.length(), true, displayView.getWidth(), null);
		// beware of totalCharstoFit, if a line started by a space then
		// delete that space
		int charCount = 0;
		StringBuilder sb = new StringBuilder(displayString);
		while (charCount < sb.length()) {
			if (sb.charAt(charCount) == separatorSpace) {
				sb.deleteCharAt(charCount);
			}
			charCount += totalCharstoFit;
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

	/**
	 * Handle PlaceholderFragment's messages
	 */
	@Override
	public void onFragmentViewCreated(View view) {
		// placeholder
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
			// "When you want to show your dialog, create an instance of your
			// DialogFragment and call show(), passing the FragmentManager and a
			// tag name for the dialog fragment."
			SettingsDialog myDialog = new SettingsDialog();
			myDialog.show(getSupportFragmentManager(), "settings");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Handles SettingsDialog's callbacks
	 */
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		// get checked items
		SettingsDialog settingsDialog = (SettingsDialog) dialog;
		ArrayList<String> checkedItemNames = new ArrayList<String>();

		for (int checkedItemId : settingsDialog.selectedItems) {
			checkedItemNames
					.add(getResources().getStringArray(R.array.options)[checkedItemId]);
			// mCheckItemSettings[checkedItemId] = true;
			// TODO The following equals() check seems to be CPU expensive, but
			// there's no other way :(
			if (getResources().getStringArray(R.array.options)[checkedItemId]
					.equals(getResources().getString(R.string.copy_on_click))) {
				mCopyOnClick = true;
			}
			else{
				// place more elseif here!
			}
		}

		if (BuildConfig.DEBUG) {
			Toast.makeText(getBaseContext(),
					"Checked items: " + checkedItemNames
					// + "\n" + mCheckItemSettings
					, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		if (BuildConfig.DEBUG) {
			Toast.makeText(getBaseContext(), "'Cancel' clicked",
					Toast.LENGTH_SHORT).show();
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
			TextView partOfDispCtrl = (TextView) rootView
					.findViewById(R.id.PartOfDisp);
			partOfDispCtrl.setMovementMethod(LinkMovementMethod.getInstance());
			TextView partsDispCtrl = (TextView) rootView
					.findViewById(R.id.PartsDisp);
			partsDispCtrl.setMovementMethod(LinkMovementMethod.getInstance());

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

	public static class SettingsDialog extends DialogFragment {

		// Use this instance of the interface to deliver action events
		SettingsDialogListener mListener;

		// Where we track the selected items
		ArrayList<Integer> selectedItems = new ArrayList<Integer>();

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			// Verify that the host activity implements the callback interface
			try {
				// Instantiate the NoticeDialogListener so we can send events to
				// the host
				mListener = (SettingsDialogListener) activity;
			} catch (ClassCastException e) {
				// The activity doesn't implement the interface, throw exception
				throw new ClassCastException(activity.toString()
						+ " must implement SettingsDialogListener");
			}
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// 1. Instantiate an AlertDialog.Builder with its constructor
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			// 2. Chain together various setter methods to set the dialog
			// characteristics
			builder.setTitle("Settings")
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mListener
											.onDialogPositiveClick(SettingsDialog.this);
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mListener
											.onDialogNegativeClick(SettingsDialog.this);
								}
							})
					.setMultiChoiceItems(R.array.options, null,
							new DialogInterface.OnMultiChoiceClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
									if (isChecked) {
										// If the user checked the item, add it
										// to the selected items
										selectedItems.add(which);
									} else if (selectedItems.contains(which)) {
										// Else, if the item is already in the
										// array, remove it
										selectedItems.remove(Integer
												.valueOf(which));
									}
								}
							});

			// 3. Get the AlertDialog from create()
			return builder.create();
		}
	}
}
