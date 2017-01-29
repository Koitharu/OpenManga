package org.nv95.openmanga.components.reader;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.nv95.openmanga.components.reader.recyclerpager.PreCachingLayoutManager;
import org.nv95.openmanga.components.reader.recyclerpager.RecyclerViewPager;
import org.nv95.openmanga.components.reader.recyclerpager.overscroll.HorizontalOverScrollBounceEffectDecorator;
import org.nv95.openmanga.components.reader.recyclerpager.overscroll.IOverScrollDecor;
import org.nv95.openmanga.components.reader.recyclerpager.overscroll.IOverScrollState;
import org.nv95.openmanga.components.reader.recyclerpager.overscroll.IOverScrollStateListener;
import org.nv95.openmanga.components.reader.recyclerpager.overscroll.IOverScrollUpdateListener;
import org.nv95.openmanga.components.reader.recyclerpager.overscroll.RecyclerViewOverScrollDecorAdapter;
import org.nv95.openmanga.components.reader.recyclerpager.overscroll.VerticalOverScrollBounceEffectDecorator;

/**
 * Created by nv95 on 15.11.16.
 */

public class MangaReader extends RecyclerViewPager implements IOverScrollUpdateListener, IOverScrollStateListener {

    private static final float OVERSCROLL_THRESHOLD = 80;

    @Nullable
    private IOverScrollDecor mOverScrollDecor;
    @Nullable
    private PreCachingLayoutManager mLayoutManager;
    @Nullable
    private OnOverScrollListener mOverScrollListener;
    private int mOverScrollDirection;
    private float mOverScrollFactor;
    private boolean mNavEnabled;
    private float[] mNavPoint;

    public MangaReader(Context context) {
        super(context);
        init(context);
    }

    public MangaReader(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MangaReader(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    
    private int getSide(float x) {
        final int w = getResources().getDisplayMetrics().widthPixels;
        final float sw = w / 3.f;
        if (x < sw) {
            return -1;
        } else if (x > (sw * 2.f)) {
            return 1;
        } else {
            return 0;
        }
    }

    private void init(Context context) {
        mLayoutManager = null;
        mNavEnabled = false;
        mNavPoint = new float[2];
        addOnItemTouchListener(new OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent ev) {
                if (mNavEnabled && rv.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    switch (ev.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mNavPoint[0] = ev.getX();
                            if (getSide(mNavPoint[0]) == 0) {
                                return false;
                            }
                            mNavPoint[1] = ev.getY();
                            return true;
                        case MotionEvent.ACTION_UP:
                            float dx = mNavPoint[0] - ev.getX();
                            float dy = mNavPoint[1] - ev.getY();
                            double delta = Math.sqrt(dx * dx + dy * dy);
                            if (delta < 10) {
                                int d = getSide(mNavPoint[0]) * (isReversed() ? -1 : 1);
                                smoothScrollToPosition(getCurrentPosition() + d);
                            }
                            break;
                    }
                }
                return false;
            }
    
            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        
            }
    
            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        
            }
        });
    }

    public void applyConfig(boolean vertical, boolean reverse, boolean sticky) {
        int oldPos = getCurrentPosition();
        setLayoutManager(mLayoutManager = new PreCachingLayoutManager(
                getContext(),
                vertical ? PreCachingLayoutManager.VERTICAL : PreCachingLayoutManager.HORIZONTAL,
                reverse
        ));
        setSinglePageFling(sticky);
        setSticky(sticky);
        if (mOverScrollDecor != null) {
            mOverScrollDecor.detach();
        }
        mOverScrollDecor = vertical ?
                new VerticalOverScrollBounceEffectDecorator(new RecyclerViewOverScrollDecorAdapter(this))
                : new HorizontalOverScrollBounceEffectDecorator(new RecyclerViewOverScrollDecorAdapter(this));
        mOverScrollDecor.setOverScrollUpdateListener(this);
        mOverScrollDecor.setOverScrollStateListener(this);
        if (oldPos != RecyclerView.NO_POSITION) {
            scrollToPosition(oldPos);
        }
    }

    public boolean isVertical() {
        return mLayoutManager != null && mLayoutManager.getOrientation() == PreCachingLayoutManager.VERTICAL;
    }

    public boolean isReversed() {
        return mLayoutManager != null && mLayoutManager.getReverseLayout();
    }

    public void setOnOverScrollListener(OnOverScrollListener listener) {
        mOverScrollListener = listener;
    }
    
    public void setTapNavs(boolean val) {
        mNavEnabled = val;
    }

    void scrollToPosition(int pos, boolean animate) {
        if (animate) {
            smoothScrollToPosition(pos);
        } else {
            scrollToPosition(pos);
        }
    }

    public boolean scrollToNext(boolean animate) {
        int pos = getCurrentPosition();
        if (pos < getItemCount() - 1) {
            scrollToPosition(pos + 1, animate);
            return true;
        } else {
            return false;
        }
    }

    public boolean scrollToPrevious(boolean animate) {
        int pos = getCurrentPosition();
        if (pos > 0) {
            scrollToPosition(pos - 1, animate);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onOverScrollUpdate(IOverScrollDecor decor, int state, float offset) {
        mOverScrollFactor = offset;
        if (mOverScrollListener != null) {
            mOverScrollListener.onOverScrollFlying(mOverScrollDirection, offset);
        }
    }

    @Override
    public void onOverScrollStateChange(IOverScrollDecor decor, int oldState, int newState) {
        switch (newState) {
            case IOverScrollState.STATE_IDLE:
                if (mOverScrollListener != null) {
                    mOverScrollListener.onOverScrollCancelled(mOverScrollDirection);
                }
                // No over-scroll is in effect.
                break;
            case IOverScrollState.STATE_DRAG_START_SIDE:
                // Dragging started at the left-end.
                mOverScrollFactor = 0;
                mOverScrollDirection = isVertical() ? OnOverScrollListener.TOP : OnOverScrollListener.LEFT;
                if (mOverScrollListener != null) {
                    mOverScrollListener.onOverScrollStarted(mOverScrollDirection);
                }
                break;
            case IOverScrollState.STATE_DRAG_END_SIDE:
                // Dragging started at the right-end.
                mOverScrollFactor = 0;
                mOverScrollDirection = isVertical() ? OnOverScrollListener.BOTTOM : OnOverScrollListener.RIGHT;
                if (mOverScrollListener != null) {
                    mOverScrollListener.onOverScrollStarted(mOverScrollDirection);
                }
                break;
            case IOverScrollState.STATE_BOUNCE_BACK:
                if (oldState == IOverScrollState.STATE_DRAG_START_SIDE || oldState == IOverScrollState.STATE_DRAG_END_SIDE) {
                    if (mOverScrollListener != null) {
                        if (Math.abs(mOverScrollFactor) < OVERSCROLL_THRESHOLD) {
                            mOverScrollListener.onOverScrollCancelled(mOverScrollDirection);
                        } else {
                            mOverScrollListener.onOverScrolled(mOverScrollDirection);
                        }
                    }
                    // Dragging stopped -- view is starting to bounce back from the *left-end* onto natural position.
                } else { // i.e. (oldState == STATE_DRAG_END_SIDE)
                    // View is starting to bounce back from the *right-end*.
                }
                break;
        }
    }
}
