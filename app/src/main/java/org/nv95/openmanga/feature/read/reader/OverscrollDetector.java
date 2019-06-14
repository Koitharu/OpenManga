package org.nv95.openmanga.feature.read.reader;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

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
                int dY = (int) (mStartPoint.y - motionEvent.getY());
                int dX = (int) (mStartPoint.x - motionEvent.getX());
                if (mCanScrollTop && dY < 0)
                    mListener.onOverScrollFlying(OnOverScrollListener.TOP, -dY);
                else if (mCanScrollBottom && dY > 0)
                    mListener.onOverScrollFlying(OnOverScrollListener.BOTTOM,  dY);
                else if (mCanScrollLeft && dX < 0)
                    mListener.onOverScrollFlying(OnOverScrollListener.LEFT,  -dX);
                else if (mCanScrollRight && dX > 0)
                    mListener.onOverScrollFlying(OnOverScrollListener.RIGHT,  dX);
                else return false;
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                dY = (int) (mStartPoint.y - motionEvent.getY());
                dX = (int) (mStartPoint.x - motionEvent.getX());

                if (mCanScrollTop && dY < 0 && mListener.onOverScrollFinished(OnOverScrollListener.TOP, -dY))
                    mListener.onOverScrolled(OnOverScrollListener.TOP);
                else if (mCanScrollBottom && dY > 0 && mListener.onOverScrollFinished(OnOverScrollListener.BOTTOM,  dY))
                    mListener.onOverScrolled(OnOverScrollListener.BOTTOM);
                else if (mCanScrollLeft && dX < 0 && mListener.onOverScrollFinished(OnOverScrollListener.LEFT,  -dX))
                    mListener.onOverScrolled(OnOverScrollListener.LEFT);
                else if (mCanScrollRight && dX > 0 && mListener.onOverScrollFinished(OnOverScrollListener.RIGHT,  dX))
                    mListener.onOverScrolled(OnOverScrollListener.RIGHT);
                else return false;
                return true;
        }
        return false;
    }
}
