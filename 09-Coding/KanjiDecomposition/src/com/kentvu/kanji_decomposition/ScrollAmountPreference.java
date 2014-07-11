package com.kentvu.kanji_decomposition;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

public class ScrollAmountPreference extends DialogPreference {
	private int mNewValue;
	private SeekBar mSeekBar;
	private ViewGroup mParentView;
	private int mCurrentValue;

	public ScrollAmountPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		setDialogLayoutResource(R.layout.scroll_amount_dialog);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);

		setDialogIcon(null);
	}

	@Override
	protected void onBindDialogView(View view) {
		// TODO Auto-generated method stub
		super.onBindDialogView(view);
		mSeekBar = (SeekBar) view;
		mSeekBar.setProgress(mCurrentValue);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// super.onDialogClosed(positiveResult);
		// When the user selects "OK", persist the new value
		if (positiveResult) {
			if (mSeekBar == null) {
				PreferenceActivity activity = (PreferenceActivity) getContext();
				mSeekBar = (SeekBar) activity.findViewById(R.id.ScrollAmountCtrl);
			}
			if (mSeekBar != null) {
				mNewValue = mSeekBar.getProgress();
				persistInt(mNewValue);
			}
		}
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		// super.onSetInitialValue(restorePersistedValue, defaultValue);
		if (restorePersistedValue) {
			// Restore existing state
			mCurrentValue = this.getPersistedInt(getContext()
					.getResources().getInteger(
							R.integer.default_scroll_amount));
		} else {
			// Set default state from the XML attribute
			mCurrentValue = (Integer) defaultValue;
			persistInt(mCurrentValue);
		}
//		mSeekBar.setProgress(mCurrentValue);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		// return super.onGetDefaultValue(a, index);
		return a.getInteger(
				index,
				getContext().getResources().getInteger(
						R.integer.default_scroll_amount));
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		// return super.onSaveInstanceState();
		final Parcelable superState = super.onSaveInstanceState();
		// Check whether this Preference is persistent (continually saved)
		if (isPersistent()) {
			// No need to save instance state since it's persistent,
			// use superclass state
			return superState;
		}
		
	    // Create instance of custom BaseSavedState
	    final ScrollAmountPreference.SavedState myState = new SavedState(superState);
	    // Set the state's value with the class member that holds current
	    // setting value
	    myState.value = mNewValue;
	    return myState;
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
	    // Check whether we saved the state in onSaveInstanceState
	    if (state == null || !state.getClass().equals(ScrollAmountPreference.SavedState.class)) {
	        // Didn't save the state, so call superclass
	        super.onRestoreInstanceState(state);
	        return;
	    }

	    // Cast state to custom BaseSavedState and pass to superclass
	    ScrollAmountPreference.SavedState myState = (ScrollAmountPreference.SavedState) state;
	    super.onRestoreInstanceState(myState.getSuperState());
	    
	    // Set this Preference's widget to reflect the restored state
	    mSeekBar.setProgress(myState.value);
	}

	private static class SavedState extends BaseSavedState {
		// Member that holds the setting's value
		// Change this data type to match the type saved by your Preference
		int value;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		public SavedState(Parcel source) {
			super(source);
			// Get the current preference's value
			value = source.readInt(); // Change this to read the appropriate
										// data type
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			// Write the preference's value
			dest.writeInt(value); // Change this to write the appropriate
									// data type
		}

		// Standard creator object using an instance of this class
		public static final Parcelable.Creator<ScrollAmountPreference.SavedState> CREATOR = new Parcelable.Creator<ScrollAmountPreference.SavedState>() {

			public ScrollAmountPreference.SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public ScrollAmountPreference.SavedState[] newArray(int size) {
				return new ScrollAmountPreference.SavedState[size];
			}
		};
	}

}