package org.nv95.openmanga.components.reader.webtoon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;
import com.davemorrissey.labs.subscaleview.decoder.SkiaImageRegionDecoder;

/**
 * Created by admin on 01.08.17.
 */

public class PhantomBitmap {

    private final int mHeight;
    private final int mWidth;
    @Nullable
    private final ImageRegionDecoder mDecoder;

    public PhantomBitmap(Context context, String filename) {
        ImageRegionDecoder decoder = null;
        Point dimensions;
        try {
            decoder = new SkiaImageRegionDecoder();
            dimensions = decoder.init(context, Uri.parse("file://" + filename));
        } catch (Exception e) {
            dimensions = new Point(0,0);
            e.printStackTrace();
        }
        mWidth = dimensions.x;
        mHeight = dimensions.y;
        mDecoder = decoder;
    }

    public boolean isReady() {
        return mDecoder != null && mDecoder.isReady();
    }

    public Bitmap decode(Rect rect) {
        assert mDecoder != null;
        return mDecoder.decodeRegion(rect, 0);
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    public void recycle() {
        if (mDecoder != null) {
            mDecoder.recycle();
        }
    }
}
