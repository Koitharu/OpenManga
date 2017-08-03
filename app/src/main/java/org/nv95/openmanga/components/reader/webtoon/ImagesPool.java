package org.nv95.openmanga.components.reader.webtoon;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.components.reader.PageLoader;
import org.nv95.openmanga.components.reader.PageWrapper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by admin on 01.08.17.
 */

public class ImagesPool {

    private final BitmapLruCache<Integer> mCache;
    private final PageLoader mLoader;
    private final ChangesListener mListener;
    private final ExecutorService mExecutor;

    public ImagesPool(Context context, ChangesListener listener) {
        mExecutor = Executors.newSingleThreadExecutor();
        mLoader = new PageLoader(context);
        mListener = listener;
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
            AsyncBitmapDecoder.decode(pw.getFilename(), new AsyncBitmapDecoder.DecodeCallback() {
                @Override
                public void onBitmapDecoded(@NonNull Bitmap bitmap) {
                    mCache.put(pos, bitmap);
                    mListener.notifyDataSetChanged();
                }
            }, mExecutor);
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
