package org.nv95.openmanga.common.views.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.support.annotation.ColorInt;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

import org.nv95.openmanga.R;

public final class ColorPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {

	@ColorInt
	private int mValue;
	private boolean mValueSet = false;
	private final int[] mColor = new int[3];
	private View mViewSample;

	public ColorPreference(Context context) {
		this(context, null,android.R.attr.dialogPreferenceStyle);
	}

	public ColorPreference(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.dialogPreferenceStyle);
	}

	public ColorPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setDialogLayoutResource(R.layout.dialog_pref_color);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		setValue(restorePersistedValue ? getPersistedInt(mValue)
				: (Integer) defaultValue);
	}

	private void setValue(@ColorInt int value) {
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

	@ColorInt
	public int getColor() {
		return mValue;
	}

	@Override
	protected View onCreateDialogView() {
		View view = super.onCreateDialogView();
		initSeekBar(view.findViewById(R.id.seekBar_red));
		initSeekBar(view.findViewById(R.id.seekBar_green));
		initSeekBar(view.findViewById(R.id.seekBar_blue));
		mViewSample = view.findViewById(R.id.view_sample);
		return view;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			int i = Integer.parseInt((String) seekBar.getTag());
			mColor[i] = progress;
			mViewSample.setBackgroundColor(Color.rgb(
					mColor[0],
					mColor[1],
					mColor[2]
			));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		mColor[0] = Color.red(mValue);
		mColor[1] = Color.green(mValue);
		mColor[2] = Color.blue(mValue);
		view.<AppCompatSeekBar>findViewById(R.id.seekBar_red).setProgress(mColor[0]);
		view.<AppCompatSeekBar>findViewById(R.id.seekBar_green).setProgress(mColor[1]);
		view.<AppCompatSeekBar>findViewById(R.id.seekBar_blue).setProgress(mColor[2]);
		mViewSample.setBackgroundColor(Color.rgb(
				mColor[0],
				mColor[1],
				mColor[2]));
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			final int newValue = Color.rgb(
					mColor[0],
					mColor[1],
					mColor[2]
			);
			if (callChangeListener(newValue)) {
				setValue(newValue);
			}
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 0);
	}

	private void initSeekBar(AppCompatSeekBar seekBar) {
		seekBar.setPadding(0, 0, 0, 0);
		seekBar.setOnSeekBarChangeListener(this);
	}
}
