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

    @Override
    protected int sizeOf(Integer key, PageImage pageImage) {
        // The cache size will be measured in kilobytes rather than
        // number of items.
        return pageImage.bytesSize() / 1024;
    }

    public boolean contains(Integer key) {
        return get(key) != null;
    }
}
