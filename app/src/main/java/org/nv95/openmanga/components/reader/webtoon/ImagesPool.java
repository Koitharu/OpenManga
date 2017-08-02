package org.nv95.openmanga.components.reader.webtoon;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.components.reader.PageLoader;
import org.nv95.openmanga.components.reader.PageWrapper;

/**
 * Created by admin on 01.08.17.
 */

public class ImagesPool {

    private final BitmapLruCache<Integer> mCache;
    private final PageLoader mLoader;

    public ImagesPool(Context context) {
        mLoader = new PageLoader(context);
        mCache = new BitmapLruCache<>(4);
    }

    @Nullable
    public PageImage get(final int pos) {
        Bitmap bitmap = mCache.get(pos);
        if (bitmap != null) {
            return new PageImage(bitmap);
        }
        PageWrapper pw = mLoader.requestPage(pos);
        if (pw != null && pw.isLoaded()) {
            new AsyncBitmapDecoder(pw.getFilename(), new AsyncBitmapDecoder.DecodeCallback() {
                @Override
                public void onBitmapDecoded(@NonNull Bitmap bitmap) {
                    mCache.put(pos, bitmap);
                }
            }).start();
        }
        return null;
    }

    public PageLoader getLoader() {
        return mLoader;
    }

    public void recycle() {
        mCache.evictAll();
    }
}
