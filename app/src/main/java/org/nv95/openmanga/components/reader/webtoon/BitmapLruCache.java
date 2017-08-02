package org.nv95.openmanga.components.reader.webtoon;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by admin on 02.08.17.
 */

public class BitmapLruCache <T> extends LruCache<T, Bitmap> {

    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected void entryRemoved(boolean evicted, T key, Bitmap oldValue, Bitmap newValue) {
        oldValue.recycle();
    }
}
