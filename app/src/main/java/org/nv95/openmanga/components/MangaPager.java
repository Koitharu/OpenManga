package org.nv95.openmanga.components;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.nv95.openmanga.PagerReaderAdapter;
import org.nv95.openmanga.items.MangaPage;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by nv95 on 04.01.16.
 */
public class MangaPager extends ViewPager {

    private PagerReaderAdapter mAdapter;
    private ArrayList<MangaPage> mList;
    private boolean mReverse = false;
    private boolean mVertical = false;
    private OverScrollListener mOverScrollListener;
    private int lastX = -1;

    public MangaPager(Context context) {
        super(context);
        init(context);
    }

    public MangaPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mList = new ArrayList<>();
        mAdapter = new PagerReaderAdapter(context, mList);
        setOverScrollMode(OVER_SCROLL_NEVER);
        super.setAdapter(mAdapter);
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
        if (checkForOverscroll(ev))
            return false;
        boolean intercepted = super.onInterceptTouchEvent(mVertical ? swapXY(ev) : ev);
        if (mVertical) {
            swapXY(ev);
        }
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (checkForOverscroll(ev)) {
            return false;
        }
        ;
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

    public void setReverse(boolean reverse) {
        if (mReverse != reverse) {
            int pos = getCurrentPageIndex();
            mReverse = reverse;
            Collections.reverse(mList);
            mAdapter.notifyDataSetChanged();
            setAdapter(mAdapter);
            setCurrentPageIndex(pos);
        }
    }

    public boolean isVertical() {
        return mVertical;
    }

    public void setVertical(boolean vertical) {
        if (mVertical != vertical) {
            mVertical = vertical;
            setPageTransformer(true, vertical ? new VerticalPageTransformer() : null);
            mAdapter.notifyDataSetChanged();
            setAdapter(mAdapter);
        }
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

    protected boolean checkForOverscroll(MotionEvent event) {
        if (mOverScrollListener == null) {
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
                lastX = mOverScrollListener.OnOverScroll(this, 0, state) ? (int) event.getX() : -1;
                break;
            case MotionEvent.ACTION_MOVE:
                if (lastX == -1) {
                    return false;
                }
                int deltaX = (int) (event.getX() - lastX);
                if (state == -1 && deltaX > 0) {
                    return mOverScrollListener.OnOverScroll(this, deltaX, state);
                } else if (state == 1 && deltaX < 0) {
                    return mOverScrollListener.OnOverScroll(this, -deltaX, state);
                } else {
                    lastX = -1;
                }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (lastX != -1) {
                    mOverScrollListener.OnOverScroll(this, 0, 0);
                }
                lastX = -1;
                break;
        }
        return false;
    }

    public interface OverScrollListener {
        boolean OnOverScroll(MangaPager viewPager, int deltaX, int direction);
    }
}
