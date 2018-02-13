package org.nv95.openmanga.common.views.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import org.nv95.openmanga.R;

public final class IntPickerPreference extends DialogPreference implements IntegerPreference, NumberPicker.OnValueChangeListener {

	private int mValue;
	private boolean mValueSet;
	private final int mMinValue;
	private final int mMaxValue;
	private int mNewValue;

	public IntPickerPreference(Context context) {
		this(context, null, android.R.attr.dialogPreferenceStyle);
	}

	public IntPickerPreference(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.dialogPreferenceStyle);
	}

	public IntPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setDialogLayoutResource(R.layout.dialog_pref_intpicker);
		final TypedArray a = getContext().obtainStyledAttributes(
				attrs, R.styleable.IntSelectPreferenceAttrs);
		mMinValue = a.getInt(R.styleable.IntSelectPreferenceAttrs_minValue, 0);
		mMaxValue = a.getInt(R.styleable.IntSelectPreferenceAttrs_maxValue, 100);
		a.recycle();
	}

	public void setValue(int newValue) {
		// Always persist/notify the first time.
		final boolean changed = mValue != newValue;
		if (changed || !mValueSet) {
			mValue = newValue;
			mValueSet = true;
			persistInt(newValue);
			if(changed) {
				notifyDependencyChange(shouldDisableDependents());
				notifyChanged();
			}
		}
	}

	@Override
	public int getValue() {
		return mValue;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		final NumberPicker picker = view.findViewById(R.id.numberPicker);
		picker.setMinValue(mMinValue);
		picker.setMaxValue(mMaxValue);
		picker.setValue(mValue);
		picker.setOnValueChangedListener(this);
		mNewValue = mValue;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			if (callChangeListener(mNewValue)) {
				setValue(mNewValue);
			}
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 0);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		setValue(restoreValue ? getPersistedInt(mValue) : (Integer) defaultValue);
	}

	@Override
	public boolean shouldDisableDependents() {
		return mValue == 0 || super.shouldDisableDependents();
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		mNewValue = newVal;
	}
}
