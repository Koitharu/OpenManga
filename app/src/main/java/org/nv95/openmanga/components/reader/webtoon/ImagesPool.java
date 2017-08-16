package org.nv95.openmanga.components.reader.webtoon;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import org.nv95.openmanga.components.reader.PageLoader;
import org.nv95.openmanga.components.reader.PageWrapper;

import java.util.Set;
import java.util.TreeSet;
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
    private final Set<Integer> mDecodeQueue;

    private final int MAX_MEMORY = (int) (Runtime.getRuntime().maxMemory() / 1024);

    public ImagesPool(Context context, ChangesListener listener) {
        mExecutor = Executors.newSingleThreadExecutor();
        mLoader = new PageLoader(context);
        mListener = listener;
        mDecodeQueue = new TreeSet<>();
        mCache = new PagesLruCache(MAX_MEMORY / 8);
    }

    @Nullable
    public PageImage get(int pos) {
        PageImage page = mCache.get(pos);
        if (page != null && !page.isRecycled()) {
            return page;
        }
        load(pos);
        return null;
    }

    public void prefetch(final int pos) {
        if (!mCache.contains(pos)) {
            load(pos);
        }
    }

    private void load(final int pos) {
        PageWrapper pw = mLoader.requestPage(pos);
        if (pw != null && pw.isLoaded() && !mDecodeQueue.contains(pos)) {
            mDecodeQueue.add(pos);
            AsyncBitmapDecoder.decode(pw.getFilename(), new AsyncBitmapDecoder.DecodeCallback() {
                @WorkerThread
                @Override
                public void onBitmapDecoded(@Nullable Bitmap bitmap) {
                    Log.d("DECODE", pos+"");
                    if (bitmap != null) {
                        mCache.put(pos, new PageImage(bitmap));
                        mListener.notifyDataSetChanged();
                    }
                    mDecodeQueue.remove(pos);
                }
            }, mExecutor);
        }
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
