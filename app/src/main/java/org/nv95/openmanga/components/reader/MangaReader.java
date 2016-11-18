package org.nv95.openmanga.components.reader;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import org.nv95.openmanga.components.reader.recyclerpager.PreCachingLayoutManager;
import org.nv95.openmanga.components.reader.recyclerpager.RecyclerViewPager;

/**
 * Created by nv95 on 15.11.16.
 */

public class MangaReader extends RecyclerViewPager {

    @Nullable
    private PreCachingLayoutManager mLayoutManager;

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
    }

    public void applyConfig(boolean vertical, boolean reverse, boolean sticky) {
        setLayoutManager(mLayoutManager = new PreCachingLayoutManager(
                getContext(),
                vertical ? PreCachingLayoutManager.VERTICAL : PreCachingLayoutManager.HORIZONTAL,
                reverse
        ));
        setSinglePageFling(sticky);
        setSticky(sticky);
    }

    public boolean isVertical() {
        return mLayoutManager != null && mLayoutManager.getOrientation() == PreCachingLayoutManager.VERTICAL;
    }

    public boolean isReversed() {
        return mLayoutManager != null && mLayoutManager.getReverseLayout();
    }

    void scrollToPosition(int pos, boolean animate) {
        if (animate) {
            smoothScrollToPosition(pos);
        } else {
            scrollToPosition(pos);
        }
    }

    public void scrollToNext(boolean animate) {
        int pos = getCurrentPosition();
        if (pos < getItemCount() - 1) {
            scrollToPosition(pos + 1);
        }
    }

    public void scrollToPrevious(boolean animate) {
        int pos = getCurrentPosition();
        if (pos > 0) {
            scrollToPosition(pos - 1);
        }
    }
}
