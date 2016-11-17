package org.nv95.openmanga.components.pager;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.reader.imagecontroller.PagerReaderAdapter;
import org.nv95.openmanga.items.MangaPage;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by nv95 on 04.01.16.
 */
public class MangaPager extends ViewPager {

    public static final int TRANSFORM_MODE_SCROLL = 0;
    public static final int TRANSFORM_MODE_SLIDE = 1;

    private PagerReaderAdapter mAdapter;
    private ArrayList<MangaPage> mList;
    private boolean mReverse, mVertical;
    private int mTransformMode;
    private OverScrollListener mOverScrollListener;
    private OverScrollDetector mDetector;

    public MangaPager(Context context) {
        super(context);
        init(context);
    }

    public MangaPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mTransformMode = TRANSFORM_MODE_SCROLL;
        mReverse = false;
        mVertical = false;
        mList = new ArrayList<>();
        mAdapter = new PagerReaderAdapter(context, mList);
        setOverScrollMode(OVER_SCROLL_NEVER);
        initDetector();
        super.setAdapter(mAdapter);
    }

    private void initDetector() {
        mDetector = new OverScrollDetector(getContext()) {
            @SuppressWarnings("SimplifiableIfStatement")
            @Override
            public boolean canOverScroll(int direction) {
                if (direction == DIRECTION_LEFT) {
                    return getCurrentItem() == 0 &&
                            (mOverScrollListener == null || mOverScrollListener.canOverScroll(direction));
                } else if (direction == DIRECTION_RIGHT) {
                    return getCurrentItem() == getCount() - 1 &&
                            (mOverScrollListener == null || mOverScrollListener.canOverScroll(direction));
                } else {
                    return false;
                }
            }

            @Override
            public void onOverScroll(int direction, float deltaX, float deltaY) {
                if (mOverScrollListener != null) {
                    mOverScrollListener.onOverScroll(direction, deltaX, deltaY);
                }
            }

            @Override
            public void onOverScrolled(int direction) {
                if (mOverScrollListener != null) {
                    mOverScrollListener.onOverScrollDone(direction);
                }
            }

            @Override
            public void onPreOverscroll(int direction) {
                if (mOverScrollListener != null) {
                    mOverScrollListener.onPreOverScroll(direction);
                }
            }

            @Override
            public void onCancelled(int direction) {
                if (mOverScrollListener != null) {
                    mOverScrollListener.onCancelled(direction);
                }
            }

            @Override
            public void onSwipeLeft() {
                if (mOverScrollListener != null) {
                    mOverScrollListener.onSwipeLeft();
                }
            }

            @Override
            public void onSwipeRight() {
                if (mOverScrollListener != null) {
                    mOverScrollListener.onSwipeRight();
                }
            }
        };
        mDetector.setSensitivityDone(getContext().getResources().getDimensionPixelSize(R.dimen.overscroll_size));
    }

    public void setPages(ArrayList<MangaPage> pages, int position) {
        mList.clear();
        mList.addAll(pages);
        if (mReverse) {
            Collections.reverse(mList);
            position = mList.size() - position - 1;
        }
        mAdapter.notifyDataSetChanged();
        setAdapter(mAdapter);
        setCurrentItem(position, false);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mDetector.onTouch(this, ev);
        boolean intercepted = super.onInterceptTouchEvent(mVertical ? swapXY(ev) : ev);
        if (mVertical) {
            swapXY(ev);
        }
        return intercepted && !mDetector.isOnFly();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mDetector.onTouch(this, ev);
        return super.onTouchEvent(mVertical ? swapXY(ev) : ev);
    }

    public int getCurrentPageIndex() {
        int index = getCurrentItem();
        return mReverse ? mAdapter.getCount() - index - 1 : index;
    }

    public void setCurrentPageIndex(int index) {
        setCurrentItem(mReverse ? mAdapter.getCount() - index - 1 : index, false);
    }

    public void scrollToPage(int index) {
        setCurrentItem(mReverse ? mAdapter.getCount() - index - 1 : index, true);
    }

    public PagerReaderAdapter getReaderAdapter() {
        return mAdapter;
    }

    public int getCount() {
        return mAdapter.getCount();
    }

    public MangaPage getCurrentPage() {
        return mList.get(getCurrentItem());
    }

    public ArrayList<MangaPage> getPages() {
        return mList;
    }

    public boolean isReverse() {
        return mReverse;
    }

    public boolean isVertical() {
        return mVertical;
    }

    public void setBehavior(boolean vertical, boolean reverse, int transformMode, int scaleMode) {
        final int pos = getCurrentPageIndex();
        mVertical = vertical;
        if (mReverse != reverse) {
            mReverse = reverse;
            Collections.reverse(mList);
        }
        mTransformMode = transformMode;
        switch (mTransformMode) {
            case TRANSFORM_MODE_SCROLL:
                setPageTransformer(true, mVertical ? new VerticalPageTransformer() : null);
                break;
            case TRANSFORM_MODE_SLIDE:
                if (mVertical) {
                    setPageTransformer(true, new VerticalSlidePageTransformer());
                } else {
                    setPageTransformer(!mReverse, new SlidePageTransformer(mReverse));
                }
                break;
        }
        setAdapter(null);
        mAdapter.setScaleMode(scaleMode);
        setAdapter(mAdapter);
        setCurrentPageIndex(pos);
    }

    public void setOverScrollListener(OverScrollListener overScrollListener) {
        this.mOverScrollListener = overScrollListener;
    }

    private MotionEvent swapXY(MotionEvent ev) {
        float width = getWidth();
        float height = getHeight();
        float newX = (ev.getY() / height) * width;
        float newY = (ev.getX() / width) * height;
        ev.setLocation(newX, newY);
        return ev;
    }

    public interface OverScrollListener {
        void onOverScrollDone(int direction);
        void onOverScroll(int direction, float deltaX, float deltaY);
        boolean canOverScroll(int direction);
        void onPreOverScroll(int direction);
        void onCancelled(int direction);
        void onSwipeLeft();
        void onSwipeRight();
    }

    //это что и зачем?
    int getMaxScroll(int distance){
        int maxScroll = getWidth() / 2;
        if(Math.abs(distance) > maxScroll)
            return distance > 0 ? maxScroll : -maxScroll;
        return distance;
    }

    public void onConfigurationChange(Activity activity){
        mAdapter.setIsLandOrientation(getOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE);
    }

    public static int getOrientation(Activity mContext){
        final int orientation = mContext.getResources().getConfiguration().orientation;
        final int rotation = mContext.getWindowManager().getDefaultDisplay().getRotation();

        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return Configuration.ORIENTATION_PORTRAIT;
            }
            else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return Configuration.ORIENTATION_PORTRAIT;
            }
            else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return Configuration.ORIENTATION_LANDSCAPE;
    }
}
