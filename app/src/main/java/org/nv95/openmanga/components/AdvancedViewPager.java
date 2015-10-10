package org.nv95.openmanga.components;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by nv95 on 07.10.15.
 * View pager with vertical sliding
 */
public class AdvancedViewPager extends ViewPager {
    /** @hide */
    @IntDef({HORIZONTAL, VERTICAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {}

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    //---------------------------------------------------------
    private int orientation;
    private View overscrollFrontView;

    public AdvancedViewPager(Context context) {
        super(context);
        init();
    }

    public AdvancedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        // The easiest way to get rid of the overscroll drawing that happens on the left and right
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    private MotionEvent swapXY(MotionEvent ev) {
        float width = getWidth();
        float height = getHeight();

        float newX = (ev.getY() / height) * width;
        float newY = (ev.getX() / width) * height;

        ev.setLocation(newX, newY);

        return ev;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        boolean intercepted = super.onInterceptTouchEvent(orientation == VERTICAL ? swapXY(ev) : ev);
        if (orientation == VERTICAL) {
            swapXY(ev);
        }
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(orientation == VERTICAL ? swapXY(ev) : ev);
    }

    public @OrientationMode int getOrientation() {
        return orientation;
    }

    public void setOrientation(@OrientationMode int orientation) {
        this.orientation = orientation;
        setPageTransformer(true, orientation == VERTICAL ? new VerticalPageTransformer() : null);
        int pos = getCurrentItem();
        setAdapter(getAdapter());
        if (getAdapter() != null)
            setCurrentItem(pos);
    }

    public View getOverscrollFrontView() {
        return overscrollFrontView;
    }

    public void setOverscrollFrontView(View overscrollFrontView) {
        this.overscrollFrontView = overscrollFrontView;
    }

}
