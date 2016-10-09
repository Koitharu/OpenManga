package org.nv95.openmanga.lists;

import java.util.ArrayList;

/**
 * Created by nv95 on 25.01.16.
 */
public class PagedList<T> extends ArrayList<T> {

    private int mPages = 0;
    private boolean mHasNext;

    public void appendPage(PagedList<T> list) {
        mPages++;
        this.addAll(list);
    }

    @Override
    public void clear() {
        super.clear();
        mPages = 0;
    }

    public int getPagesCount() {
        return mPages;
    }

    public boolean isHasNext() {
        return mHasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.mHasNext = hasNext;
    }

    public void setPagesCount(int count) {
        mPages = Math.max(count, 0);
    }
}
