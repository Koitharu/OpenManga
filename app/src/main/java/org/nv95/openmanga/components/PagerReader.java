package org.nv95.openmanga.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AdapterViewFlipper;

/**
 * Created by nv95 on 30.09.15.
 *
 */
public class PagerReader extends AdapterViewFlipper {


    private float fromPosition;

    public PagerReader(Context context) {
        super(context);
    }

    public PagerReader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PagerReader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PagerReader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                fromPosition = event.getX();
                return true;
            case MotionEvent.ACTION_UP:
                float toPosition = event.getX();
                if (fromPosition > toPosition)
                    showNext();
                else if (fromPosition < toPosition)
                    showPrevious();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void showPrevious() {
        if (getDisplayedChild() > 0)
            super.showPrevious();
    }

    @Override
    public void showNext() {
        if (getDisplayedChild() < getAdapter().getCount() - 1)
            super.showNext();
    }
}