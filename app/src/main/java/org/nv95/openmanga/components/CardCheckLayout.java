package org.nv95.openmanga.components;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.LayoutUtils;

/**
 * Created by admin on 10.07.17.
 */

public class CardCheckLayout extends CardView implements Checkable {

    private boolean mChecked;
    private static int mPadding;
    private ImageView mCheckMark;

    public CardCheckLayout(Context context) {
        super(context);
        init();
    }

    public CardCheckLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CardCheckLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mChecked = false;
        if (mPadding == 0) {
            mPadding = getResources().getDimensionPixelOffset(R.dimen.padding8);
        }
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked == checked) {
            return;
        }
        mChecked = checked;
        if (mChecked) {
            getCheckMark().setVisibility(VISIBLE);
            LayoutUtils.animatePress(this);
        } else {
            getCheckMark().setVisibility(View.GONE);
            LayoutUtils.animatePress(this);
        }
    }

    private View getCheckMark() {
        if (mCheckMark == null) {
            mCheckMark = new ImageView(getContext());
            mCheckMark.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mCheckMark.setBackgroundResource(R.drawable.selector_frame);
            mCheckMark.setPadding(mPadding, mPadding, mPadding, mPadding);
            mCheckMark.setScaleType(ImageView.ScaleType.MATRIX);
            mCheckMark.setImageResource(R.drawable.ic_checkmark);
            mCheckMark.setVisibility(GONE);
            addView(mCheckMark);
        }
        return mCheckMark;
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }
}