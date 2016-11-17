package org.nv95.openmanga.components.reader;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by nv95 on 15.11.16.
 */

public class MangaReader extends RecyclerView {

    private static final float SCROLL_FACTOR = 1f;

    @Nullable
    private LinearLayoutManager mLayoutManager;
    private boolean mSticky;

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

    private void init(Context context) {
        mLayoutManager = null;
        mSticky = true;
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(mSticky && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    completeScroll();
                }
            }
        });
    }

    public int getCurrentPosition() {
        if (mLayoutManager == null) {
            return RecyclerView.NO_POSITION;
        }
        int lastPos = mLayoutManager.findLastVisibleItemPosition();
        View lastView = mLayoutManager.findViewByPosition(lastPos);
        if (lastView == null) {
            return RecyclerView.NO_POSITION;
        }
        if (isVertical()) {
            int center = getHeight() / 2;
            lastPos = lastView.getTop() < center ? lastPos : lastPos - 1;
        } else {
            int center = getWidth() / 2;
            lastPos = lastView.getLeft() < center ? lastPos : lastPos - 1;
        }
        return lastPos < 0 ? 0 : lastPos;
    }

    private void completeScroll() {
        int pos = getCurrentPosition();
        if (pos != RecyclerView.NO_POSITION) {
            smoothScrollToPosition(pos);
        }
    }

    public void applyConfig(boolean vertical, boolean reverse, boolean sticky) {
        setLayoutManager(mLayoutManager = new LinearLayoutManager(
                getContext(),
                vertical ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL,
                reverse
        ));
        mSticky = sticky;
    }

    public boolean isVertical() {
        return mLayoutManager != null && mLayoutManager.getOrientation() == LinearLayoutManager.VERTICAL;
    }

    public boolean isReversed() {
        return mLayoutManager != null && mLayoutManager.getReverseLayout();
    }
}
