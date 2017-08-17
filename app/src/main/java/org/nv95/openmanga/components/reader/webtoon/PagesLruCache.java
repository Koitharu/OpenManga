package org.nv95.openmanga.components.reader.webtoon;

import android.util.LruCache;

/**
 * Created by admin on 02.08.17.
 */

public class PagesLruCache extends LruCache<Integer, PageImage> {

    public PagesLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected void entryRemoved(boolean evicted, Integer key, PageImage oldValue, PageImage newValue) {
        oldValue.recycle();
    }

    public boolean contains(Integer key) {
        PageImage value = get(key);
        return value != null && !value.isRecycled();
    }
}
