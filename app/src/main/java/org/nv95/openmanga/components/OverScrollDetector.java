package org.nv95.openmanga.components;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by nv95 on 10.04.16.
 */
public abstract class OverScrollDetector implements View.OnTouchListener {
    public static final int DIRECTION_NONE = -1;
    public static final int DIRECTION_LEFT = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_TOP = 2;
    public static final int DIRECTION_BOTTOM = 3;
    public static final float SENSITIVITY_BEGIN = 20f;
    public float mSensitivityDone = 300f;

    private boolean mDown;
    private boolean mFly;
    private float mStartX, mStartY;

    public OverScrollDetector() {
        mDown = false;
        mFly = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDown = true;
                mStartX = event.getX();
                mStartY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mDown) {
                    float dx = mStartX - event.getX();
                    float dy = mStartY - event.getY();
                    final int direction = getDirection(SENSITIVITY_BEGIN, dx, dy);
                    if (mFly) {
                        onFly(direction, dx, dy);
                    } else if (direction != DIRECTION_NONE && canOverScroll(direction)) {
                        onPreOverscroll(direction);
                        mFly = true;
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mFly) {
                    onOverScrolled(getDirection(mSensitivityDone, mStartX - event.getX(), mStartY - event.getY()));
                }
                mDown = false;
                mFly = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                onCancelled(getDirection(SENSITIVITY_BEGIN, mStartX - event.getX(), mStartY - event.getY()));
                mDown = false;
                mFly = false;
                break;
        }
        return true;
    }

    public boolean isOnFly() {
        return mFly;
    }

    public void setSensitivityDone(float value) {
        mSensitivityDone = value;
    }

    private int getDirection(float sensitivity, float deltaX, float deltaY) {
        if (deltaX > sensitivity && deltaX > Math.abs(deltaY)) {
            return DIRECTION_RIGHT;
        }
        if (deltaX < -sensitivity && deltaX < -Math.abs(deltaY)) {
            return DIRECTION_LEFT;
        }
        if (deltaY > sensitivity && deltaY > Math.abs(deltaX)) {
            return DIRECTION_BOTTOM;
        }
        if (deltaY < -sensitivity && deltaY < -Math.abs(deltaX)) {
            return DIRECTION_TOP;
        }
        return DIRECTION_NONE;
    }

    public abstract boolean canOverScroll(int direction);

    public abstract void onFly(int direction, float deltaX, float deltaY);

    public abstract void onOverScrolled(int direction);

    public abstract void onPreOverscroll(int direction);

    public abstract void onCancelled(int direction);
}
