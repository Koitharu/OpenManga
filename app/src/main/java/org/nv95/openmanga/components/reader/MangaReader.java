package org.nv95.openmanga.components.reader;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
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

    private void completeScroll() {
        int pos = getCurrentPosition();
        if (pos != RecyclerView.NO_POSITION) {
            smoothScrollToPosition(pos);
        }
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
}
