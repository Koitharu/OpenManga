package org.nv95.openmanga.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import org.nv95.openmanga.R;

/**
 * Created by admin on 24.07.17.
 */

public class PickerPreference extends DialogPreference implements IntegerPreference {

    private final int mMaxValue;
    private final int mMinValue;
    private int mValue;
    private boolean mValueSet;
    private NumberPicker mPicker;

    public PickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.pref_picker);
        TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.IntSelectPreferenceAttrs);
        mMaxValue = a.getInt(R.styleable.IntSelectPreferenceAttrs_maxValue, 100);
        mMinValue = a.getInt(R.styleable.IntSelectPreferenceAttrs_minValue, 0);
        mValue = 100;
        a.recycle();
        mValueSet = false;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mPicker = view.findViewById(R.id.numberPicker);
        mPicker.setMinValue(mMinValue);
        mPicker.setMaxValue(mMaxValue);
        mPicker.setValue(mValue);
    }

    public void setValue(int value) {
        // Always persist/notify the first time.
        final boolean changed = mValue != value;
        if (changed || !mValueSet) {
            mValue = value;
            mValueSet = true;
            persistInt(value);
            if (changed) {
                notifyChanged();
            }
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            OnPreferenceChangeListener changeListener = getOnPreferenceChangeListener();
            if (changeListener == null || changeListener.onPreferenceChange(this, mPicker.getValue())) {
                setValue(mPicker.getValue());
            }
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        int newValue = 0;
        if (defaultValue instanceof Integer) {
            newValue = (int) defaultValue;
        } else if (defaultValue instanceof String) {
            newValue = Integer.parseInt((String) defaultValue);
        }
        if (restoreValue) {
            newValue = getPersistedInt(mValue);
        }
        setValue(newValue);
    }

    @Override
    public int getValue() {
        return mValue;
    }
}
