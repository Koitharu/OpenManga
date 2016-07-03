package org.nv95.openmanga.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.nv95.openmanga.R;

/**
 * Created by Владимир on 06.06.2016.
 */

public class StatusBarController extends View {
    private boolean mIsStatusBar;
    private int mLimitYStatusBar;

    public StatusBarController(Context context) {
        this(context, null);
    }

    public StatusBarController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusBarController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StatusBarController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mLimitYStatusBar = getContext().getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin);
    }

    /**
     * Для устройств с системными кнопками
     * При проведении снизу в верх или сверзу вниз у края экрана, для отображения системных кнопок
     * @param ev
     * @return
     */

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if (ev.getY() < mLimitYStatusBar || (getHeight() - ev.getY() < (mLimitYStatusBar * 2))) {
                    mIsStatusBar = true;
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_HOVER_MOVE:
                if (mIsStatusBar)
                    return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsStatusBar = false;
                break;
        }
        return mIsStatusBar || super.onTouchEvent(ev);
    }
}
