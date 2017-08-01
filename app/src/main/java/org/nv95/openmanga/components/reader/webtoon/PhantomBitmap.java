package org.nv95.openmanga.components.reader.webtoon;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

/**
 * Created by admin on 01.08.17.
 */

public class PhantomBitmap {

    private int mHeight;
    private int mWidth;
    @Nullable
    private Bitmap mBitmap;

    @Nullable
    public Bitmap get() {
        return mBitmap;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    public void recycle() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    public void set(Bitmap bitmap) {
        recycle();
        mBitmap = bitmap;
        mHeight = bitmap.getHeight();
        mWidth = bitmap.getWidth();
    }
}
