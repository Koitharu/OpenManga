package org.nv95.openmanga.components;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by nv95 on 06.10.15.
 */
public class ReaderViewPager extends ViewPager {
    private boolean scrolling = false;
    private int lastX, lastY;

    public ReaderViewPager(Context context) {
        super(context);
        setOverScrollMode(OVER_SCROLL_ALWAYS);
    }

    public ReaderViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOverScrollMode(OVER_SCROLL_ALWAYS);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }


}
