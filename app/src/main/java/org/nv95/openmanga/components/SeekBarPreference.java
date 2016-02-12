package org.nv95.openmanga.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.nv95.openmanga.R;

/**
 * Created by nv95 on 12.02.16.
 */
public class SeekBarPreference extends Preference implements AppCompatSeekBar.OnSeekBarChangeListener {
    private static final String ATTR_NS = "http://schemas.android.com/apk/res-auto";
    private TextView valueTextView;
    private int currentValue;
    private int max;
    private Drawable icon;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SeekBarPreferenceAttrs);
        icon = a.getDrawable(R.styleable.SeekBarPreferenceAttrs_iconDrawable);
        max = a.getInt(R.styleable.SeekBarPreferenceAttrs_max, 100);
        currentValue = a.getInt(R.styleable.SeekBarPreferenceAttrs_currentValue, 0);
        a.recycle();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected View onCreateView(ViewGroup parent) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(
                getContext())
                .inflate(R.layout.pref_seekbar, parent, false);
        ((TextView) layout.findViewById(R.id.title)).setText(getTitle());
        AppCompatSeekBar bar = (AppCompatSeekBar) layout.findViewById(R.id.seekBar);
        bar.setMax(max);
        bar.setProgress(currentValue);
        bar.setOnSeekBarChangeListener(this);
        ImageView imageView = (ImageView) layout.findViewById(R.id.icon);
        if (icon != null) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageDrawable(icon);
        }
        valueTextView = (TextView) layout.findViewById(R.id.value);
        valueTextView.setText(String.valueOf(currentValue));
        return layout;
    }

    //Функция, вызываемая каждый раз при перемещении ползунка
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        valueTextView.setText(String.valueOf(progress));
        valueTextView.invalidate();
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    //Функция, вызываемая после окончания движения пользователем
    public void onStopTrackingTouch(SeekBar seekBar) {
        currentValue = seekBar.getProgress();
        updatePreference(currentValue);
        notifyChanged();
    }

    //Сохранение значения настройки
    private void updatePreference(int newValue) {
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(getKey(), newValue);
        editor.commit();
    }
}