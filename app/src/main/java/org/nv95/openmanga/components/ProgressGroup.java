package org.nv95.openmanga.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

/**
 * Created by nv95 on 17.03.16.
 */
public class ProgressGroup extends LinearLayout {
    public ProgressGroup(Context context) {
        this(context, null, 0);
    }

    public ProgressGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProgressGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setProgressCount(int value) {
        for (int i = getChildCount(); i > value; i--) {
            removeViewAt(i);
        }
        for (int i = getChildCount(); i < value; i++) {
            addView(createProgressBar(), -1,
                    new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof ProgressBar) {
            super.addView(child, index, params);
        } else {
            throw new ClassCastException("Child of ProgressGroup must be an ProgressBar");
        }
    }

    public ProgressBar getProgressBar(int position) {
        return (ProgressBar) getChildAt(position);
    }

    public int getBarsCount() {
        return getChildCount();
    }

    public int getProgress(int position) {
        return getProgressBar(position).getProgress();
    }

    public void setProgress(int position, int value, int max) {
        getProgressBar(position).setMax(max);
        getProgressBar(position).setProgress(value);
    }

    protected ProgressBar createProgressBar() {
        ProgressBar bar = new ProgressBar(getContext(),
                null,
                android.R.attr.progressBarStyleHorizontal);
        return bar;
    }
}
