package org.nv95.openmanga.components;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.nv95.openmanga.PagerReaderAdapter;

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

    public interface OnScrollListener {
        void OnScroll(AdvancedViewPager viewPager, int x, int y, int oldx, int oldy);
        boolean OnOverScroll(AdvancedViewPager viewPager, int deltaX, int direction);
    }

    protected OnScrollListener onScrollListener;

    //---------------------------------------------------------
    private int orientation;
    private boolean reverseOrder;
    private int lastX = -1;

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
        reverseOrder = false;
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
    public int getCurrentItem() {
        int currentItem = super.getCurrentItem();
        if (reverseOrder && getAdapter() != null) {
            return getAdapter().getCount() - 1 - currentItem;
        } else {
            return currentItem;
        }
    }


    @Override
    public void setCurrentItem(int item) {
        if (reverseOrder && getAdapter() != null) {
            item = getAdapter().getCount() - 1 - item;
        }
        super.setCurrentItem(item);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        if (reverseOrder && getAdapter() != null) {
            item = getAdapter().getCount() - 1 - item;
        }
        super.setCurrentItem(item, smoothScroll);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        if (checkForOverscroll(ev))
            return false;
        boolean intercepted = super.onInterceptTouchEvent(orientation == VERTICAL ? swapXY(ev) : ev);
        if (orientation == VERTICAL) {
            swapXY(ev);
        }
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (checkForOverscroll(ev)) {
            return false;
        };
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

    protected boolean checkForOverscroll(MotionEvent event) {
        if (onScrollListener == null || getAdapter() == null) {
            return false;
        }
        int state = 0;
        if (getCurrentItem() == 0) {
            state = -1;
        } else if (getCurrentItem() == getAdapter().getCount() - 1) {
            state = 1;
        }
        if (state == 0) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = onScrollListener.OnOverScroll(this, 0, state) ? (int) event.getX() : -1;
                break;
            case MotionEvent.ACTION_MOVE:
                if (lastX == -1) {
                    return false;
                }
                int deltaX = (int) (event.getX() - lastX);
                if (state == -1 && deltaX > 0) {
                    return onScrollListener.OnOverScroll(this, deltaX, state);
                } else if (state == 1 && deltaX < 0) {
                    return onScrollListener.OnOverScroll(this, -deltaX, state);
                } else {
                    lastX = -1;
                }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (lastX != -1) {
                    onScrollListener.OnOverScroll(this, 0, 0);
                }
                lastX = -1;
                break;
        }
        return false;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollListener != null) {
            onScrollListener.OnScroll(this, l, t, oldl, oldt);
        }
    }

    public boolean isReverseOrder() {
        return reverseOrder;
    }

    public void setReverseOrder(boolean reverseOrder) {
        PagerAdapter adapter = getAdapter();
        if (adapter != null && adapter instanceof PagerReaderAdapter) {
            ((PagerReaderAdapter)adapter).setReversed(reverseOrder);
        }
        if (this.reverseOrder != reverseOrder) {
            int pos = getCurrentItem();
            this.reverseOrder = reverseOrder;
            setCurrentItem(pos);
        }
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        if (adapter != null && adapter instanceof PagerReaderAdapter) {
            ((PagerReaderAdapter)adapter).setReversed(reverseOrder);
        }
        super.setAdapter(adapter);
    }

    public OnScrollListener getOnScrollListener() {
        return onScrollListener;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }
}
