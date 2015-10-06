package org.nv95.openmanga.components;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

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
/*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean res = super.onTouchEvent(event);
        if (getCurrentItem() > 0) {
            return res;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                scrolling = true;
                break;
            case MotionEvent.ACTION_UP:
                scrolling = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs(lastX - (int)event.getX());
                int dy = Math.abs(lastY - (int)event.getY());

                break;
        }
        return res;
    }
   */
}
