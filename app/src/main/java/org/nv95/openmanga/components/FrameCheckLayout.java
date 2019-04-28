package org.nv95.openmanga.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.AnimUtils;

/**
 * Created by nv95 on 30.06.16.
 */

public class FrameCheckLayout extends FrameLayout implements ExtraCheckable {

    private boolean mChecked;
    private static int mPadding;
    private ImageView mCheckMark;

    public FrameCheckLayout(Context context) {
        super(context);
        init();
    }

    public FrameCheckLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FrameCheckLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FrameCheckLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
        } else {
            getCheckMark().setVisibility(View.GONE);
        }
    }


    @Override
    public void setCheckedAnimated(boolean checked) {
        if (mChecked == checked) {
            return;
        }
        mChecked = checked;
        if (mChecked) {
            AnimUtils.crossfade(null, getCheckMark());
        } else {
            AnimUtils.crossfade(getCheckMark(), null);
        }
    }

    private View getCheckMark() {
        if (mCheckMark == null) {
            mCheckMark = new ImageView(getContext());
            mCheckMark.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mCheckMark.setPadding(mPadding, mPadding, mPadding, mPadding);
            mCheckMark.setScaleType(ImageView.ScaleType.MATRIX);
            //mCheckMark.setImageResource(R.drawable.ic_checkmark);
            mCheckMark.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.black_owerlay_40));
            mCheckMark.setVisibility(GONE);
            ViewCompat.setElevation(mCheckMark, 10000);
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
