package org.nv95.openmanga.components.reader.webtoon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import org.nv95.openmanga.components.reader.PageLoader;
import org.nv95.openmanga.components.reader.PageWrapper;

/**
 * Created by admin on 01.08.17.
 */

public class ImagesPool {

    private final LruCache<Integer, Bitmap> mCache;
    private final PageLoader mLoader;

    public ImagesPool(Context context) {
        mLoader = new PageLoader(context);
        mCache = new LruCache<>(4);
    }

    @Nullable
    public PageImage get(int pos) {
        Bitmap bitmap = mCache.get(pos);
        if (bitmap != null) {
            return new PageImage(bitmap);
        }
        PageWrapper pw = mLoader.requestPage(pos);
        if (pw != null && pw.isLoaded()) {
            try {
                bitmap = BitmapFactory.decodeFile(pw.getFilename());
            } catch (Exception e) {
                e.printStackTrace();
                bitmap = null;
            }
        }
        if (bitmap != null) {
            mCache.put(pos, bitmap);
            return new PageImage(bitmap);
        } else {
            return null;
        }
    }

    public PageLoader getLoader() {
        return mLoader;
    }
}
