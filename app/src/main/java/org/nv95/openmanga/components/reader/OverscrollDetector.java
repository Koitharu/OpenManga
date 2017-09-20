package org.nv95.openmanga.components.reader;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import org.nv95.openmanga.utils.DecartUtils;

/**
 * Created by unravel22 on 16.09.17.
 */

public class OverscrollDetector implements View.OnTouchListener {

    private final OnOverScrollListener mListener;
    private boolean mCanScrollLeft, mCanScrollRight, mHandleHorizontal;
    private boolean mCanScrollTop, mCanScrollBottom, mHandleVertical;
    private PointF mStartPoint;

    public OverscrollDetector(OnOverScrollListener onOverScrollListener) {
        this.mListener = onOverScrollListener;
    }

    public void setDirections(boolean handleVertical, boolean handleHorizontal) {
        mHandleHorizontal = handleHorizontal;
        mHandleVertical = handleVertical;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getPointerCount() != 1) return false;
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCanScrollTop = mHandleVertical && !view.canScrollVertically(-1);
                mCanScrollBottom = mHandleVertical && !view.canScrollVertically(1);
                mCanScrollLeft = mHandleHorizontal && !view.canScrollHorizontally(-1);
                mCanScrollRight = mHandleHorizontal && !view.canScrollHorizontally(1);
                if (mCanScrollTop) mListener.onOverScrollStarted(OnOverScrollListener.TOP);
                else if (mCanScrollBottom)
                    mListener.onOverScrollStarted(OnOverScrollListener.BOTTOM);
                else if (mCanScrollLeft) mListener.onOverScrollStarted(OnOverScrollListener.LEFT);
                else if (mCanScrollRight) mListener.onOverScrollStarted(OnOverScrollListener.RIGHT);
                else return false;
                mStartPoint = new PointF(motionEvent.getX(), motionEvent.getY());
                return true;
            case MotionEvent.ACTION_MOVE:
                if (mCanScrollTop)
                    mListener.onOverScrollFlying(OnOverScrollListener.TOP, Math.max(0, mStartPoint.y - motionEvent.getY()));
                else if (mCanScrollBottom)
                    mListener.onOverScrollFlying(OnOverScrollListener.BOTTOM,  Math.max(0, motionEvent.getY() - mStartPoint.y));
                else if (mCanScrollLeft)
                    mListener.onOverScrollFlying(OnOverScrollListener.LEFT,  Math.max(0, mStartPoint.x - motionEvent.getX()));
                else if (mCanScrollRight)
                    mListener.onOverScrollFlying(OnOverScrollListener.RIGHT,  Math.max(0, motionEvent.getX() - mStartPoint.x));
                else return false;
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mCanScrollTop) mListener.onOverScrollCancelled(OnOverScrollListener.TOP);
                else if (mCanScrollBottom)
                    mListener.onOverScrollCancelled(OnOverScrollListener.BOTTOM);
                else if (mCanScrollLeft) mListener.onOverScrollCancelled(OnOverScrollListener.LEFT);
                else if (mCanScrollRight)
                    mListener.onOverScrollCancelled(OnOverScrollListener.RIGHT);
                else return false;
                return true;

        }
        return false;
    }
}
