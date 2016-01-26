/*
 * Copyright (C) 2016 Vasily Nikitin
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 */

package org.nv95.openmanga.components;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.nv95.openmanga.R;

/**
 * Created by nv95 on 14.10.15.
 */
@Deprecated
public class InlayoutNotify extends LinearLayout implements View.OnClickListener, Animator.AnimatorListener {
    protected TextView textView;
    protected ImageView imageView;

    public InlayoutNotify(Context context) {
        super(context);
        init();
    }

    public InlayoutNotify(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InlayoutNotify(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InlayoutNotify(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
        boolean alreadyClosed = getContext().getSharedPreferences("iln", Context.MODE_PRIVATE).getBoolean(String.valueOf(getId()), false);
        if (alreadyClosed) {
            this.setVisibility(GONE);
            return;
        }
        this.setOrientation(HORIZONTAL);
        int p = getResources().getDimensionPixelSize(R.dimen.inlayout_notify_padding);
        textView = new TextView(getContext());
        textView.setPadding(p, p, p, p);
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        this.addView(textView);
        imageView = new ImageView(getContext());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_cancel_light));
        int[] attrs = new int[]{android.R.attr.selectableItemBackground /* index 0 */};
        TypedArray ta = getContext().obtainStyledAttributes(attrs);
        Drawable drawableFromTheme = ta.getDrawable(0 /* index */);
        ta.recycle();
        if (Build.VERSION.SDK_INT < 16) {
            imageView.setBackgroundDrawable(drawableFromTheme);
        } else {
            imageView.setBackground(drawableFromTheme);
        }
        imageView.setOnClickListener(this);
        this.addView(imageView);
    }

    public InlayoutNotify setText(String text) {
        if (textView != null) {
            textView.setText(text);
        }
        return this;
    }

    public InlayoutNotify setText(int resid) {
        if (textView != null) {
            textView.setText(resid);
        }
        return this;
    }

    @Override
    public void onClick(View v) {
        this.animate().setInterpolator(new AccelerateInterpolator()).translationX(getMeasuredWidth() + 20).setListener(this).setDuration(100).start();
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        this.setVisibility(GONE);
        getContext().getSharedPreferences("iln", Context.MODE_PRIVATE).edit().putBoolean(String.valueOf(getId()), true).apply();
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
