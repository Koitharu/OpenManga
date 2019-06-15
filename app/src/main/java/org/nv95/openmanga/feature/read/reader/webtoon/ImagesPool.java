package org.nv95.openmanga.feature.read.reader.webtoon;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.nv95.openmanga.feature.read.reader.PageLoader;
import org.nv95.openmanga.feature.read.reader.PageWrapper;

import java.util.Collections;
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
    private int mBaseWidth = 0;

    public ImagesPool(Context context, ChangesListener listener) {
        mExecutor = Executors.newSingleThreadExecutor();
        mLoader = new PageLoader(context);
        mListener = listener;
        mDecodeQueue = Collections.synchronizedSet(new TreeSet<Integer>());
        mCache = new PagesLruCache(4);
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
                    if (bitmap != null) {
                        int w = bitmap.getWidth();
                        int h = (int)(mBaseWidth / (float)w * bitmap.getHeight());
                        mCache.put(pos, new PageImage(Bitmap.createScaledBitmap(
                                bitmap,
                                mBaseWidth,
                                h,
                                true
                        )));
                        bitmap.recycle();
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

    public void setBaseWidth(int width) {
        if (mBaseWidth != width) {
            mCache.evictAll();
            mBaseWidth = width;
        }
    }
}
