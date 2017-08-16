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

    private final PagesLruCache mCache;
    private final PageLoader mLoader;
    private final ChangesListener mListener;
    private final ExecutorService mExecutor;

    public ImagesPool(Context context, ChangesListener listener) {
        mExecutor = Executors.newSingleThreadExecutor();
        mLoader = new PageLoader(context);
        mListener = listener;
        mCache = new PagesLruCache(4);
    }

    @Nullable
    public PageImage get(final int pos) {
        PageImage page = mCache.get(pos);
        if (page != null) {
            return page;
        }
        PageWrapper pw = mLoader.requestPage(pos);
        if (pw != null && pw.isLoaded()) {
            AsyncBitmapDecoder.decode(pw.getFilename(), new AsyncBitmapDecoder.DecodeCallback() {
                @Override
                public void onBitmapDecoded(@NonNull Bitmap bitmap) {
                    mCache.put(pos, new PageImage(bitmap));
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

    public void resetPreScale() {
        for (PageImage o : mCache.snapshot().values()) {
            o.resetPreScale();
        }
    }
}
