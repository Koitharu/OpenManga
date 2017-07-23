package org.nv95.openmanga.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import org.nv95.openmanga.R;

import java.util.ArrayList;

/**
 * Created by admin on 23.07.17.
 */

public class IntSelectPreference extends DialogPreference implements AdapterView.OnItemClickListener {

    private final int mMaxValue;
    private final int mMinValue;
    private int mValue;
    private int mNewValue;
    private boolean mValueSet;
    private final ArrayList<Integer> mValues;


    public IntSelectPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.pref_intselect);
        TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.IntSelectPreferenceAttrs);
        mMaxValue = a.getInt(R.styleable.IntSelectPreferenceAttrs_maxValue, 4);
        mMinValue = a.getInt(R.styleable.IntSelectPreferenceAttrs_minValue, 0);
        mValue = 1;
        a.recycle();
        mValueSet = false;
        mValues = new ArrayList<>();
        for (int i = mMinValue; i <= mMaxValue; i++) {
            mValues.add(i);
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        GridView gridView = view.findViewById(R.id.gridView);
        gridView.setNumColumns(Math.min(mValues.size(), 5));
        gridView.setAdapter(new ArrayAdapter<>(
                view.getContext(),
                R.layout.item_cell_selectable,
                android.R.id.text1,
                mValues
        ));
        gridView.setItemChecked(mValues.indexOf(mValue), true);
        gridView.setOnItemClickListener(this);
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
            if (changeListener == null || changeListener.onPreferenceChange(this, mNewValue)) {
                setValue(mNewValue);
            }
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        mNewValue = restoreValue ? getPersistedInt(mValue) : (int) defaultValue;
        setValue(mNewValue);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mNewValue = mValues.get(i);
    }

    public int getValue() {
        return mValue;
    }
}
