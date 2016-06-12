package org.nv95.openmanga.utils.imagecontroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.nv95.openmanga.R;

/**
 * Created by Владимир on 06.06.2016.
 */

public class StatusBarChecker extends View {
    private boolean isStatusBar;
    private int limitYstatusBar;

    public StatusBarChecker(Context context) {
        this(context, null);
    }

    public StatusBarChecker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusBarChecker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("NewApi")
    public StatusBarChecker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        limitYstatusBar = getContext().getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin);
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

                if (ev.getY() < limitYstatusBar || (getHeight() - ev.getY() < (limitYstatusBar * 2))) {
                    isStatusBar = true;
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_HOVER_MOVE:
                if (isStatusBar)
                    return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isStatusBar = false;
                break;
        }
        return isStatusBar || super.onTouchEvent(ev);
    }
}
